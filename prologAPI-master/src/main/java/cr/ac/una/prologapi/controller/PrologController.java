package cr.ac.una.prologapi.controller;

import cr.ac.una.prologapi.dto.PlanificacionRequest;
import cr.ac.una.prologapi.dto.PlanificacionResponse;
import cr.ac.una.prologapi.service.PrologService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prolog")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class PrologController {

    private final PrologService prologService;

    @PostMapping("/planificar")
    public ResponseEntity<PlanificacionResponse> planificar(@RequestBody PlanificacionRequest request) {
        log.info("Recibida petición de planificación");
        log.info("Tiempo disponible: {} horas", request.getTiempoDisponible());
        log.info("Clima: {}", request.getClimaActual());
        log.info("Tareas recibidas: {}", request.getTareas().size());

        PlanificacionResponse response = prologService.planificar(request);

        log.info("Planificación completada. Viable: {}", response.getViable());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Prolog API Service is running");
    }

    // Endpoint de prueba (el que ya tenías)
    @GetMapping("/sum")
    public ResponseEntity<Integer> sum(@RequestParam int a, @RequestParam int b) {
        return ResponseEntity.ok(a + b);
    }
}