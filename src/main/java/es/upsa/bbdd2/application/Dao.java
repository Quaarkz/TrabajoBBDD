package es.upsa.bbdd2.application;

import es.upsa.bbdd2.domain.entities.CantidadIngrediente;
import es.upsa.bbdd2.domain.entities.EnumeracionTipo;
import es.upsa.bbdd2.domain.entities.Menu;
import es.upsa.bbdd2.domain.entities.Plato;
import es.upsa.bbdd2.exceptions.ApplicationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface Dao extends AutoCloseable{
    public Plato registrarPlato(String nombre, String descripcion, double precio,
                                EnumeracionTipo tipo, List<CantidadIngrediente> cantidadesIngredientes)
                                throws ApplicationException;
    public Menu registrarMenu(String nombre, LocalDate hasta, LocalDate desde, List<String> platos)
                                throws ApplicationException;
    public List<Menu> buscarMenu(LocalDate fecha) throws SQLException;
    public List<Plato> buscarPlato(EnumeracionTipo tipo, List<String> ingredientes) throws SQLException;
    public void subirPlatoPrecio(String nombre, double porcentaje) throws ApplicationException;
}
