using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Proinstalar.API.Models
{
    public class Proveedor
    {
        public int Id { get; set; }

        [Required, MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;

        [MaxLength(30)]
        public string Nit { get; set; } = string.Empty;

        [MaxLength(30)]
        public string Telefono { get; set; } = string.Empty;

        [MaxLength(100)]
        public string Email { get; set; } = string.Empty;

        [MaxLength(200)]
        public string Direccion { get; set; } = string.Empty;

        public bool Estado { get; set; } = true;

        public List<Compra> Compras { get; set; } = new();
    }

    public class Compra
    {
        public int Id { get; set; }

        public int ProveedorId { get; set; }
        public Proveedor? Proveedor { get; set; }

        [Column(TypeName = "decimal(12, 2)")]
        public decimal Total { get; set; }

        public DateTime Fecha { get; set; } = DateTime.Now;

        [MaxLength(200)]
        public string Observacion { get; set; } = string.Empty;

        public List<DetalleCompra> Detalles { get; set; } = new();
    }

    public class DetalleCompra
    {
        public int Id { get; set; }

        public int CompraId { get; set; }

        public int ProductoId { get; set; }

        [MaxLength(150)]
        public string Nombre { get; set; } = string.Empty;

        public int Cantidad { get; set; }

        [Column(TypeName = "decimal(10, 2)")]
        public decimal CostoUnitario { get; set; }
    }

    /// <summary>Cada entrada, salida o ajuste de existencias queda registrado aquí (trazabilidad).</summary>
    public class MovimientoStock
    {
        public int Id { get; set; }

        public int ProductoId { get; set; }

        [MaxLength(150)]
        public string ProductoNombre { get; set; } = string.Empty;

        [MaxLength(20)]
        public string Tipo { get; set; } = "AJUSTE"; // ENTRADA, SALIDA o AJUSTE

        public int Cantidad { get; set; } // positivo = entra, negativo = sale

        public int StockResultante { get; set; }

        [MaxLength(200)]
        public string Motivo { get; set; } = string.Empty;

        [MaxLength(50)]
        public string Referencia { get; set; } = string.Empty;

        public int UsuarioId { get; set; }

        public DateTime Fecha { get; set; } = DateTime.Now;

        public static MovimientoStock Crear(Producto p, string tipo, int cantidad, string motivo, string referencia, int usuarioId)
        {
            return new MovimientoStock
            {
                ProductoId = p.Id,
                ProductoNombre = p.Nombre,
                Tipo = tipo,
                Cantidad = cantidad,
                StockResultante = p.Stock,
                Motivo = motivo,
                Referencia = referencia,
                UsuarioId = usuarioId,
                Fecha = DateTime.Now
            };
        }
    }
}