using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using Proinstalar.API.Data;
using Proinstalar.API.DTOs;
using Proinstalar.API.Models;
using Microsoft.AspNetCore.Authorization;
namespace Proinstalar.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly AppDbContext _context;
        private readonly IConfiguration _config;

        public AuthController(AppDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }

        [HttpPost("login")]
        public async Task<IActionResult> Login([FromBody] LoginDto dto)
        {
            var email = dto.Email.Trim().ToLower();
            var user = await _context.Usuarios.FirstOrDefaultAsync(u => u.Email == email);
            if (user == null || !BCrypt.Net.BCrypt.Verify(dto.Password, user.PasswordHash))
            {
                return Unauthorized(new { mensaje = "Credenciales incorrectas" });
            }

            var token = GenerarJwtToken(user);
            return Ok(new AuthResponseDto(user.Id, user.Nombre, user.Email, user.Rol, token, user.Telefono, user.Direccion));
        }

        [HttpPost("register")]
        public async Task<IActionResult> Register([FromBody] RegisterDto dto)
        {
            var email = dto.Email.Trim().ToLower();
            if (await _context.Usuarios.AnyAsync(u => u.Email == email))
            {
                return BadRequest(new { mensaje = "El correo ya está registrado" });
            }

            var user = new Usuario
            {
                Nombre = dto.Nombre.Trim(),
                Email = email,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password),
                Telefono = dto.Telefono,
                Direccion = dto.Direccion,
                Rol = "CLIENTE"
            };

            _context.Usuarios.Add(user);
            await _context.SaveChangesAsync();

            var token = GenerarJwtToken(user);
            return Ok(new AuthResponseDto(user.Id, user.Nombre, user.Email, user.Rol, token, user.Telefono, user.Direccion));
        }
        // Gestión de perfil: el usuario corrige su nombre, teléfono y dirección
        [HttpPut("perfil")]
        [Authorize]
        public async Task<IActionResult> ActualizarPerfil([FromBody] PerfilUpdateDto dto)
        {
            var nombre = (dto.Nombre ?? "").Trim();
            var telefono = (dto.Telefono ?? "").Trim();
            var direccion = (dto.Direccion ?? "").Trim();

            if (nombre.Length < 3 || nombre.Length > 100)
                return BadRequest(new { mensaje = "El nombre debe tener entre 3 y 100 caracteres" });
            if (telefono.Length < 7 || telefono.Length > 15 || !telefono.All(char.IsDigit))
                return BadRequest(new { mensaje = "El teléfono debe tener solo números (7 a 15 dígitos)" });
            if (direccion.Length < 5 || direccion.Length > 200)
                return BadRequest(new { mensaje = "La dirección no es válida" });

            var id = int.Parse(User.FindFirstValue(ClaimTypes.NameIdentifier)!);
            var user = await _context.Usuarios.FindAsync(id);
            if (user == null) return Unauthorized(new { mensaje = "Usuario no válido, inicia sesión de nuevo" });

            user.Nombre = nombre;
            user.Telefono = telefono;
            user.Direccion = direccion;
            await _context.SaveChangesAsync();

            var token = GenerarJwtToken(user);
            return Ok(new AuthResponseDto(user.Id, user.Nombre, user.Email, user.Rol, token, user.Telefono, user.Direccion));
        }
        private string GenerarJwtToken(Usuario user)
        {
            var jwtKey = _config["Jwt:Key"] ?? "ClaveSecretaSuperSeguraParaProinstalarFerreteria2026!";
            var tokenHandler = new JwtSecurityTokenHandler();
            var key = Encoding.UTF8.GetBytes(jwtKey);

            var tokenDescriptor = new SecurityTokenDescriptor
            {
                Subject = new ClaimsIdentity(new[]
                {
                    new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                    new Claim(ClaimTypes.Name, user.Nombre),
                    new Claim(ClaimTypes.Email, user.Email),
                    new Claim(ClaimTypes.Role, user.Rol)
                }),
                Expires = DateTime.UtcNow.AddDays(7),
                SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key), SecurityAlgorithms.HmacSha256Signature)
            };

            var token = tokenHandler.CreateToken(tokenDescriptor);
            return tokenHandler.WriteToken(token);
        }
    }
}
