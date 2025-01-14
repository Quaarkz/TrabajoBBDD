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


public class Plato {

    private int id;
    private String nombre;
    private String descripcion;
    private float precio;
    private EnumeracionTipo tipo;
    private List<Ingrediente> ingredientes;//hacer enum

}
