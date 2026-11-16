using System.ComponentModel.DataAnnotations;

namespace Proinstalar.API.DTOs
{
    public record LoginDto([Required] string Email, [Required] string Password);

    public record RegisterDto(
        [Required, MaxLength(100)] string Nombre,
        [Required, EmailAddress, MaxLength(100)] string Email,
        [Required, MinLength(6)] string Password,
        [Required, MaxLength(20)] string Telefono,
        [Required, MaxLength(200)] string Direccion);

    public record AuthResponseDto(int Id, string Nombre, string Email, string Rol, string Token, string Telefono, string Direccion);
}
