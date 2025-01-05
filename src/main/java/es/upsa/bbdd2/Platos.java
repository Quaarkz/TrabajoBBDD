package es.upsa.bbdd2;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")


public class Platos {
    private int id;
    private String nombre;
    private String descripcion;
    private float precio;
    private EnumeracionTipo tipo; //hacer enum
}
