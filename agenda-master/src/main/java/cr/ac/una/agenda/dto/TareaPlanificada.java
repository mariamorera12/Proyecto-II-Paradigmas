package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaPlanificada {
    private String tarea;
    private String horaInicio;
}