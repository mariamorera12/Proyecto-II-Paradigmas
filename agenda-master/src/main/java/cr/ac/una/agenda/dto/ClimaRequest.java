package cr.ac.una.agenda.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para crear Clima
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClimaRequest {
    private String tipo; // "soleado", "lluvioso", etc.
}