package cr.ac.una.agenda.service;

import cr.ac.una.agenda.dto.*;
import cr.ac.una.agenda.*;
import cr.ac.una.agenda.entity.Clima;
import cr.ac.una.agenda.entity.Prioridad;
import cr.ac.una.agenda.entity.Tarea;
import cr.ac.una.agenda.repository.*;
import cr.ac.una.agenda.service.AgendaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TareaService {

    private final TareaRepository tareaRepository;
    private final PrioridadRepository prioridadRepository;
    private final ClimaRepository climaRepository;
    private final AgendaService agendaService; // ← CAMBIO: usamos AgendaService

    @Transactional
    public TareaResponse crearTarea(TareaRequest request) {
        Tarea tarea = new Tarea();
        tarea.setNombre(request.getNombre());

        Prioridad prioridad = prioridadRepository.findByNivel(request.getPrioridad())
                .orElseThrow(() -> new RuntimeException("Prioridad no encontrada: " + request.getPrioridad()));
        tarea.setPrioridad(prioridad);

        tarea.setTiempoEstimado(request.getTiempoEstimado());

        if (request.getClima() != null) {
            Clima clima = climaRepository.findByTipo(request.getClima())
                    .orElseThrow(() -> new RuntimeException("Clima no encontrado: " + request.getClima()));
            tarea.setClima(clima);
        }

        if (request.getTareaDependenciaId() != null) {
            Tarea dependencia = tareaRepository.findById(request.getTareaDependenciaId())
                    .orElseThrow(() -> new RuntimeException("Tarea dependencia no encontrada"));
            tarea.setTareaDependencia(dependencia);
        }

        tarea.setFecha(request.getFecha());
        tarea.setCompletada(false);

        Tarea saved = tareaRepository.save(tarea);
        return convertirATareaResponse(saved);
    }

    public List<TareaResponse> obtenerTareasPorFecha(LocalDate fecha) {
        return tareaRepository.findByFecha(fecha).stream()
                .map(this::convertirATareaResponse)
                .collect(Collectors.toList());
    }

    public List<TareaResponse> obtenerTodasLasTareas() {
        return tareaRepository.findAll().stream()
                .map(this::convertirATareaResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TareaResponse actualizarTarea(Long id, TareaRequest request) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        tarea.setNombre(request.getNombre());

        Prioridad prioridad = prioridadRepository.findByNivel(request.getPrioridad())
                .orElseThrow(() -> new RuntimeException("Prioridad no encontrada"));
        tarea.setPrioridad(prioridad);

        tarea.setTiempoEstimado(request.getTiempoEstimado());

        if (request.getClima() != null) {
            Clima clima = climaRepository.findByTipo(request.getClima())
                    .orElseThrow(() -> new RuntimeException("Clima no encontrado"));
            tarea.setClima(clima);
        } else {
            tarea.setClima(null);
        }

        // 🔹 Editar dependencia
        if (request.getTareaDependenciaId() != null) {
            if (request.getTareaDependenciaId().equals(id)) {
                throw new RuntimeException("Una tarea no puede depender de sí misma");
            }
            Tarea dependencia = tareaRepository.findById(request.getTareaDependenciaId())
                    .orElseThrow(() -> new RuntimeException("Tarea dependencia no encontrada"));
            tarea.setTareaDependencia(dependencia);
        } else {
            tarea.setTareaDependencia(null);
        }

        // 🔹 Actualizar fecha
        tarea.setFecha(request.getFecha());

        Tarea updated = tareaRepository.save(tarea);
        return convertirATareaResponse(updated);
    }


    @Transactional
    public void eliminarTarea(Long id) {
        tareaRepository.deleteById(id);
    }

    @Transactional
    public void marcarComoCompletada(Long id) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));
        tarea.setCompletada(true);
        tareaRepository.save(tarea);
    }

    public PlanificacionResponse planificarDia(PlanificacionRequest request) {
        LocalDate fecha = LocalDate.parse(request.getFecha());
        List<Tarea> tareas = tareaRepository.findByFechaAndCompletada(fecha, false);

        if (tareas.isEmpty()) {
            PlanificacionResponse response = new PlanificacionResponse();
            response.setViable(true);
            response.setMensaje("No hay tareas pendientes para este día");
            response.setPlan(new ArrayList<>());
            return response;
        }

        // Preparar datos para Prolog
        Map<String, Object> prologRequest = new HashMap<>();
        prologRequest.put("tiempoDisponible", request.getTiempoDisponible());
        prologRequest.put("climaActual", request.getClimaActual());

        List<Map<String, Object>> tareasData = tareas.stream().map(t -> {
            Map<String, Object> tareaMap = new HashMap<>();
            tareaMap.put("id", t.getId());
            tareaMap.put("nombre", t.getNombre());
            tareaMap.put("prioridad", t.getPrioridad().getNivel().toLowerCase());
            tareaMap.put("peso", t.getPrioridad().getPeso());
            tareaMap.put("tiempo", t.getTiempoEstimado());
            tareaMap.put("clima", t.getClima() != null ? t.getClima().getTipo() : "cualquiera");

            if (t.getTareaDependencia() != null) {
                tareaMap.put("dependeDe", t.getTareaDependencia().getNombre());
            }

            return tareaMap;
        }).collect(Collectors.toList());

        prologRequest.put("tareas", tareasData);

        // CAMBIO: usar agendaService en lugar de prologClientService
        PlanificacionResponse response = agendaService.planificarTareas(prologRequest);

        // Guardar horas de inicio si el plan es viable
        if (response.getViable() && response.getPlan() != null) {
            actualizarHorasInicio(tareas, response.getPlan());
        }

        return response;
    }

    @Transactional

    private void actualizarHorasInicio(List<Tarea> tareas, List<TareaPlanificada> plan) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (TareaPlanificada tp : plan) {
            tareas.stream()
                    .filter(t -> t.getNombre().equals(tp.getTarea()))
                    .findFirst()
                    .ifPresent(tarea -> {
                        LocalTime hora = LocalTime.parse(tp.getHoraInicio(), formatter);
                        tarea.setHoraInicio(hora);
                        tareaRepository.save(tarea);
                    });
        }
    }

    private TareaResponse convertirATareaResponse(Tarea tarea) {
        TareaResponse response = new TareaResponse();
        response.setId(tarea.getId());
        response.setNombre(tarea.getNombre());
        response.setPrioridad(tarea.getPrioridad().getNivel());
        response.setTiempoEstimado(tarea.getTiempoEstimado());
        response.setClima(tarea.getClima() != null ? tarea.getClima().getTipo() : null);
        response.setTareaDependencia(tarea.getTareaDependencia() != null ?
                tarea.getTareaDependencia().getNombre() : null);
        response.setFecha(tarea.getFecha().toString());
        response.setHoraInicio(tarea.getHoraInicio());
        response.setCompletada(tarea.getCompletada());
        return response;
    }
}