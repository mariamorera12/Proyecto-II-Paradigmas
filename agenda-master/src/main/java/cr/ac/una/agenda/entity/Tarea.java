package cr.ac.una.agenda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tareas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "prioridad_id", nullable = false)
    private Prioridad prioridad;

    @Column(name = "tiempo_estimado", nullable = false)
    private Integer tiempoEstimado; // en minutos

    @ManyToOne
    @JoinColumn(name = "clima_id")
    private Clima clima;

    @ManyToOne
    @JoinColumn(name = "tarea_dependencia_id")
    private Tarea tareaDependencia;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "completada")
    private Boolean completada = false;

}