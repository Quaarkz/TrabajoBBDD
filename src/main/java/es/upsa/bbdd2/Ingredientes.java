package es.upsa.bbdd2;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")


public class Ingredientes {

    private int id;
    private String ingrediente;

}
