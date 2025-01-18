package es.upsa.bbdd2.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")

public class CantidadIngrediente {

    private String cantidad;
    private UnidadMedida unidadMedida;
}
