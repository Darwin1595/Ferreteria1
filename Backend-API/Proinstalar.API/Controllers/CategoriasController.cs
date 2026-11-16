using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;

namespace Proinstalar.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CategoriasController : ControllerBase
    {
        private readonly AppDbContext _context;

        public CategoriasController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll()
        {
            var categorias = await _context.Categorias
                .Select(c => new
                {
                    c.Id,
                    c.Nombre,
                    c.Descripcion,
                    c.ImagenUrl,
                    CantidadProductos = c.Productos.Count(p => p.Estado)
                })
                .ToListAsync();

            return Ok(categorias);
        }
    }
}
