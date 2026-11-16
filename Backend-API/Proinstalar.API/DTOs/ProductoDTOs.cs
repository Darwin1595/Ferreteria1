namespace Proinstalar.API.DTOs
{
    public record ProductoCreateDto(string Nombre, string? Descripcion, decimal Precio, int Stock, int StockMinimo, int CategoriaId, string? ImagenUrl);
    public record ProductoResponseDto(int Id, string Nombre, string Descripcion, decimal Precio, int Stock, int StockMinimo, int CategoriaId, string CategoriaNombre, string ImagenUrl, bool Estado);
}