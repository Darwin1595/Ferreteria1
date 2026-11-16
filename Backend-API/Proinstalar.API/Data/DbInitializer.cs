using Microsoft.EntityFrameworkCore;
using Proinstalar.API.Models;

namespace Proinstalar.API.Data
{
    public static class DbInitializer
    {
        public static void Initialize(AppDbContext context)
        {
            context.Database.EnsureCreated();
            AsegurarEsquemaInventario(context);

            if (!context.Categorias.Any())
            {
                var categorias = new Categoria[]
                {
                    new Categoria { Nombre = "Herramientas", Descripcion = "Herramientas manuales y eléctricas para instalación", ImagenUrl = "herramienta1.png" },
                    new Categoria { Nombre = "Pinturas", Descripcion = "Pinturas, brochas, rodillos y acabados", ImagenUrl = "pintura1.png" },
                    new Categoria { Nombre = "Gas y Plomería", Descripcion = "Tuberías, conexiones y repuestos de gas", ImagenUrl = "gas1.png" }
                };
                context.Categorias.AddRange(categorias);
                context.SaveChanges();
            }

            if (!context.TiposServicio.Any())
            {
                var servicios = new TipoServicio[]
                {
                    new TipoServicio { Nombre = "Instalación de Gas", Descripcion = "Mantenimiento e instalación certificada de redes de gas residencial", PrecioBase = 120000 },
                    new TipoServicio { Nombre = "Pintura General", Descripcion = "Servicio de pintura para interiores y exteriores", PrecioBase = 250000 },
                    new TipoServicio { Nombre = "Instalación Eléctrica", Descripcion = "Montaje de tableros, tomacorrientes y luminarias", PrecioBase = 95000 }
                };
                context.TiposServicio.AddRange(servicios);
                context.SaveChanges();
            }

            // Tablas nuevas (pedidos): si la BD ya existía, EnsureCreated no las crea, por eso se aseguran aquí.
            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `Pedidos` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `UsuarioId` int NOT NULL,
                `Total` decimal(12,2) NOT NULL,
                `Estado` varchar(30) NOT NULL,
                `Direccion` varchar(200) NOT NULL,
                `FechaCreacion` datetime(6) NOT NULL,
                PRIMARY KEY (`Id`)
            )");
            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `PedidoItems` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `PedidoId` int NOT NULL,
                `ProductoId` int NOT NULL,
                `Nombre` varchar(150) NOT NULL,
                `Cantidad` int NOT NULL,
                `PrecioUnitario` decimal(10,2) NOT NULL,
                PRIMARY KEY (`Id`)
            )");

            // Catálogo: se agregan los productos que falten (no duplica si ya existen).
            var catHerramientas = context.Categorias.First(c => c.Nombre == "Herramientas").Id;
            var catPinturas = context.Categorias.First(c => c.Nombre == "Pinturas").Id;
            var catGas = context.Categorias.First(c => c.Nombre == "Gas y Plomería").Id;

