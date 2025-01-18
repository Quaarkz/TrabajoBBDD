package es.upsa.bbdd2.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Builder(setterPrefix = "with")
@AllArgsConstructor
@Data

public class Menu {

    private String id;
    private String nombre;
    private double precio;
    private LocalDate desde;
    private LocalDate hasta;
    private Map<EnumeracionTipo, List<Plato>> platosPorTipo;
}
