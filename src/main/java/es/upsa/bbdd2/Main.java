package es.upsa.bbdd2;

import es.upsa.bbdd2.application.Dao;
import es.upsa.bbdd2.application.Impl.DaoImpl;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        try (Dao dao = new DaoImpl("jdbc:postgresql://localhost:5432/postgres", "system", "manager")) {


        }
    }
}

