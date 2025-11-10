package cr.ac.una.agenda.repository;

import cr.ac.una.agenda.entity.Clima;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClimaRepository extends JpaRepository<Clima, Long> {
    Optional<Clima> findByTipo(String tipo);
}