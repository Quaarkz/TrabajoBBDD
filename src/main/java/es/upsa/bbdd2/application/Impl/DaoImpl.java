package es.upsa.bbdd2.application.Impl;

import es.upsa.bbdd2.application.Dao;
import es.upsa.bbdd2.domain.entities.CantidadIngrediente;
import es.upsa.bbdd2.domain.entities.EnumeracionTipo;
import es.upsa.bbdd2.domain.entities.Menu;
import es.upsa.bbdd2.domain.entities.Plato;
import es.upsa.bbdd2.exceptions.ApplicationException;
import org.postgresql.Driver;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class DaoImpl implements Dao {
    Connection connection;
    public DaoImpl(String url, String user, String password) throws SQLException {
        DriverManager.registerDriver(new Driver());
        this.connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public Plato registrarPlato(String nombre, String descripcion, double precio, EnumeracionTipo tipo, List<CantidadIngrediente> cantidadesIngredientes) throws ApplicationException {
        final String SQL = """
                INSERT INTO plato(ID,nombre,descripcion,precio,tipo) 
                VALUES(nextval('seq_platos'),?,?,?,?)
                """;
        final String SQL2 = """
                INSERT INTO platoingrediente(plato_id,nombre,ingrediente_id,cantidad,unidad_medida)
                VALUES(?,?,?,?)
                """;

        final String[] fields = {"id"};
        Plato platoInsertado = Plato.builder()
                .withId("0")
                .withNombre(nombre)
                .withDescripcion(descripcion)
                .withPrecio(precio)
                .withTipo(tipo)
                .withIngredientes(cantidadesIngredientes)
                .build();
        try(PreparedStatement preparedStatement = connection.prepareStatement(SQL, fields)){
            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, descripcion);
            preparedStatement.setDouble(3, precio);
            preparedStatement.setString(4, tipo.name());
            preparedStatement.executeUpdate();
            try(ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){
                generatedKeys.next();
                String id = generatedKeys.getString(1);
                platoInsertado.setId(id);
                return platoInsertado;
            }
        }catch (SQLException sqlException){
            throw new ApplicationException(sqlException);
            //throw manageSQLException(sqlException);
        }
        //INSERT Plato
        //ID sale de seq_platos
        //CantidadIngrediente registra el ingrediente si no existe,
        //          con el id de seq_ingredientes y el nombre
        //El objeto Plato devuelto contendrá los datos del plato incluyendo su id
        //          y una lista de objetos Compuesto{
        //                                       Ingrediente ingrediente;
        //                                       CantidadIngrediente.cantidad
        //                                       CantidadIngrediente.unidad}
        //Un objeto Compuesto por cada ingrediente del plato
    }

    @Override
    public Menu registrarMenu(String nombre, LocalDate hasta, LocalDate desde, List<String> platos) throws ApplicationException {
        //INSERT Menu
        //ID sale de seq_menus
        //Precio del menu = suma precio de los platos - 15%
        //El objeto Menu devuelto contendrá los datos del menú
        //      y un Map que representa los platos, su clave será EnumeracionTipo
        //      y su valor una lista formada por los platos con ese tipo
        //Si no existiera alguno de los platos de la lista se propagará excepcion y no se creará
        return null;
    }

    @Override
    public List<Menu> buscarMenu(LocalDate fecha) throws SQLException {
        //Devuelve los objetos menús disponibles en la fecha determinada
        //La forma del objeto será:
        //"Menu: "   menuConsulta.nombre o algo asi
        //"Precio: " menuConsulta.precio
        //"Platos:"  map como antes
        //Tener en cuenta que puede no haber ninguno en la fecha o varios
        return List.of();
    }

    @Override
    public List<Plato> buscarPlato(EnumeracionTipo tipo, List<String> ingredientes) throws SQLException {
        //Devuelve los platos de EnumeracionTipo que NO tienen ningún ingrediente de la lista
        //Tener en cuenta que puede no haber ninguno o muchos
        return List.of();
    }

    @Override
    public void subirPlatoPrecio(String nombre, double porcentaje) throws ApplicationException {
        //Incrementa el valor del plato en cierto porcentaje
        //El plato se identifica por el nombre UNIQUE
        //Valores entre 0 y 1 que habra que usar como porcentajes
        //Por ejemplo 0.4 = 40%
    }

    @Override
    public void close() throws Exception {
        if(connection != null) {
           this.connection.close();
            this.connection = null;
        }
    }
}
