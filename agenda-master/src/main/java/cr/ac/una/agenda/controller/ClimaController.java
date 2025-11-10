package cr.ac.una.agenda.controller;

import cr.ac.una.agenda.dto.ClimaRequest;
import cr.ac.una.agenda.entity.Clima;
import cr.ac.una.agenda.service.ClimaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/climas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClimaController {

    private final ClimaService climaService;

    @GetMapping
    public ResponseEntity<List<Clima>> obtenerClimas() {
        return ResponseEntity.ok(climaService.obtenerClimas());
    }

    @PostMapping
    public ResponseEntity<Clima> crearClima(@RequestBody ClimaRequest request) {
        return ResponseEntity.ok(climaService.crearClima(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Clima> actualizarClima(
            @PathVariable Long id,
            @RequestBody ClimaRequest request) {
        return ResponseEntity.ok(climaService.actualizarClima(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarClima(@PathVariable Long id) {
        climaService.eliminarClima(id);
        return ResponseEntity.noContent().build();
    }
}