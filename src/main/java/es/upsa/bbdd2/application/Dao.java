package es.upsa.bbdd2.application;

import es.upsa.bbdd2.domain.entities.CantidadIngrediente;
import es.upsa.bbdd2.domain.entities.EnumeracionTipo;
import es.upsa.bbdd2.domain.entities.Plato;
import es.upsa.bbdd2.exceptions.ApplicationException;

import java.util.List;

public interface Dao extends AutoCloseable{
    public Plato registratPlato(String nombre, String descripcion, double precio,
                                EnumeracionTipo tipo, List<CantidadIngrediente> cantidadIngredientes)
                                throws ApplicationException;

}
