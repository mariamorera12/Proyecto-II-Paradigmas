package cr.ac.una.agenda.service;

import cr.ac.una.agenda.dto.PlanificacionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class AgendaService {
    private final WebClient client;

    public AgendaService(WebClient.Builder builder) {
        // Nota: "prologapi" es el NOMBRE de la app registrada en Eureka
        this.client = builder.baseUrl("http://prologapi").build();
    }

    // Método original de prueba
    public Integer sum(int a, int b) {
        return client.get()
                .uri(uri -> uri.path("/api/sum").queryParam("a", a).queryParam("b", b).build())
                .retrieve()
                .bodyToMono(Integer.class)
                .block(); // bloqueante (simple)
    }

    // NUEVO: Método para planificar tareas con Prolog
    public PlanificacionResponse planificarTareas(Map<String, Object> request) {
        try {
            log.info("Enviando request a Prolog API para planificar tareas");
            log.info("Request body: {}", request);

            PlanificacionResponse response = client.post()
                    .uri("/api/prolog/planificar")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PlanificacionResponse.class)
                    .block();

            log.info("Respuesta recibida de Prolog API: {}", response);

            return response;

        } catch (Exception e) {
            log.error("Error al comunicarse con Prolog API: {}", e.getMessage(), e);

            // Devolver respuesta de error
            PlanificacionResponse errorResponse = new PlanificacionResponse();
            errorResponse.setViable(false);
            errorResponse.setMensaje("Error al comunicarse con el servicio de planificación: " + e.getMessage());

            return errorResponse;
        }
    }
}