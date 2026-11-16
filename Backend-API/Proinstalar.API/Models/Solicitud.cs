using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Proinstalar.API.Models
{
    public class Solicitud
    {
        public int Id { get; set; }
        
        [Required, MaxLength(100)]
        public string ClienteNombre { get; set; } = string.Empty;
        
        [Required, MaxLength(100)]
        public string ClienteEmail { get; set; } = string.Empty;
        
        [Required, MaxLength(20)]
        public string ClienteTelefono { get; set; } = string.Empty;
        
        [Required, MaxLength(200)]
        public string ClienteDireccion { get; set; } = string.Empty;
        
        public int TipoServicioId { get; set; }
        public TipoServicio? TipoServicio { get; set; }
        
        [Column(TypeName = "decimal(10, 2)")]
        public decimal Total { get; set; }
        
        [MaxLength(30)]
        public string Estado { get; set; } = "PENDIENTE"; // PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO
        
        public string Notas { get; set; } = string.Empty;
        
        public DateTime FechaCreacion { get; set; } = DateTime.Now;
    }
}
