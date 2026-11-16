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
    [Authorize]
    public class SolicitudesController : ControllerBase
    {
        private readonly AppDbContext _context;

        public SolicitudesController(AppDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> GetAll()
        {
            var solicitudes = await _context.Solicitudes
                .Include(s => s.TipoServicio)
                .OrderByDescending(s => s.FechaCreacion)
                .ToListAsync();

            return Ok(solicitudes);
        }

        // Solicitudes del usuario autenticado
        [HttpGet("mias")]
        public async Task<IActionResult> Mias()
        {
            var email = User.FindFirstValue(ClaimTypes.Email);
            var lista = await _context.Solicitudes
                .Include(s => s.TipoServicio)
                .Where(s => s.ClienteEmail == email)
                .OrderByDescending(s => s.FechaCreacion)
                .ToListAsync();
            return Ok(lista);
        }

        [HttpGet("{id}")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> GetById(int id)
        {
            var s = await _context.Solicitudes.Include(x => x.TipoServicio).FirstOrDefaultAsync(x => x.Id == id);
            if (s == null) return NotFound();
            return Ok(s);
        }

        // Mini formulario: servicio + causa. El cliente sale del token.
        [HttpPost]
        public async Task<IActionResult> Create([FromBody] SolicitudCreateDto dto)
        {
            if (string.IsNullOrWhiteSpace(dto.Causa))
                return BadRequest(new { mensaje = "Cuéntanos la causa o motivo de la solicitud" });

            var servicio = await _context.TiposServicio.FindAsync(dto.TipoServicioId);
            if (servicio == null)
                return BadRequest(new { mensaje = "El servicio seleccionado no existe" });

            var uid = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
            var usuario = await _context.Usuarios.FindAsync(uid);
            if (usuario == null)
                return Unauthorized(new { mensaje = "Usuario no válido, inicia sesión de nuevo" });

            var solicitud = new Solicitud
            {
                ClienteNombre = usuario.Nombre,
                ClienteEmail = usuario.Email,
                ClienteTelefono = string.IsNullOrWhiteSpace(dto.Telefono) ? usuario.Telefono : dto.Telefono.Trim(),
                ClienteDireccion = string.IsNullOrWhiteSpace(dto.Direccion) ? usuario.Direccion : dto.Direccion.Trim(),
                TipoServicioId = servicio.Id,
                Total = servicio.PrecioBase,
                Notas = dto.Causa.Trim(),
                Estado = "PENDIENTE",
                FechaCreacion = DateTime.Now
            };

            _context.Solicitudes.Add(solicitud);
            await _context.SaveChangesAsync();

            return Ok(new SolicitudResponseDto(solicitud.Id, solicitud.Estado,
                "¡Solicitud recibida! Un técnico se contactará contigo en breve."));
        }
        // Admin: cambia el estado y el valor de una solicitud
        [HttpPut("{id}/gestion")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> Gestionar(int id, [FromBody] SolicitudGestionDto dto)
        {
            var permitidos = new[] { "PENDIENTE", "EN_REVISION", "TERMINADO" };
            var estado = (dto.Estado ?? "").Trim().ToUpper();
            if (!permitidos.Contains(estado))
                return BadRequest(new { mensaje = "Estado no válido" });
            if (dto.Total.HasValue && dto.Total.Value < 0)
                return BadRequest(new { mensaje = "El valor no puede ser negativo" });

            var s = await _context.Solicitudes.Include(x => x.TipoServicio).FirstOrDefaultAsync(x => x.Id == id);
            if (s == null) return NotFound(new { mensaje = "Solicitud no encontrada" });

            s.Estado = estado;
            if (dto.Total.HasValue) s.Total = dto.Total.Value;
            await _context.SaveChangesAsync();
            return Ok(s);
        }
        
        [HttpPatch("{id}/estado")]
        [Authorize(Roles = "ADMIN")]
        public async Task<IActionResult> UpdateEstado(int id, [FromBody] SolicitudStatusDto dto)
        {
            var s = await _context.Solicitudes.FindAsync(id);
            if (s == null) return NotFound();

            s.Estado = dto.Estado;
            await _context.SaveChangesAsync();

            return Ok(s);
        }
    }
}
