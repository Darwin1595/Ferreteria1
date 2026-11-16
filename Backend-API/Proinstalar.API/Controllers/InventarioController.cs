using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;
using Proinstalar.API.DTOs;
using Proinstalar.API.Models;

namespace Proinstalar.API.Controllers
{
    // RF-02-05, 07, 08 y 09: existencias, ajustes, movimientos y alertas (solo ADMIN)
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "ADMIN")]
    public class InventarioController : ControllerBase
    {
        private readonly AppDbContext _context;

        public InventarioController(AppDbContext context)
        {
            _context = context;
        }

        private int UsuarioId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        private static string EstadoDe(Producto p) =>
            p.Stock <= 0 ? "AGOTADO" : p.Stock <= p.StockMinimo ? "STOCK_BAJO" : "DISPONIBLE";

        private static object ToItem(Producto p) => new
        {
            p.Id,
            p.Nombre,
            p.Descripcion,
            p.CategoriaId,
            Categoria = p.Categoria != null ? p.Categoria.Nombre : "",
            p.Precio,
            p.Stock,
            p.StockMinimo,
            p.ImagenUrl,
            EstadoStock = EstadoDe(p)
        };

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var lista = await _context.Productos.Include(p => p.Categoria)
                .Where(p => p.Estado).OrderBy(p => p.Nombre).ToListAsync();
            return Ok(lista.Select(ToItem));
        }

        // RF-02-09: productos en o por debajo del mínimo
        [HttpGet("alertas")]
        public async Task<IActionResult> Alertas()
        {
            var lista = await _context.Productos.Include(p => p.Categoria)
                .Where(p => p.Estado && p.Stock <= p.StockMinimo)
                .OrderBy(p => p.Stock).ThenBy(p => p.Nombre).ToListAsync();
            return Ok(lista.Select(ToItem));
        }

        // RF-02-08: historial de movimientos
        [HttpGet("movimientos")]
        public async Task<IActionResult> Movimientos([FromQuery] int? productoId)
        {
            var query = _context.MovimientosStock.AsQueryable();
            if (productoId.HasValue) query = query.Where(m => m.ProductoId == productoId.Value);

            var lista = await query.OrderByDescending(m => m.Fecha).ThenByDescending(m => m.Id)
                .Take(200).ToListAsync();

            return Ok(lista.Select(m => new
            {
                m.Id,
                m.ProductoNombre,
                m.Tipo,
                m.Cantidad,
                m.StockResultante,
                m.Motivo,
                m.Referencia,
                m.Fecha
            }));
        }

        // RF-02-05 y RF-02-07: ajuste manual, nunca deja el stock negativo
        [HttpPost("ajuste")]
        public async Task<IActionResult> Ajustar([FromBody] AjusteStockDto dto)
        {
            if (dto.NuevoStock < 0)
                return BadRequest(new { mensaje = "El stock no puede quedar negativo" });

            var motivo = (dto.Motivo ?? "").Trim();
            if (motivo.Length < 4)
                return BadRequest(new { mensaje = "Escribe el motivo del ajuste" });

            var p = await _context.Productos.FirstOrDefaultAsync(x => x.Id == dto.ProductoId && x.Estado);
            if (p == null) return NotFound(new { mensaje = "Producto no encontrado" });

            var diferencia = dto.NuevoStock - p.Stock;
            if (diferencia == 0)
                return BadRequest(new { mensaje = "El stock ya tiene ese valor" });

            p.Stock = dto.NuevoStock;
            _context.MovimientosStock.Add(MovimientoStock.Crear(p, "AJUSTE", diferencia, motivo,
                $"AJUSTE-{p.Id}", UsuarioId));
            await _context.SaveChangesAsync();

            return Ok(new { mensaje = "Existencias actualizadas" });
        }
    }
}