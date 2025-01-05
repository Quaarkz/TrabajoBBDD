package es.upsa.bbdd2.database;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")

public class CantidadIngrediente {

    private int cantidad;
    private UnidadMedida unidadMedida;
}
