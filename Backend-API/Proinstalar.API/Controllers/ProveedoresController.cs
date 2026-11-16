using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Data;
using Proinstalar.API.DTOs;
using Proinstalar.API.Models;

namespace Proinstalar.API.Controllers
{
    // RF-02-03: administrar proveedores (solo ADMIN)
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "ADMIN")]
    public class ProveedoresController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ProveedoresController(AppDbContext context)
        {
            _context = context;
        }

        private static object ToDto(Proveedor p) =>
            new { p.Id, p.Nombre, p.Nit, p.Telefono, p.Email, p.Direccion, p.Estado };

        private async Task<string?> Validar(ProveedorDto dto, int? idActual)
        {
            var nombre = (dto.Nombre ?? "").Trim();
            if (nombre.Length < 3 || nombre.Length > 150) return "El nombre debe tener entre 3 y 150 caracteres";

            var nit = (dto.Nit ?? "").Trim();
            if (nit.Length < 5 || nit.Length > 30) return "El NIT debe tener entre 5 y 30 caracteres";

            var tel = (dto.Telefono ?? "").Trim();
            if (tel.Length < 7 || tel.Length > 30) return "El teléfono no es válido";

            var email = (dto.Email ?? "").Trim();
            if (email.Length > 0 && !new EmailAddressAttribute().IsValid(email)) return "El correo no es válido";

            var idIgnorar = idActual ?? 0;
            if (await _context.Proveedores.AnyAsync(p => p.Estado && p.Nit == nit && p.Id != idIgnorar))
                return "Ya existe un proveedor con ese NIT";

            return null;
        }

        [HttpGet]
        public async Task<IActionResult> GetAll([FromQuery] string? q)
        {
            var query = _context.Proveedores.Where(p => p.Estado);
            if (!string.IsNullOrWhiteSpace(q))
                query = query.Where(p => p.Nombre.Contains(q) || p.Nit.Contains(q));

            var lista = await query.OrderBy(p => p.Nombre).ToListAsync();
            return Ok(lista.Select(ToDto));
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(int id)
        {
            var p = await _context.Proveedores.FirstOrDefaultAsync(x => x.Id == id && x.Estado);
            if (p == null) return NotFound(new { mensaje = "Proveedor no encontrado" });
            return Ok(ToDto(p));
        }

        [HttpPost]
        public async Task<IActionResult> Create([FromBody] ProveedorDto dto)
        {
            var error = await Validar(dto, null);
            if (error != null) return BadRequest(new { mensaje = error });

            var p = new Proveedor
            {
                Nombre = dto.Nombre.Trim(),
                Nit = (dto.Nit ?? "").Trim(),
                Telefono = (dto.Telefono ?? "").Trim(),
                Email = (dto.Email ?? "").Trim(),
                Direccion = (dto.Direccion ?? "").Trim()
            };
            _context.Proveedores.Add(p);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetById), new { id = p.Id }, ToDto(p));
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> Update(int id, [FromBody] ProveedorDto dto)
        {
            var p = await _context.Proveedores.FirstOrDefaultAsync(x => x.Id == id && x.Estado);
            if (p == null) return NotFound(new { mensaje = "Proveedor no encontrado" });

            var error = await Validar(dto, id);
            if (error != null) return BadRequest(new { mensaje = error });

            p.Nombre = dto.Nombre.Trim();
            p.Nit = (dto.Nit ?? "").Trim();
            p.Telefono = (dto.Telefono ?? "").Trim();
            p.Email = (dto.Email ?? "").Trim();
            p.Direccion = (dto.Direccion ?? "").Trim();
            await _context.SaveChangesAsync();
            return Ok(ToDto(p));
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var p = await _context.Proveedores.FirstOrDefaultAsync(x => x.Id == id && x.Estado);
            if (p == null) return NotFound(new { mensaje = "Proveedor no encontrado" });

            p.Estado = false; // las compras anteriores se conservan
            await _context.SaveChangesAsync();
            return Ok(new { mensaje = "Proveedor eliminado" });
        }
    }
}