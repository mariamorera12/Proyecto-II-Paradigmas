package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaRequest {
    private String nombre;
    private String prioridad; // "Alta", "Media", "Baja"
    private Integer tiempoEstimado;
    private String clima; // "soleado", "lluvioso", "nublado", "cualquiera"
    private Long tareaDependenciaId;
    private LocalDate fecha;
}