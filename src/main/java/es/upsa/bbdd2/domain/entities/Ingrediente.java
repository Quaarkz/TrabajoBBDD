package es.upsa.bbdd2.domain.entities;


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
    private String nombre;
    private List<CantidadIngrediente> cantidades;

}

