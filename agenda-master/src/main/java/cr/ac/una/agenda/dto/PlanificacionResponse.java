package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanificacionResponse {
    private List<TareaPlanificada> plan;
    private String mensaje;
    private Boolean viable;
}