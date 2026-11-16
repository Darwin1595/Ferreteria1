using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;

namespace Proinstalar.API.Controllers
{
    // RF-02-10: reportes (solo ADMIN)
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "ADMIN")]
    public class ReportesController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ReportesController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet("stock")]
        public async Task<IActionResult> Stock()
        {
            var productos = await _context.Productos.Include(p => p.Categoria)
                .Where(p => p.Estado).ToListAsync();

            var porCategoria = productos
                .GroupBy(p => p.Categoria != null ? p.Categoria.Nombre : "Sin categoría")
                .Select(g => new
                {
                    Categoria = g.Key,
                    Productos = g.Count(),
                    Unidades = g.Sum(p => p.Stock),
                    Valor = g.Sum(p => p.Precio * p.Stock)
                })
                .OrderBy(x => x.Categoria)
                .ToList();

            return Ok(new
            {
                TotalProductos = productos.Count,
                UnidadesTotales = productos.Sum(p => p.Stock),
                ValorInventario = productos.Sum(p => p.Precio * p.Stock),
                ProductosBajoMinimo = productos.Count(p => p.Stock > 0 && p.Stock <= p.StockMinimo),
                ProductosAgotados = productos.Count(p => p.Stock <= 0),
                PorCategoria = porCategoria
            });
        }

        [HttpGet("resumen")]
        public async Task<IActionResult> Resumen()
        {
            var pedidos = await _context.Pedidos.ToListAsync();
            var compras = await _context.Compras.ToListAsync();
            var items = await _context.PedidoItems.ToListAsync();

            var masVendidos = items
                .GroupBy(i => i.Nombre)
                .Select(g => new
                {
                    Nombre = g.Key,
                    Unidades = g.Sum(i => i.Cantidad),
                    Total = g.Sum(i => i.PrecioUnitario * i.Cantidad)
                })
                .OrderByDescending(x => x.Unidades)
                .Take(5)
                .ToList();

            return Ok(new
            {
                VentasTotal = pedidos.Sum(p => p.Total),
                VentasCantidad = pedidos.Count,
                ComprasTotal = compras.Sum(c => c.Total),
                ComprasCantidad = compras.Count,
                MasVendidos = masVendidos
            });
        }
    }
}