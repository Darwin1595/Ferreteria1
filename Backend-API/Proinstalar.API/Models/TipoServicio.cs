using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Proinstalar.API.Models
{
    public class TipoServicio
    {
        public int Id { get; set; }
        
        [Required, MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;
        
        public string Descripcion { get; set; } = string.Empty;
        
        [Column(TypeName = "decimal(10, 2)")]
        public decimal PrecioBase { get; set; }
    }
}
