package unam.fes.model;

import java.math.BigDecimal;

public class InventarioVendedorDTO {

    private Long idProductoVendedor;
    private String nombreProducto;
    private String descripcionProducto;
    private BigDecimal precio;
    private Boolean activo;

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

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}