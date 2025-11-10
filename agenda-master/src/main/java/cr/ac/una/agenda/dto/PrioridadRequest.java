package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para crear Prioridad
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioridadRequest {
    private String nivel; // "Alta", "Media", "Baja"
    private Integer peso;  // 3, 2, 1
}