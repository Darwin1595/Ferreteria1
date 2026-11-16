using System.ComponentModel.DataAnnotations;

namespace Proinstalar.API.Models
{
    public class Categoria
    {
        public int Id { get; set; }
        
        [Required, MaxLength(100)]
        public string Nombre { get; set; } = string.Empty;
        
        [MaxLength(255)]
        public string Descripcion { get; set; } = string.Empty;
        
        [MaxLength(255)]
        public string ImagenUrl { get; set; } = string.Empty;

        public List<Producto> Productos { get; set; } = new();
    }
}
