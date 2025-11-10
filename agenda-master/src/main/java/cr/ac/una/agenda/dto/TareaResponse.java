package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaResponse {
    private Long id;
    private String nombre;
    private String prioridad;
    private Integer tiempoEstimado;
    private String clima;
    private String tareaDependencia;
    private String fecha;
    private LocalTime horaInicio;
    private Boolean completada;
}