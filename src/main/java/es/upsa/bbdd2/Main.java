package es.upsa.bbdd2;

import es.upsa.bbdd2.application.Dao;
import es.upsa.bbdd2.application.Impl.DaoImpl;
import es.upsa.bbdd2.domain.entities.*;
import es.upsa.bbdd2.exceptions.ApplicationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Dao dao = new DaoImpl("jdbc:postgresql://localhost:5432/postgres", "system", "manager")) {
            try {
                // Registro de un plato
                String nombrePlato = "Ensalada César";
                String descripcion = "Ensalada fresca con aderezo César, pollo y crutones.";
                double precio = 12.50;
                EnumeracionTipo tipo = EnumeracionTipo.ENTRANTE;

                List<CantidadIngrediente> cantidadesIngredientes = new ArrayList<>();
                cantidadesIngredientes.add(new CantidadIngrediente("Lechuga", 100, UnidadMedida.GRAMOS));
                cantidadesIngredientes.add(new CantidadIngrediente("Pollo", 150, UnidadMedida.GRAMOS));
                cantidadesIngredientes.add(new CantidadIngrediente("Aderezo César", 50, UnidadMedida.GRAMOS));
                cantidadesIngredientes.add(new CantidadIngrediente("Crutones", 30, UnidadMedida.GRAMOS));

                Plato plato = dao.registrarPlato(nombrePlato, descripcion, precio, tipo, cantidadesIngredientes);

                System.out.println("Plato registrado con éxito:");
                System.out.println("ID: " + plato.getId());
                System.out.println("Nombre: " + plato.getNombre());
                System.out.println("Descripción: " + plato.getDescripcion());
                System.out.println("Precio: " + plato.getPrecio());
                System.out.println("Tipo: " + plato.getTipo());
                System.out.println("Ingredientes:");
                for (Compuesto compuesto : plato.getIngredientes()) {
                    System.out.println("- Ingrediente: " + compuesto.getIngrediente().getNombre() +
                            ", Cantidad: " + compuesto.getCantidad() +
                            ", Unidad: " + compuesto.getUnidadMedida());
                }
            } catch (ApplicationException e) {
                System.err.println("Error al registrar el plato: " + e.getMessage());
            }

            try {
                String nombreMenu = "Menú Especial Invierno";
                LocalDate desde = LocalDate.of(2025, 1, 1);
                LocalDate hasta = LocalDate.of(2025, 1, 31);

                List<String> platosIds = Arrays.asList("1", "2");

                Menu menu = dao.registrarMenu(nombreMenu, hasta, desde, platosIds);

                System.out.println("Menú registrado con éxito:");
                System.out.println("ID del menú: " + menu.getId());
                System.out.println("Nombre: " + menu.getNombre());
                System.out.println("Precio: " + menu.getPrecio());
                System.out.println("Válido desde: " + menu.getDesde());
                System.out.println("Válido hasta: " + menu.getHasta());

                // Mostrar los platos agrupados por tipo
                // Aquí se accede directamente al mapa de platos
                Map<EnumeracionTipo, List<Plato>> platosPorTipo = menu.getPlatosPorTipo();

                if (platosPorTipo != null && !platosPorTipo.isEmpty()) {
                    platosPorTipo.forEach((tipoMenu, listaPlatos) -> {
                        System.out.println("Tipo: " + tipoMenu);
                        listaPlatos.forEach(plato -> {
                            System.out.println("    - " + plato.getNombre());
                        });
                    });
                } else {
                    System.out.println("No hay platos registrados en este menú.");
                }
            } catch (ApplicationException e) {
                System.err.println("Error al registrar el menú: " + e.getMessage());
            }

            try {
                List<Menu> menus = dao.buscarMenu(LocalDate.now());
                System.out.println("Menus disponibles para la fecha actual:");
                for (Menu menu : menus) {
                    System.out.println("Nombre: " + menu.getNombre());
                    System.out.println("Precio: " + menu.getPrecio());
                }
            } catch (SQLException e) {
                System.err.println("Error al buscar los menus: " + e.getMessage());
            }

            try {
                List<Plato> platos = dao.buscarPlato(EnumeracionTipo.ENTRANTE, Arrays.asList("Lechuga", "Pollo"));
                System.out.println("Platos encontrados:");
                for (Plato plato : platos) {
                    System.out.println("Nombre: " + plato.getNombre());
                    System.out.println("Precio: " + plato.getPrecio());
                }
            } catch (SQLException e) {
                System.err.println("Error al ejecutar el servicio: " + e.getMessage());
            }

            try {
                double porcentaje = 0.7;
                String nombrePlato = "Atun con tomate";

                dao.subirPlatoPrecio(nombrePlato, porcentaje);

            } catch (ApplicationException e) {
                System.err.println("Error: " + e.getMessage());


            } catch (Exception e) {
                System.err.println("Error general: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
