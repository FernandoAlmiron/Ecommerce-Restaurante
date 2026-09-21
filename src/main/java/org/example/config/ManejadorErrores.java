package org.example.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ManejadorErrores {

    // datos que faltan o son incorrectos -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> datosInvalidos(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // una regla del negocio no se cumple (sin stock, local cerrado, sin lugar...) -> 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> reglaDeNegocio(IllegalStateException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    // no existe lo que se busca por id (findById(...).orElseThrow) -> 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NoSuchElementException e) {
        String mensaje = e.getMessage();
        // orElseThrow() sin mensaje trae "No value present": no le sirve a nadie
        if (mensaje == null || mensaje.equals("No value present")) {
            mensaje = "No se encontro el registro pedido";
        }
        return respuesta(HttpStatus.NOT_FOUND, mensaje);
    }

    // la base rechazo el dato (usuario repetido, campo obligatorio vacio...) -> 409
    // el mensaje real de MySQL no se devuelve: muestra nombres de tablas y columnas
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT, "Ya existe un registro con esos datos, o falta un dato obligatorio");
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        String texto = (mensaje != null) ? mensaje : "Solicitud invalida";
        return ResponseEntity.status(estado).body(Map.of("error", texto));
    }
}
