namespace Proinstalar.API.DTOs
{
    public record ProveedorDto(string Nombre, string? Nit, string? Telefono, string? Email, string? Direccion);

    public record CompraItemDto(int ProductoId, int Cantidad, decimal CostoUnitario);
    public record CompraCreateDto(int ProveedorId, string? Observacion, List<CompraItemDto> Items);

    public record AjusteStockDto(int ProductoId, int NuevoStock, string? Motivo);

    public record PerfilUpdateDto(string Nombre, string Telefono, string Direccion);
}