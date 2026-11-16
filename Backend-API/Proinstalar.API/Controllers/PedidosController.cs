using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;
using Proinstalar.API.DTOs;
using Proinstalar.API.Models;

namespace Proinstalar.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Route("api/ventas")]
    [Authorize]
    public class PedidosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public PedidosController(AppDbContext context)
        {
            _context = context;
        }

        private int UsuarioId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        // Venta/compra del cliente: valida stock, descuenta inventario, registra el pedido
        // y deja un movimiento de stock (SALIDA) por cada producto.
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] PedidoCreateDto dto)
        {
            if (dto.Items == null || dto.Items.Count == 0)
                return BadRequest(new { mensaje = "El carrito está vacío" });

            var usuario = await _context.Usuarios.FindAsync(UsuarioId);
            if (usuario == null)
                return Unauthorized(new { mensaje = "Usuario no válido, inicia sesión de nuevo" });

            // Agrupa por producto por si llega repetido
            var pedidos = dto.Items
                .Where(i => i.Cantidad > 0)
                .GroupBy(i => i.ProductoId)
                .Select(g => new { ProductoId = g.Key, Cantidad = g.Sum(x => x.Cantidad) })
                .ToList();

            if (pedidos.Count == 0)
                return BadRequest(new { mensaje = "Cantidades inválidas" });

            var ids = pedidos.Select(p => p.ProductoId).ToList();
            var productos = await _context.Productos
                .Where(p => ids.Contains(p.Id) && p.Estado)
                .ToDictionaryAsync(p => p.Id);

            var pedido = new Pedido
            {
                UsuarioId = usuario.Id,
                Direccion = string.IsNullOrWhiteSpace(dto.Direccion) ? usuario.Direccion : dto.Direccion.Trim(),
                // Venta de mostrador (admin) queda entregada; la del cliente queda pagada
                Estado = User.IsInRole("ADMIN") ? "ENTREGADO" : "PAGADO",
                FechaCreacion = DateTime.Now
            };

            foreach (var linea in pedidos)
            {
                if (!productos.TryGetValue(linea.ProductoId, out var prod))
                    return BadRequest(new { mensaje = $"El producto {linea.ProductoId} ya no está disponible" });

                // RF-02-07: el stock nunca queda negativo
                if (prod.Stock < linea.Cantidad)
                    return BadRequest(new { mensaje = $"Stock insuficiente para '{prod.Nombre}' (disponible: {prod.Stock})" });

                prod.Stock -= linea.Cantidad;
                pedido.Items.Add(new PedidoItem
                {
                    ProductoId = prod.Id,
                    Nombre = prod.Nombre,
                    Cantidad = linea.Cantidad,
                    PrecioUnitario = prod.Precio
                });
            }

            pedido.Total = pedido.Items.Sum(i => i.PrecioUnitario * i.Cantidad);

            // Pedido + descuento de stock + movimientos: todo o nada
            await using var tx = await _context.Database.BeginTransactionAsync();
            _context.Pedidos.Add(pedido);
            await _context.SaveChangesAsync();

            foreach (var item in pedido.Items)
            {
                var prod = productos[item.ProductoId];
                _context.MovimientosStock.Add(MovimientoStock.Crear(prod, "SALIDA", -item.Cantidad,
                    "Venta", $"VENTA-{pedido.Id}", usuario.Id));
            }
            await _context.SaveChangesAsync();
            await tx.CommitAsync();

            return Ok(new PedidoResponseDto(pedido.Id, pedido.Total, pedido.Estado, pedido.FechaCreacion,
                "Tu compra fue aprobada. ¡Gracias por confiar en PROINSTALAR!"));
        }

        // Historial de compras del usuario autenticado
        [HttpGet("mis-pedidos")]
        public async Task<IActionResult> MisPedidos()
        {
            var uid = UsuarioId;
            var pedidos = await _context.Pedidos
                .Where(p => p.UsuarioId == uid)
                .Include(p => p.Items)
                .OrderByDescending(p => p.FechaCreacion)
                .ToListAsync();
            return Ok(pedidos);
        }

        // Admin: todos los pedidos con los datos del cliente
        [HttpGet]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> GetAll()
        {
            var pedidos = await _context.Pedidos.Include(p => p.Items)
                .OrderByDescending(p => p.FechaCreacion).ToListAsync();
            var ids = pedidos.Select(p => p.UsuarioId).Distinct().ToList();
            var usuarios = await _context.Usuarios.Where(u => ids.Contains(u.Id)).ToDictionaryAsync(u => u.Id);

            var res = pedidos.Select(p =>
            {
                usuarios.TryGetValue(p.UsuarioId, out var u);
                return new
                {
                    p.Id,
                    p.Total,
                    p.Estado,
                    p.Direccion,
                    p.FechaCreacion,
                    ClienteNombre = u?.Nombre ?? "(usuario eliminado)",
                    ClienteEmail = u?.Email ?? "",
                    Items = p.Items.Select(i => new { i.Nombre, i.Cantidad, i.PrecioUnitario }).ToList()
                };
            }).ToList();

            return Ok(res);
        }

        // Admin: cambia el estado de un pedido
        [HttpPut("{id}/estado")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> CambiarEstado(int id, [FromBody] PedidoEstadoDto dto)
        {
            var permitidos = new[] { "PAGADO", "ENVIADO", "ENTREGADO" };
            var estado = (dto.Estado ?? "").Trim().ToUpper();
            if (!permitidos.Contains(estado))
                return BadRequest(new { mensaje = "Estado no válido" });

            var p = await _context.Pedidos.FindAsync(id);
            if (p == null) return NotFound(new { mensaje = "Pedido no encontrado" });

            p.Estado = estado;
            await _context.SaveChangesAsync();
            return Ok(new { mensaje = "Estado actualizado" });
        }
    }
}