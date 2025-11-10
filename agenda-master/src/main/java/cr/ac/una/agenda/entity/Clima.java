package cr.ac.una.agenda.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "climas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clima {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tipo; // "soleado", "lluvioso", "nublado", "cualquiera"

}