package com.rrhh.Shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("success", false);
        response.put("error", "VALIDACION");
        response.put("message", "Error de validacion. Revise los campos obligatorios.");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "RECURSO_NO_ENCONTRADO");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<Map<String, Object>> handleConflicto(ConflictoException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "CONFLICTO");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseEntity<Map<String, Object>> handleFaltaDato(Exception ex) {
        String nombre = ex instanceof MissingServletRequestParameterException p
                ? p.getParameterName() : ((MissingRequestHeaderException) ex).getHeaderName();
        return badRequest("DATOS_INVALIDOS", "Falta el dato requerido: " + nombre + ".");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTipoIncorrecto(MethodArgumentTypeMismatchException ex) {
        return badRequest("DATOS_INVALIDOS", "Error: el valor de '" + ex.getName() + "' no es valido.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "METODO_NO_PERMITIDO");
        response.put("message", "Metodo no permitido para esta ruta.");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleContenidoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "CONTENIDO_NO_SOPORTADO");
        response.put("message", "Tipo de contenido no soportado. Use application/json.");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException formato) {
            String campo = formato.getPath().isEmpty() ? "" : formato.getPath().get(0).getFieldName();
            Class<?> tipo = formato.getTargetType();
            if (tipo != null && (Number.class.isAssignableFrom(tipo) || tipo.isPrimitive())) {
                String texto = (Integer.class == tipo || Long.class == tipo || int.class == tipo || long.class == tipo)
                        ? "Error: solo se permiten numeros enteros."
                        : "Error: solo se permiten numeros.";
                return badRequest("DATOS_INVALIDOS", texto + (campo.isEmpty() ? "" : " (campo: " + campo + ")"));
            }
        }
        return badRequest("DATOS_INVALIDOS", "Los datos enviados no tienen el formato correcto.");
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleArchivoInvalido(ArchivoInvalidoException ex) {
        return badRequest("ARCHIVO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        return badRequest("ARCHIVO_INVALIDO", "El archivo excede el tamaño máximo permitido (10MB).");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return badRequest("SOLICITUD_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return badRequest("ESTADO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(CambioSueldoPendienteException.class)
    public ResponseEntity<Map<String, Object>> handleCambioSueldoPendiente(CambioSueldoPendienteException ex) {
        return badRequest("CAMBIO_SUELDO_PENDIENTE", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "ACCESO_DENEGADO");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler({NullPointerException.class, NumberFormatException.class})
    public ResponseEntity<Map<String, Object>> handleBadInput(Exception ex) {
        return badRequest("DATOS_INVALIDOS", "Datos de entrada invalidos o faltantes.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return badRequest("ERROR_DE_NEGOCIO", ex.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleDatabase(SQLException ex) {
        log.error("Error de conexion con la base de datos", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "ERROR_BASE_DE_DATOS");
        response.put("message", "Error de conexion con la base de datos. Intente mas tarde.");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Error interno del servidor", ex);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "ERROR_INTERNO");
        response.put("message", "Error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<Map<String, Object>> badRequest(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }
}
