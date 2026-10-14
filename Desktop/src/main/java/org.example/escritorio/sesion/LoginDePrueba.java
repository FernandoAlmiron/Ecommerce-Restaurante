package org.example.escritorio.sesion;

import java.util.Arrays;
import java.util.Map;

/**
 * LOGIN TEMPORAL, sin base de datos: los mismos usuarios de data.sql (clave = usuario + "123").
 * Se borra cuando exista AutenticacionService conectado al backend.
 */
public class LoginDePrueba implements ServicioLogin {

    private record Usuario(String clave, String nombre, String sector, int legajo) {}

    private static final Map<String, Usuario> USUARIOS = Map.of(
            "admin", new Usuario("admin123", "Laura Benitez", "ADMINISTRACION", 1),
            "recepcion", new Usuario("recepcion123", "Camila Suarez", "RECEPCION", 2),
            "mozo", new Usuario("mozo123", "Martin Acosta", "SALON", 3),
            "barra", new Usuario("barra123", "Julieta Romero", "BARRA", 4),
            "cocina", new Usuario("cocina123", "Gustavo Ferrari", "COCINA", 5),
            "caja", new Usuario("caja123", "Valeria Molina", "CAJA", 6),
            "repartidor", new Usuario("repartidor123", "Nicolas Vega", "REPARTIDOR", 7),
            "rrhh", new Usuario("rrhh123", "Patricia Duarte", "RECURSOSHUMANOS", 8));

    @Override
    public void ingresar(String usuario, char[] clave) {
        esperar(); // simula lo que tarda la base, para ver el cursor de espera

        if ("cliente".equals(usuario)) {
            throw new IllegalStateException(
                    "Esta aplicación es para el personal del restaurante. Los clientes usan la página web.");
        }

        Usuario encontrado = USUARIOS.get(usuario);
        if (encontrado == null || !Arrays.equals(encontrado.clave().toCharArray(), clave)) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        Rol rol = Rol.desdeSector(encontrado.sector());
        if (!rol.puedeUsarEscritorio()) {
            throw new IllegalStateException(
                    "Tu usuario no tiene un sector con permisos. Consultá con Recursos Humanos.");
        }

        Sesion.iniciar(usuario, encontrado.nombre(), rol, encontrado.legajo());
    }

    private void esperar() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}