package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanificacionRequest {
    private Integer tiempoDisponible; // en horas
    private String climaActual;
    private String fecha; // formato "yyyy-MM-dd"
}