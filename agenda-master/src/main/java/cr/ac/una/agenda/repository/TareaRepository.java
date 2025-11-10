package cr.ac.una.agenda.repository;

import cr.ac.una.agenda.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findByFecha(LocalDate fecha);
    List<Tarea> findByFechaAndCompletada(LocalDate fecha, Boolean completada);
}