package es.upsa.bbdd2;

import es.upsa.bbdd2.application.Dao;
import es.upsa.bbdd2.application.Impl.DaoImpl;
import es.upsa.bbdd2.domain.entities.*;
import es.upsa.bbdd2.exceptions.ApplicationException;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Dao dao = new DaoImpl("jdbc:postgresql://localhost:5432/postgres", "system", "manager")) {

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
        } catch (ApplicationException applicationException) {
            throw new Exception(applicationException.getMessage());
        }
    }
}

