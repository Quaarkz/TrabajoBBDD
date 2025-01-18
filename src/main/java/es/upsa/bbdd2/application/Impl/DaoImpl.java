package es.upsa.bbdd2.application.Impl;

import es.upsa.bbdd2.application.Dao;
import es.upsa.bbdd2.domain.entities.*;
import es.upsa.bbdd2.exceptions.*;
import org.postgresql.Driver;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.exit;

public class DaoImpl implements Dao {
    Connection connection;

    public DaoImpl(String url, String user, String password) throws SQLException {
        DriverManager.registerDriver(new Driver());
        this.connection = DriverManager.getConnection(url, user, password);
    }

    @Override
    public Plato registrarPlato(String nombre, String descripcion, double precio, EnumeracionTipo tipo, List<CantidadIngrediente> cantidadesIngredientes) throws ApplicationException {
        final String SQL_INSERT_PLATO = """
        INSERT INTO plato(id, nombre, descripcion, precio, tipo) 
        VALUES(nextval('seq_platos'), ?, ?, ?, ?)
        """;
        final String SQL_INSERT_INGREDIENTE = """
        INSERT INTO ingrediente(id, nombre) 
        VALUES(nextval('seq_ingredientes'), ?)
        """;
        final String SQL_SELECT_INGREDIENTE_ID = """
        SELECT id FROM ingrediente WHERE nombre = ?
        """;
        final String SQL_INSERT_PLATO_INGREDIENTE = """
        INSERT INTO platoingrediente(plato_id, ingrediente_id, cantidad, unidad_medida)
        VALUES(?, ?, ?, ?)
        """;

        final String[] generatedColumns = {"id"};

        try {
            // Insertar el plato y obtener su ID
            String idPlato;
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_PLATO, generatedColumns)) {
                preparedStatement.setString(1, nombre);
                preparedStatement.setString(2, descripcion);
                preparedStatement.setDouble(3, precio);
                preparedStatement.setString(4, tipo.name());
                preparedStatement.executeUpdate();

                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idPlato = generatedKeys.getString(1);
                    } else {
                        throw new ApplicationException("No se pudo obtener el ID del plato recién insertado.");
                    }
                }
            }

            // Registrar ingredientes y construir los objetos Compueso
            List<Compuesto> compuestos = new ArrayList<>();
            for (CantidadIngrediente cantidadIngrediente : cantidadesIngredientes) {
                String idIngrediente;

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_INGREDIENTE_ID)) {
                    preparedStatement.setString(1, cantidadIngrediente.getNombre());
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            idIngrediente = resultSet.getString(1);
                        } else {
                            try (PreparedStatement insertStatement = connection.prepareStatement(SQL_INSERT_INGREDIENTE, generatedColumns)) {
                                insertStatement.setString(1, cantidadIngrediente.getNombre());
                                insertStatement.executeUpdate();
                                try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                                    if (generatedKeys.next()) {
                                        idIngrediente = generatedKeys.getString(1);
                                    } else {
                                        throw new ApplicationException("No se pudo obtener el ID del ingrediente recién insertado.");
                                    }
                                }
                            }
                        }
                    }
                }

                try (PreparedStatement preparedStatement = connection.prepareStatement(SQL_INSERT_PLATO_INGREDIENTE)) {
                    preparedStatement.setString(1, idPlato);
                    preparedStatement.setString(2, idIngrediente);
                    preparedStatement.setInt(3, cantidadIngrediente.getCantidad());
                    preparedStatement.setString(4, cantidadIngrediente.getUnidadMedida().name());
                    preparedStatement.executeUpdate();
                }

                // Construir el objeto Compuesto
                Ingrediente ingrediente = new Ingrediente(idIngrediente, cantidadIngrediente.getNombre());
                Compuesto compuesto = new Compuesto(ingrediente, cantidadIngrediente.getCantidad(), cantidadIngrediente.getUnidadMedida().name());
                compuestos.add(compuesto);
            }

            // Construir y devolver el objeto Plato
            return Plato.builder()
                    .withId(idPlato)
                    .withNombre(nombre)
                    .withDescripcion(descripcion)
                    .withPrecio(precio)
                    .withTipo(tipo)
                    .withIngredientes(compuestos)
                    .build();
        } catch (SQLException sqlException) {
            throw new ApplicationException(sqlException);
        }
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


        final String MenuSql = """
                INSERT INTO Menu (id, nombre, precio, hasta, desde) VALUES (nextval('seq_menus'), ?, ?, ?, ?)
            """;

        final String selectPlato = """
                SELECT * 
                FROM plato as p
                WHERE p.id = ?
                """;

        final String[] fields = {"id"};

        double menuPrecio = 0.0;
        Map<EnumeracionTipo, List<Plato>> platosPorTipo = new HashMap<>();
        for (String plato : platos) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectPlato)) {
                preparedStatement.setString(1, plato);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        menuPrecio += resultSet.getDouble("precio");
                        EnumeracionTipo tipo = EnumeracionTipo.valueOf(resultSet.getString("tipo"));
                        if (platosPorTipo.containsKey(tipo)) {
                            platosPorTipo.get(tipo).add(Plato.builder()
                                    .withId(resultSet.getString("id"))
                                    .withNombre(resultSet.getString("nombre"))
                                    .withDescripcion(resultSet.getString("descripcion"))
                                    .withPrecio(resultSet.getDouble("precio"))
                                    .withTipo(tipo)
                                    .build());
                        } else {
                            List<Plato> platosAñadidos = new ArrayList<>();
                            platosAñadidos.add(Plato.builder()
                                    .withId(resultSet.getString("id"))
                                    .withNombre(resultSet.getString("nombre"))
                                    .withDescripcion(resultSet.getString("descripcion"))
                                    .withPrecio(resultSet.getDouble("precio"))
                                    .withTipo(tipo)
                                    .build());
                            platosPorTipo.put(tipo, platosAñadidos);
                        }
                    } else {
                        throw new ApplicationException("El plato " + plato + " no existe");
                    }
                }
            } catch (SQLException sqlException) {
                throw new ApplicationException(sqlException);
            }
        }
        menuPrecio = menuPrecio * 0.85;

        Menu menuInsertado = Menu.builder()
                .withId("0")
                .withNombre(nombre)
                .withPrecio(menuPrecio)
                .withHasta(hasta)
                .withDesde(desde)
                .withPlatosPorTipo(platosPorTipo)
                .build();

        try (PreparedStatement preparedStatement = connection.prepareStatement(MenuSql, fields)) {
            preparedStatement.setString(1, nombre);
            preparedStatement.setDouble(2, menuPrecio);
            preparedStatement.setDate(3, Date.valueOf(hasta));
            preparedStatement.setDate(4, Date.valueOf(desde));
            preparedStatement.executeUpdate();
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                generatedKeys.next();
                String id = generatedKeys.getString(1);
                menuInsertado.setId(id);
                return menuInsertado;
            }
        } catch (SQLException sqlException) {
            throw manageSQLException(sqlException);
        }

    }

    private ApplicationException manageSQLException(SQLException sqlException) {
        String message = sqlException.getMessage();
        if(message.contains("NN_MENU.NOMBRE")) return new NombreRequiredException(message);
        if(message.contains("NN_MENU.PRECIO")) return new PrecioRequiredException(message);
        if(message.contains("NN_MENU.HASTA")) return new HastaRequiredException(message);
        if(message.contains("NN_MENU.DESDE")) return new DesdeRequiredException(message);
        return new ApplicationException(sqlException);

    }


    @Override
                public List<Menu> buscarMenu (LocalDate fecha) throws SQLException {
                    //Devuelve los objetos menús disponibles en la fecha determinada
                    //La forma del objeto será:
                    //"Menu: "   menuConsulta.nombre o algo asi
                    //"Precio: " menuConsulta.precio
                    //"Platos:"  map como antes
                    //Tener en cuenta que puede no haber ninguno en la fecha o varios
                    return List.of();
                }

                @Override
                public List<Plato> buscarPlato (EnumeracionTipo tipo, List < String > ingredientes) throws SQLException
                {
                    //Devuelve los platos de EnumeracionTipo que NO tienen ningún ingrediente de la lista
                    //Tener en cuenta que puede no haber ninguno o muchos
                    return List.of();
                }

                @Override
                public void subirPlatoPrecio (String nombre,double porcentaje) throws ApplicationException {
                    //Incrementa el valor del plato en cierto porcentaje
                    //El plato se identifica por el nombre UNIQUE
                    //Valores entre 0 y 1 que habra que usar como porcentajes
                    //Por ejemplo 0.4 = 40%
                }

                @Override
                public void close () throws Exception {
                    if (connection != null) {
                        this.connection.close();
                        this.connection = null;
                    }
                }
}
