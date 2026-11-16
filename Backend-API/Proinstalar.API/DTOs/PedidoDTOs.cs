namespace Proinstalar.API.DTOs
{
    public record PedidoItemDto(int ProductoId, int Cantidad);
    public record PedidoCreateDto(List<PedidoItemDto> Items, string? Direccion);
    public record PedidoResponseDto(int Id, decimal Total, string Estado, DateTime FechaCreacion, string Mensaje);
        public record PedidoEstadoDto(string Estado);
}
