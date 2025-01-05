package es.upsa.bbdd2.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Builder(setterPrefix = "with")
@AllArgsConstructor
@Data

public class Menu {

    private int id;
    private String nombre;
    private float precio;
    private Date desde;
    private Date hasta;
    private List<Plato> platos;
}
