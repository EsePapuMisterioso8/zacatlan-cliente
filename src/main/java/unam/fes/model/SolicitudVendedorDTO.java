
package unam.fes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudVendedorDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String telefono;
    private String estadoSolicitud;
    private LocalDateTime fechaSolicitud;
    private BloqueDTO bloque;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BloqueDTO {

        private Long id;
        private String nombre;
    }
}
