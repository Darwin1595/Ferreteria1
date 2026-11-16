using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;
using Proinstalar.API.DTOs;
using Proinstalar.API.Models;

namespace Proinstalar.API.Controllers
{
    // RF-02-04 y RF-02-05: registrar compras y actualizar existencias (solo ADMIN)
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "ADMIN")]
    public class ComprasController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ComprasController(AppDbContext context)
        {
            _context = context;
        }

        private int UsuarioId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var compras = await _context.Compras
                .Include(c => c.Proveedor)
                .Include(c => c.Detalles)
                .OrderByDescending(c => c.Fecha)
                .ToListAsync();

            return Ok(compras.Select(c => new
            {
                c.Id,
                c.Fecha,
                c.Total,
                ProveedorNombre = c.Proveedor != null ? c.Proveedor.Nombre : "",
                c.Observacion,
                Items = c.Detalles.Select(d => new { d.Nombre, d.Cantidad, d.CostoUnitario }).ToList()
            }));
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] CompraCreateDto dto)
        {
            if (dto.Items == null || dto.Items.Count == 0)
                return BadRequest(new { mensaje = "Agrega al menos un producto a la compra" });
            if (dto.Items.Any(i => i.Cantidad <= 0 || i.CostoUnitario <= 0))
                return BadRequest(new { mensaje = "Cantidad y costo unitario deben ser mayores a 0" });

            var proveedor = await _context.Proveedores.FirstOrDefaultAsync(p => p.Id == dto.ProveedorId && p.Estado);
            if (proveedor == null)
                return BadRequest(new { mensaje = "Selecciona un proveedor válido" });

            var ids = dto.Items.Select(i => i.ProductoId).Distinct().ToList();
            var productos = await _context.Productos.Where(p => ids.Contains(p.Id) && p.Estado)
                .ToDictionaryAsync(p => p.Id);

            var compra = new Compra
            {
                ProveedorId = proveedor.Id,
                Observacion = (dto.Observacion ?? "").Trim(),
                Fecha = DateTime.Now
            };
            var movimientos = new List<MovimientoStock>();

            foreach (var item in dto.Items)
            {
                if (!productos.TryGetValue(item.ProductoId, out var prod))
                    return BadRequest(new { mensaje = $"El producto {item.ProductoId} no existe o está deshabilitado" });

                prod.Stock += item.Cantidad; // entra mercancía
                compra.Detalles.Add(new DetalleCompra
                {
                    ProductoId = prod.Id,
                    Nombre = prod.Nombre,
                    Cantidad = item.Cantidad,
                    CostoUnitario = item.CostoUnitario
                });
                movimientos.Add(MovimientoStock.Crear(prod, "ENTRADA", item.Cantidad,
                    $"Compra a {proveedor.Nombre}", "", UsuarioId));
            }

            compra.Total = compra.Detalles.Sum(d => d.CostoUnitario * d.Cantidad);

            // Todo o nada: compra + existencias + movimientos en una sola transacción
            await using var tx = await _context.Database.BeginTransactionAsync();
            _context.Compras.Add(compra);
            await _context.SaveChangesAsync();

            foreach (var m in movimientos) m.Referencia = $"COMPRA-{compra.Id}";
            _context.MovimientosStock.AddRange(movimientos);
            await _context.SaveChangesAsync();
            await tx.CommitAsync();

            return Ok(new { id = compra.Id, total = compra.Total, mensaje = "Compra registrada y existencias actualizadas" });
        }
    }
}