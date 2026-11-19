package unam.fes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VentaResponseDTO {

    private Long idVenta;
    private String nombreVendedor;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private String metodoPago;
    private List<String> nombresProductos;

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public String getNombreVendedor() {
        return nombreVendedor;
    }

    public void setNombreVendedor(String nombreVendedor) {
        this.nombreVendedor = nombreVendedor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<String> getNombresProductos() {
        return nombresProductos;
    }

    public void setNombresProductos(List<String> nombresProductos) {
        this.nombresProductos = nombresProductos;
    }
}