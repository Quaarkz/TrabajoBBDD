package es.upsa.bbdd2.database.domain.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")


public class Ingrediente {

    private int id;
    private String ingrediente;
    private List<CantidadIngrediente> cantidades;

}
