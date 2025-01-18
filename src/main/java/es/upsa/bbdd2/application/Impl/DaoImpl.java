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

        final String insertMenuSQL = """
            INSERT INTO Menu (id, nombre, precio, hasta, desde) VALUES (nextval('seq_menus'), ?, ?, ?, ?)
        """;

        final String selectPlatoSQL = """
            SELECT * 
            FROM plato as p
            WHERE p.id = ?
            """;

        final String insertMenuPlatoSQL = """
            INSERT INTO menuplato(menu_id, plato_id) VALUES (?, ?)
        """;

        final String[] fields = {"id"};

        double menuPrecio = 0.0;
        Map<EnumeracionTipo, List<Plato>> platosPorTipo = new HashMap<>();
        List<String> platosIds = new ArrayList<>();

        // Buscar platos y agruparlos por tipo
        for (String platoId : platos) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectPlatoSQL)) {
                preparedStatement.setString(1, platoId);
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
                        platosIds.add(resultSet.getString("id"));  // Agregar el id del plato a la lista
                    } else {
                        throw new ApplicationException("El plato con ID " + platoId + " no existe.");
                    }
                }
            } catch (SQLException sqlException) {
                throw new ApplicationException(sqlException);
            }
        }
        menuPrecio = menuPrecio * 0.85;

        // Insertar el menú en la base de datos
        Menu menuInsertado = Menu.builder()
                .withId("0")
                .withNombre(nombre)
                .withPrecio(menuPrecio)
                .withHasta(hasta)
                .withDesde(desde)
                .withPlatosPorTipo(platosPorTipo)
                .build();

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertMenuSQL, fields)) {
            preparedStatement.setString(1, nombre);
            preparedStatement.setDouble(2, menuPrecio);
            preparedStatement.setDate(3, Date.valueOf(hasta));
            preparedStatement.setDate(4, Date.valueOf(desde));
            preparedStatement.executeUpdate();

            // Obtener el ID del menú insertado
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    String menuId = generatedKeys.getString(1);
                    menuInsertado.setId(menuId);

                    // Insertar los platos en la tabla menuplato
                    for (String platoId : platosIds) {
                        try (PreparedStatement insertMenuPlatoStmt = connection.prepareStatement(insertMenuPlatoSQL)) {
                            insertMenuPlatoStmt.setString(1, menuId);  // menu_id
                            insertMenuPlatoStmt.setString(2, platoId); // plato_id
                            insertMenuPlatoStmt.executeUpdate();
                        } catch (SQLException sqlException) {
                            throw new ApplicationException("Error al insertar en menuplato: " + sqlException.getMessage());
                        }
                    }

                    return menuInsertado;
                } else {
                    throw new ApplicationException("No se pudo obtener el ID del menú recién insertado.");
                }
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
    public List<Menu> buscarMenu(LocalDate fecha) throws SQLException {
        List<Menu> menus = new ArrayList<>();

        final String query = """
            
                SELECT m.id AS menu_id, m.nombre AS menu_nombre, m.precio AS menu_precio,
                   p.id AS plato_id, p.nombre AS plato_nombre, p.descripcion AS plato_descripcion, p.precio AS plato_precio, p.tipo AS plato_tipo
            FROM Menu AS m
            JOIN menuplato AS mp ON m.id = mp.menu_id
            JOIN Plato AS p ON mp.plato_id = p.id
            WHERE m.desde <= ? AND m.hasta >= ?
            """;

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setDate(1, Date.valueOf(fecha));
            preparedStatement.setDate(2, Date.valueOf(fecha));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                Map<String, Menu> menuMap = new HashMap<>();

                while (resultSet.next()) {
                    String menuId = resultSet.getString("menu_id");
                    String menuNombre = resultSet.getString("menu_nombre");
                    double menuPrecio = resultSet.getDouble("menu_precio");
                    String platoId = resultSet.getString("plato_id");
                    String platoNombre = resultSet.getString("plato_nombre");
                    String platoDescripcion = resultSet.getString("plato_descripcion");
                    double platoPrecio = resultSet.getDouble("plato_precio");
                    String platoTipoStr = resultSet.getString("plato_tipo");
                    EnumeracionTipo platoTipo = EnumeracionTipo.valueOf(platoTipoStr);


                    Menu menu = menuMap.get(menuId);
                    if (menu == null) {
                        menu = Menu.builder()
                                .withId(menuId)
                                .withNombre(menuNombre)
                                .withPrecio(menuPrecio)
                                .withPlatosPorTipo(new HashMap<>())
                                .build();
                        menuMap.put(menuId, menu);
                    }

                    Plato plato = Plato.builder()
                            .withId(platoId)
                            .withNombre(platoNombre)
                            .withDescripcion(platoDescripcion)
                            .withPrecio(platoPrecio)
                            .withTipo(platoTipo)
                            .build();

                    menu.getPlatosPorTipo()
                            .computeIfAbsent(platoTipo, tipo -> new ArrayList<>())
                            .add(plato);
                }

                if (menuMap.isEmpty()) {
                    throw new SQLException("No hay menús disponibles para la fecha indicada.");
                }
                menus.addAll(menuMap.values());
            }
            for (Menu menu : menus) {
                System.out.println("Menú: " + menu.getNombre());
                System.out.println(" Precio: " + menu.getPrecio());
                System.out.println(" Platos:");

                for (EnumeracionTipo tipo : EnumeracionTipo.values()) {
                    if (menu.getPlatosPorTipo().containsKey(tipo)) {
                        System.out.println(" " + tipo.name() + ":");
                        for (Plato plato : menu.getPlatosPorTipo().get(tipo)) {
                            System.out.println("  " + plato.getNombre());
                        }
                    }
                }
            }
            return menus;
        }
    }


    @Override
    public List<Plato> buscarPlato (EnumeracionTipo tipo, List < String > ingredientes) throws SQLException
    {
        List<Plato> platosResultado = new ArrayList<>();

        if (ingredientes.isEmpty()) {
            return platosResultado;
        }


        final String query = """
                    SELECT DISTINCT p.id, p.nombre, p.descripcion, p.precio, p.tipo, i.nombre AS nombreIngrediente
                            FROM plato as p
                            LEFT JOIN platoingrediente as pi ON p.id = pi.plato_id
                            LEFT JOIN ingrediente as i ON pi.ingrediente_id = i.id
                            WHERE p.tipo = ?
            """;


        Map<String, Plato> platosMap = new HashMap<>();
        Map<String, List<String>> ingredientesPorPlato = new HashMap<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tipo.name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String platoId = resultSet.getString("id");

                    if (!platosMap.containsKey(platoId)) {
                        Plato plato = Plato.builder()
                                .withId(platoId)
                                .withNombre(resultSet.getString("nombre"))
                                .withDescripcion(resultSet.getString("descripcion"))
                                .withPrecio(resultSet.getDouble("precio"))
                                .withTipo(EnumeracionTipo.valueOf(resultSet.getString("tipo")))
                                .build();
                        platosMap.put(platoId, plato);
                        ingredientesPorPlato.put(platoId, new ArrayList<>());
                    }
                    // Agregamos el ingrediente al mapa correspondiente al plato
                    String ingredienteNombre = resultSet.getString("nombreIngrediente");
                    if (ingredienteNombre != null) {
                        ingredientesPorPlato.get(platoId).add(ingredienteNombre);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Error al buscar los platos", e);
        }

        // Filtrar los platos que no contienen ninguno de los ingredientes excluidos
        for (Map.Entry<String, Plato> entry : platosMap.entrySet()) {
            String platoId = entry.getKey();
            List<String> ingredientesPlato = ingredientesPorPlato.get(platoId);

            // Si ningún ingrediente del plato está en la lista de ingredientes excluidos, lo agregamos al resultado
            boolean contieneIngredienteExcluido = ingredientesPlato.stream()
                    .anyMatch(ingredientes::contains);
            if (!contieneIngredienteExcluido) {
                platosResultado.add(entry.getValue());
            }
        }

        return platosResultado;
    }

    @Override
    public void subirPlatoPrecio(String nombre, double porcentaje) throws ApplicationException {

        if (porcentaje < 0 || porcentaje > 1) {
            throw new ApplicationException("El porcentaje introducido debe estar entre 0 y 1.");
        }

        final String querySelect = "SELECT precio FROM Plato WHERE nombre = ?";
        final String queryUpdate = "UPDATE Plato SET precio = ? WHERE nombre = ?";

        try {

            connection.setAutoCommit(false);


            double precioActual;
            try (PreparedStatement selectStatement = connection.prepareStatement(querySelect)) {
                selectStatement.setString(1, nombre);

                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (resultSet.next()) {
                        precioActual = resultSet.getDouble("precio");
                    } else {
                        throw new ApplicationException("No se encontró un plato con el nombre: " + nombre);
                    }
                }
            }

            double nuevoPrecio = 0.0;
            nuevoPrecio = precioActual * (1 + porcentaje);


            try (PreparedStatement updateStatement = connection.prepareStatement(queryUpdate)) {
                updateStatement.setDouble(1, nuevoPrecio);
                updateStatement.setString(2, nombre);

                int rowsUpdated = updateStatement.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new ApplicationException("No se pudo actualizar el precio del plato.");
                }
            }


            connection.commit();
            System.out.println("El precio del plato '" + nombre + "' ha sido actualizado a: " + nuevoPrecio);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new ApplicationException("Error al revertir la transacción: " + rollbackEx.getMessage(), rollbackEx);
            }
            throw new ApplicationException("Error al procesar la operación: " + e.getMessage(), e);
        } finally {
            try {

                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new ApplicationException("Error al restaurar el auto-commit: " + e.getMessage(), e);
            }
        }
    }


    @Override
    public void close () throws Exception {
        if (connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }
}





