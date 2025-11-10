package cr.ac.una.prologapi.service;

import cr.ac.una.prologapi.dto.PlanificacionRequest;
import cr.ac.una.prologapi.dto.PlanificacionResponse;
import cr.ac.una.prologapi.dto.TareaPlanificada;
import lombok.extern.slf4j.Slf4j;
import org.jpl7.*;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PrologService {

    // Ruta donde Docker copia el archivo operaciones.pl
    private static final String PROLOG_FILE = "/opt/app/prolog/operaciones.pl";

    @PostConstruct
    public void init() {
        try {
            log.info("Inicializando servicio Prolog...");
            log.info("SWI_HOME_DIR: {}", System.getenv("SWI_HOME_DIR"));
            log.info("LD_LIBRARY_PATH: {}", System.getenv("LD_LIBRARY_PATH"));
            log.info("java.library.path: {}", System.getProperty("java.library.path"));

            // Consultar el archivo Prolog
            String query = String.format("consult('%s')", PROLOG_FILE);
            Query consultQuery = new Query(query);

            if (consultQuery.hasSolution()) {
                log.info("✓ Archivo Prolog '{}' cargado exitosamente", PROLOG_FILE);
            } else {
                log.error("✗ No se pudo cargar el archivo Prolog '{}'", PROLOG_FILE);
            }
        } catch (Exception e) {
            log.error("✗ Error al inicializar Prolog: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo inicializar el motor Prolog", e);
        }
    }

    public PlanificacionResponse planificar(PlanificacionRequest request) {
        try {
            log.info("=== Iniciando planificación con Prolog ===");
            log.info("Tiempo disponible: {} horas", request.getTiempoDisponible());
            log.info("Clima actual: {}", request.getClimaActual());
            log.info("Número de tareas: {}", request.getTareas().size());

            // Limpiar base de conocimiento
            limpiarTareas();

            // Agregar tareas a Prolog
            agregarTareas(request.getTareas());

            // Ejecutar planificación
            // ⚠️ USAR org.jpl7.Integer para evitar ambigüedad
            Term tiempoDisponible = new org.jpl7.Integer(request.getTiempoDisponible());
            Term climaActual = new Atom(request.getClimaActual());
            Term resultado = new Variable("TareasResultado");

            Query planQuery = new Query(
                    "planificar_tareas",
                    new Term[]{tiempoDisponible, climaActual, resultado}
            );

            log.info("Ejecutando consulta Prolog: planificar_tareas({}, {}, TareasResultado)",
                    request.getTiempoDisponible(), request.getClimaActual());

            if (planQuery.hasSolution()) {
                Map<String, Term> solution = planQuery.oneSolution();
                Term tareasResultado = solution.get("TareasResultado");

                log.info("✓ Prolog encontró una solución");

                List<TareaPlanificada> plan = parsearResultado(tareasResultado);

                if (plan.isEmpty()) {
                    log.warn("El plan está vacío - no se pudieron completar las tareas");
                    return crearRespuestaNoViable();
                }

                log.info("✓ Plan generado con {} tareas", plan.size());
                return crearRespuestaViable(plan);
            } else {
                log.warn("✗ Prolog no encontró solución - plan no viable");
                return crearRespuestaNoViable();
            }

        } catch (Exception e) {
            log.error("✗ Error en planificación Prolog: {}", e.getMessage(), e);
            return crearRespuestaError(e);
        }
    }

    private void limpiarTareas() {
        try {
            Query cleanQuery = new Query("limpiar_tareas");
            cleanQuery.hasSolution();
            log.info("✓ Base de conocimiento Prolog limpiada");
        } catch (Exception e) {
            log.error("Error al limpiar tareas: {}", e.getMessage());
        }
    }

    private void agregarTareas(List<Map<String, Object>> tareas) {
        for (Map<String, Object> tarea : tareas) {
            try {
                String nombre = (String) tarea.get("nombre");
                String prioridad = (String) tarea.get("prioridad");
                java.lang.Integer tiempo = ((Number) tarea.get("tiempo")).intValue();
                java.lang.Integer peso = ((Number) tarea.get("peso")).intValue();
                String clima = (String) tarea.getOrDefault("clima", "cualquiera");
                String dependeDe = (String) tarea.getOrDefault("dependeDe", "ninguna");

                // ⚠️ USAR org.jpl7.Integer para los términos Prolog
                Term[] args = new Term[]{
                        new Atom(nombre),
                        new Atom(prioridad),
                        new org.jpl7.Integer(tiempo),
                        new org.jpl7.Integer(peso),
                        new Atom(clima),
                        new Atom(dependeDe)
                };

                Query addQuery = new Query("agregar_tarea", args);
                if (addQuery.hasSolution()) {
                    log.info("  ✓ Tarea agregada: {} (prioridad: {}, tiempo: {} min, clima: {})",
                            nombre, prioridad, tiempo, clima);
                } else {
                    log.warn("  ✗ No se pudo agregar tarea: {}", nombre);
                }
            } catch (Exception e) {
                log.error("Error al agregar tarea: {}", e.getMessage());
            }
        }
    }

    private List<TareaPlanificada> parsearResultado(Term resultado) {
        List<TareaPlanificada> plan = new ArrayList<>();

        try {
            if (resultado.isListNil()) {
                log.info("Lista de resultados vacía");
                return plan;
            }

            Term[] elementos = Util.listToTermArray(resultado);
            log.info("Parseando {} elementos del resultado", elementos.length);

            for (Term elemento : elementos) {
                if (elemento.hasFunctor("tarea", 2)) {
                    Term[] args = elemento.args();
                    String nombre = args[0].name();
                    String hora = args[1].name();

                    plan.add(new TareaPlanificada(nombre, hora));
                    log.info("  → {} programada a las {}", nombre, hora);
                }
            }
        } catch (Exception e) {
            log.error("Error al parsear resultado: {}", e.getMessage(), e);
        }

        return plan;
    }

    private PlanificacionResponse crearRespuestaViable(List<TareaPlanificada> plan) {
        PlanificacionResponse response = new PlanificacionResponse();
        response.setPlan(plan);
        response.setViable(true);
        response.setMensaje("Plan viable generado exitosamente. Todas las tareas pueden completarse en el tiempo disponible.");
        return response;
    }

    private PlanificacionResponse crearRespuestaNoViable() {
        PlanificacionResponse response = new PlanificacionResponse();
        response.setPlan(new ArrayList<>());
        response.setViable(false);
        response.setMensaje("No es posible completar todas las tareas en el tiempo disponible o con las restricciones de clima actuales.");
        return response;
    }

    private PlanificacionResponse crearRespuestaError(Exception e) {
        PlanificacionResponse response = new PlanificacionResponse();
        response.setPlan(new ArrayList<>());
        response.setViable(false);
        response.setMensaje("Error al procesar la planificación: " + e.getMessage());
        return response;
    }
}