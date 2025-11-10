package cr.ac.una.agenda.repository;

import cr.ac.una.agenda.entity.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Long> {
    Optional<Prioridad> findByNivel(String nivel);
}