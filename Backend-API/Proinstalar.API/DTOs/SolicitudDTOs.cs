namespace Proinstalar.API.DTOs
{
    // Formulario de la app: el cliente se toma del token, solo envía servicio, causa y datos de contacto.
    public record SolicitudCreateDto(int TipoServicioId, string Causa, string? Telefono, string? Direccion);
    public record SolicitudStatusDto(string Estado);
    public record SolicitudResponseDto(int Id, string Estado, string Mensaje);
        public record SolicitudGestionDto(string Estado, decimal? Total);
}