            var catalogo = new Producto[]
            {
                // Herramientas
                new Producto { Nombre = "Taladro Percutor 750W", Descripcion = "Taladro de alto rendimiento para concreto y madera", Precio = 245000, Stock = 15, CategoriaId = catHerramientas, ImagenUrl = "herramienta.png" },
                new Producto { Nombre = "Llave Ajustable 10-pulgadas", Descripcion = "Llave de acero cromo vanadio", Precio = 42000, Stock = 30, CategoriaId = catHerramientas, ImagenUrl = "herramienta2.png" },
                new Producto { Nombre = "Juego de Destornilladores x12", Descripcion = "Puntas planas y de estrella con mango antideslizante", Precio = 58000, Stock = 35, CategoriaId = catHerramientas, ImagenUrl = "herramienta1.png" },
                new Producto { Nombre = "Martillo de Uña 16oz", Descripcion = "Cabeza forjada y mango de fibra de vidrio", Precio = 36000, Stock = 40, CategoriaId = catHerramientas, ImagenUrl = "herramienta.png" },
                new Producto { Nombre = "Alicate Universal 8-pulgadas", Descripcion = "Alicate de acero con aislamiento para electricidad", Precio = 31000, Stock = 28, CategoriaId = catHerramientas, ImagenUrl = "herramienta2.png" },
                new Producto { Nombre = "Cinta Métrica 8m", Descripcion = "Flexómetro con freno y carcasa reforzada", Precio = 19500, Stock = 60, CategoriaId = catHerramientas, ImagenUrl = "herramienta1.png" },
                // Pinturas
                new Producto { Nombre = "Pintura Tipo 1 Lavable 5Gal", Descripcion = "Pintura acrílica blanca de alta cobertura", Precio = 185000, Stock = 20, CategoriaId = catPinturas, ImagenUrl = "pintura.png" },
                new Producto { Nombre = "Brocha de Cerda 3-pulgadas", Descripcion = "Brocha profesional para pintura epóxica y agua", Precio = 14500, Stock = 50, CategoriaId = catPinturas, ImagenUrl = "pintura2.png" },
                new Producto { Nombre = "Rodillo Profesional 9-pulgadas", Descripcion = "Rodillo de felpa para muros lisos y rugosos", Precio = 22000, Stock = 45, CategoriaId = catPinturas, ImagenUrl = "pintura1.png" },
                new Producto { Nombre = "Esmalte Sintético Negro 1Gal", Descripcion = "Esmalte brillante para metal y madera", Precio = 78000, Stock = 25, CategoriaId = catPinturas, ImagenUrl = "pintura.png" },
                new Producto { Nombre = "Cinta de Enmascarar 48mm", Descripcion = "Cinta para delimitar bordes sin dejar residuos", Precio = 8900, Stock = 80, CategoriaId = catPinturas, ImagenUrl = "pintura2.png" },
                new Producto { Nombre = "Sellador Acrílico Muros 1Gal", Descripcion = "Sellador base agua para preparar superficies", Precio = 64000, Stock = 22, CategoriaId = catPinturas, ImagenUrl = "pintura1.png" },
                // Gas y Plomería
                new Producto { Nombre = "Válvula de Seguridad Gas 1/2", Descripcion = "Válvula de corte rápido en bronce para gas natural", Precio = 38000, Stock = 40, CategoriaId = catGas, ImagenUrl = "gas.png" },
                new Producto { Nombre = "Manguera Flexible de Gas 1m", Descripcion = "Manguera de acero inoxidable blindada", Precio = 29000, Stock = 25, CategoriaId = catGas, ImagenUrl = "gas2.png" },
                new Producto { Nombre = "Regulador de Gas Doméstico", Descripcion = "Regulador de baja presión para cilindro doméstico", Precio = 45000, Stock = 30, CategoriaId = catGas, ImagenUrl = "gas1.png" },
                new Producto { Nombre = "Cinta Teflón Industrial", Descripcion = "Cinta selladora de roscas para tubería", Precio = 4500, Stock = 120, CategoriaId = catGas, ImagenUrl = "gas.png" },
                new Producto { Nombre = "Tubo CPVC 1/2 x 3m", Descripcion = "Tubería para agua caliente y fría", Precio = 26000, Stock = 50, CategoriaId = catGas, ImagenUrl = "gas2.png" },
                new Producto { Nombre = "Detector de Fugas de Gas", Descripcion = "Sensor con alarma sonora para cocina y calentador", Precio = 89000, Stock = 18, CategoriaId = catGas, ImagenUrl = "gas1.png" }
            };

            var existentes = context.Productos.Select(p => p.Nombre).ToHashSet();
            var nuevos = catalogo.Where(p => !existentes.Contains(p.Nombre)).ToList();
            if (nuevos.Count > 0)
            {
                context.Productos.AddRange(nuevos);
                context.SaveChanges();
            }

            // Cliente de prueba (solo si no hay usuarios)
            if (!context.Usuarios.Any())
            {
                context.Usuarios.Add(new Usuario
                {
                    Nombre = "Cliente Prueba",
                    Email = "cliente@gmail.com",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("Cliente123*"),
                    Telefono = "3109876543",
                    Direccion = "Carrera 7 # 45-10, Bogotá",
                    Rol = "CLIENTE"
                });
                context.SaveChanges();
            }

            // Único administrador del sistema
            const string adminEmail = "admin@proinstalar.com";
            const string adminPassword = "admin123";
            var admin = context.Usuarios.FirstOrDefault(u => u.Email == adminEmail);
            if (admin == null)
            {
                context.Usuarios.Add(new Usuario
                {
                    Nombre = "Administrador PROINSTALAR",
                    Email = adminEmail,
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword(adminPassword),
                    Telefono = "3001234567",
                    Direccion = "Bogotá",
                    Rol = "ADMIN"
                });
            }
            else
            {
                admin.Rol = "ADMIN";
                if (!BCrypt.Net.BCrypt.Verify(adminPassword, admin.PasswordHash))
                    admin.PasswordHash = BCrypt.Net.BCrypt.HashPassword(adminPassword);
            }

