using System.ComponentModel.DataAnnotations;

namespace Proinstalar.API.Models
{
    public class Usuario
    {
        public int Id { get; set; }
        
        [Required, MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;
        
        [Required, MaxLength(100), EmailAddress]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string PasswordHash { get; set; } = string.Empty;
        
        [MaxLength(20)]
        public string Telefono { get; set; } = string.Empty;

        [MaxLength(200)]
        public string Direccion { get; set; } = string.Empty;
        
        [Required, MaxLength(20)]
        public string Rol { get; set; } = "CLIENTE"; // CLIENTE o ADMIN
        
        public DateTime FechaRegistro { get; set; } = DateTime.Now;
    }
}
