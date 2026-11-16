using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Proinstalar.API.Models
{
    public class Pedido
    {
        public int Id { get; set; }

        public int UsuarioId { get; set; }

        [Column(TypeName = "decimal(12, 2)")]
        public decimal Total { get; set; }

        [MaxLength(30)]
        public string Estado { get; set; } = "PAGADO";

        [MaxLength(200)]
        public string Direccion { get; set; } = string.Empty;

        public DateTime FechaCreacion { get; set; } = DateTime.Now;

        public List<PedidoItem> Items { get; set; } = new();
    }

    public class PedidoItem
    {
        public int Id { get; set; }

        public int PedidoId { get; set; }

        public int ProductoId { get; set; }

        [MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;

        public int Cantidad { get; set; }

        [Column(TypeName = "decimal(10, 2)")]
        public decimal PrecioUnitario { get; set; }
    }
}
