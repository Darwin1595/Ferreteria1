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
    public class ProductosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ProductosController(AppDbContext context)
        {
            _context = context;
        }

        private int UsuarioId => int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);

        private static ProductoResponseDto ToDto(Producto p, string categoria) =>
            new ProductoResponseDto(p.Id, p.Nombre, p.Descripcion, p.Precio, p.Stock, p.StockMinimo,
                p.CategoriaId, categoria, p.ImagenUrl, p.Estado);

        // Validaciones del servidor (se repiten aunque la app ya valide)
        private async Task<string?> Validar(ProductoCreateDto dto, int? idActual)
        {
            var nombre = (dto.Nombre ?? "").Trim();
            if (nombre.Length < 3 || nombre.Length > 150) return "El nombre debe tener entre 3 y 150 caracteres";
            if (dto.Precio <= 0) return "El precio debe ser mayor a 0";
            if (dto.Stock < 0) return "Las existencias no pueden ser negativas";
            if (dto.StockMinimo < 0) return "El stock mínimo no puede ser negativo";
            if (!await _context.Categorias.AnyAsync(c => c.Id == dto.CategoriaId)) return "La categoría no existe";
            var idIgnorar = idActual ?? 0;
            if (await _context.Productos.AnyAsync(p => p.Estado && p.Nombre == nombre && p.Id != idIgnorar))
                return "Ya existe un producto con ese nombre";
            return null;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll([FromQuery] int? categoriaId, [FromQuery] string? q)
        {
            var query = _context.Productos.Include(p => p.Categoria).Where(p => p.Estado);

            if (categoriaId.HasValue && categoriaId.Value > 0)
            {
                query = query.Where(p => p.CategoriaId == categoriaId.Value);
            }

            if (!string.IsNullOrWhiteSpace(q))
            {
                query = query.Where(p => p.Nombre.Contains(q) || p.Descripcion.Contains(q));
            }

            var productos = await query.Select(p => new ProductoResponseDto(
                p.Id, p.Nombre, p.Descripcion, p.Precio, p.Stock, p.StockMinimo, p.CategoriaId,
                p.Categoria!.Nombre, p.ImagenUrl, p.Estado
            )).ToListAsync();

            return Ok(productos);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var p = await _context.Productos.Include(x => x.Categoria).FirstOrDefaultAsync(x => x.Id == id && x.Estado);
            if (p == null) return NotFound(new { mensaje = "Producto no encontrado" });

            return Ok(ToDto(p, p.Categoria?.Nombre ?? ""));
        }

        // RF-02-01 y RF-02-02: registrar herramientas y materiales, con su categoría
        [HttpPost]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> Create([FromBody] ProductoCreateDto dto)
        {
            var error = await Validar(dto, null);
            if (error != null) return BadRequest(new { mensaje = error });

            var producto = new Producto
            {
                Nombre = dto.Nombre.Trim(),
                Descripcion = (dto.Descripcion ?? "").Trim(),
                Precio = dto.Precio,
                Stock = dto.Stock,
                StockMinimo = dto.StockMinimo,
                CategoriaId = dto.CategoriaId,
                ImagenUrl = dto.ImagenUrl ?? ""
            };

            await using var tx = await _context.Database.BeginTransactionAsync();
            _context.Productos.Add(producto);
            await _context.SaveChangesAsync();

            if (producto.Stock > 0)
            {
                _context.MovimientosStock.Add(MovimientoStock.Crear(producto, "ENTRADA", producto.Stock,
                    "Registro inicial del producto", $"PROD-{producto.Id}", UsuarioId));
                await _context.SaveChangesAsync();
            }
            await tx.CommitAsync();

            var categoria = await _context.Categorias.FindAsync(producto.CategoriaId);
            return CreatedAtAction(nameof(GetById), new { id = producto.Id }, ToDto(producto, categoria?.Nombre ?? ""));
        }

        [HttpPut("{id}")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> Update(int id, [FromBody] ProductoCreateDto dto)
        {
            var p = await _context.Productos.FindAsync(id);
            if (p == null) return NotFound(new { mensaje = "Producto no encontrado" });

            var error = await Validar(dto, id);
            if (error != null) return BadRequest(new { mensaje = error });

            var stockAnterior = p.Stock;
            p.Nombre = dto.Nombre.Trim();
            p.Descripcion = (dto.Descripcion ?? "").Trim();
            p.Precio = dto.Precio;
            p.Stock = dto.Stock;
            p.StockMinimo = dto.StockMinimo;
            p.CategoriaId = dto.CategoriaId;
            p.ImagenUrl = dto.ImagenUrl ?? "";

            if (p.Stock != stockAnterior)
            {
                _context.MovimientosStock.Add(MovimientoStock.Crear(p, "AJUSTE", p.Stock - stockAnterior,
                    "Cambio desde la edición del producto", $"PROD-{p.Id}", UsuarioId));
            }

            await _context.SaveChangesAsync();
            var categoria = await _context.Categorias.FindAsync(p.CategoriaId);
            return Ok(ToDto(p, categoria?.Nombre ?? ""));
        }

        [HttpDelete("{id}")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> Delete(int id)
        {
            var p = await _context.Productos.FindAsync(id);
            if (p == null) return NotFound(new { mensaje = "Producto no encontrado" });

            p.Estado = false; // Desactivar lógicamente: el historial se conserva
            await _context.SaveChangesAsync();
            return Ok(new { mensaje = "Producto deshabilitado correctamente" });
        }
    }
}