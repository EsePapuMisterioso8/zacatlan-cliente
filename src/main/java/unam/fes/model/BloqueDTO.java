package unam.fes.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BloqueDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
