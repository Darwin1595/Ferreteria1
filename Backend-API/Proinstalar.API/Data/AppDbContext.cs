using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Models;

namespace Proinstalar.API.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Usuario> Usuarios => Set<Usuario>();
        public DbSet<Categoria> Categorias => Set<Categoria>();
        public DbSet<Producto> Productos => Set<Producto>();
        public DbSet<TipoServicio> TiposServicio => Set<TipoServicio>();
        public DbSet<Solicitud> Solicitudes => Set<Solicitud>();
        public DbSet<Pedido> Pedidos => Set<Pedido>();
        public DbSet<PedidoItem> PedidoItems => Set<PedidoItem>();
        public DbSet<Proveedor> Proveedores => Set<Proveedor>();
        public DbSet<Compra> Compras => Set<Compra>();
        public DbSet<DetalleCompra> DetallesCompra => Set<DetalleCompra>();
        public DbSet<MovimientoStock> MovimientosStock => Set<MovimientoStock>();

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            
            // Relaciones y configuraciones adicionales
            modelBuilder.Entity<Producto>()
                .HasOne(p => p.Categoria)
                .WithMany(c => c.Productos)
                .HasForeignKey(p => p.CategoriaId);

            modelBuilder.Entity<PedidoItem>()
                .HasOne<Pedido>()
                .WithMany(p => p.Items)
                .HasForeignKey(i => i.PedidoId);
        }
    }
}