            // Cualquier otro administrador queda como cliente
            foreach (var otro in context.Usuarios.Where(u => u.Rol == "ADMIN" && u.Email != adminEmail).ToList())
                otro.Rol = "CLIENTE";

            context.SaveChanges();

            // Historial inicial de movimientos (una entrada por producto con existencias)
            if (!context.MovimientosStock.Any())
            {
                var adminId = context.Usuarios.First(u => u.Email == adminEmail).Id;
                foreach (var prod in context.Productos.Where(x => x.Estado && x.Stock > 0).ToList())
                    context.MovimientosStock.Add(MovimientoStock.Crear(prod, "ENTRADA", prod.Stock, "Inventario inicial", $"INICIAL-{prod.Id}", adminId));
                context.SaveChanges();
            }
        }

        // Proveedores, compras y movimientos de stock + columna StockMinimo.
        // Si la BD ya existía, EnsureCreated no crea lo nuevo; por eso se asegura aquí.
        private static void AsegurarEsquemaInventario(AppDbContext context)
        {
            try
            {
                context.Database.ExecuteSqlRaw("ALTER TABLE `Productos` ADD COLUMN `StockMinimo` int NOT NULL DEFAULT 10");
            }
            catch
            {
                // La columna ya existe: no hay nada que hacer.
            }

            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `Proveedores` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `Nombre` varchar(150) NOT NULL,
                `Nit` varchar(30) NOT NULL,
                `Telefono` varchar(30) NOT NULL,
                `Email` varchar(100) NOT NULL,
                `Direccion` varchar(200) NOT NULL,
                `Estado` tinyint(1) NOT NULL,
                PRIMARY KEY (`Id`)
            )");
            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `Compras` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `ProveedorId` int NOT NULL,
                `Total` decimal(12,2) NOT NULL,
                `Fecha` datetime(6) NOT NULL,
                `Observacion` varchar(200) NOT NULL,
                PRIMARY KEY (`Id`),
                CONSTRAINT `FK_Compras_Proveedores` FOREIGN KEY (`ProveedorId`) REFERENCES `Proveedores` (`Id`)
            )");
            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `DetallesCompra` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `CompraId` int NOT NULL,
                `ProductoId` int NOT NULL,
                `Nombre` varchar(150) NOT NULL,
                `Cantidad` int NOT NULL,
                `CostoUnitario` decimal(10,2) NOT NULL,
                PRIMARY KEY (`Id`),
                CONSTRAINT `FK_DetallesCompra_Compras` FOREIGN KEY (`CompraId`) REFERENCES `Compras` (`Id`) ON DELETE CASCADE
            )");
            context.Database.ExecuteSqlRaw(@"CREATE TABLE IF NOT EXISTS `MovimientosStock` (
                `Id` int NOT NULL AUTO_INCREMENT,
                `ProductoId` int NOT NULL,
                `ProductoNombre` varchar(150) NOT NULL,
                `Tipo` varchar(20) NOT NULL,
                `Cantidad` int NOT NULL,
                `StockResultante` int NOT NULL,
                `Motivo` varchar(200) NOT NULL,
                `Referencia` varchar(50) NOT NULL,
                `UsuarioId` int NOT NULL,
                `Fecha` datetime(6) NOT NULL,
                PRIMARY KEY (`Id`)
            )");

            if (!context.Proveedores.Any())
            {
                context.Proveedores.AddRange(
                    new Proveedor { Nombre = "Distribuidora Ferretera del Centro S.A.S.", Nit = "900123456-1", Telefono = "6014567890", Email = "ventas@ferrocentro.com", Direccion = "Calle 13 # 20-45, Bogotá" },
                    new Proveedor { Nombre = "Pinturas y Acabados Andina Ltda.", Nit = "800234567-2", Telefono = "6013456789", Email = "pedidos@andina.com", Direccion = "Av. Américas # 50-12, Bogotá" },
                    new Proveedor { Nombre = "Gas y Plomería Colombia S.A.", Nit = "901345678-3", Telefono = "6012345678", Email = "comercial@gasplomeria.com", Direccion = "Carrera 30 # 8-60, Bogotá" }
                );
                context.SaveChanges();
            }
        }
    }
}