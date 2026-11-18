
package unam.fes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SolicitudPrecioClienteDTO {

    private Long idSolicitud;
    private Long idProductoVendedor;
    private String nombreProducto;
    private String nombreVendedor;
    private BigDecimal precioActual;
    private BigDecimal precioSolicitado;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private String observacionAdmin;

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public Long getIdProductoVendedor() {
        return idProductoVendedor;
    }

    public void setIdProductoVendedor(Long idProductoVendedor) {
        this.idProductoVendedor = idProductoVendedor;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public BigDecimal getPrecioActual() {
        return precioActual;
    }

    public void setPrecioActual(BigDecimal precioActual) {
        this.precioActual = precioActual;
    }

    public BigDecimal getPrecioSolicitado() {
        return precioSolicitado;
    }

    public void setPrecioSolicitado(BigDecimal precioSolicitado) {
        this.precioSolicitado = precioSolicitado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getObservacionAdmin() {
        return observacionAdmin;
    }

    public void setObservacionAdmin(String observacionAdmin) {
        this.observacionAdmin = observacionAdmin;
    }
}
