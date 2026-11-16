using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Proinstalar.API.Models
{
    public class Producto
    {
        public int Id { get; set; }
        
        [Required, MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;
        
        public string Descripcion { get; set; } = string.Empty;
        
        [Column(TypeName = "decimal(10, 2)")]
        public decimal Precio { get; set; }
        
        public int Stock { get; set; }

        public int StockMinimo { get; set; } = 10;
        
        public int CategoriaId { get; set; }
        public Categoria? Categoria { get; set; }
        
        [MaxLength(255)]
        public string ImagenUrl { get; set; } = string.Empty;

        public bool Estado { get; set; } = true;
    }
}
