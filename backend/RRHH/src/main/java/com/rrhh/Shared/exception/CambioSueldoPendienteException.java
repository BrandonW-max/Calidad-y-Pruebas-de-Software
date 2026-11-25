package com.rrhh.Shared.exception;

// El período incluye un cambio de sueldo posterior a su inicio: RRHH debe confirmar para calcularlo
public class CambioSueldoPendienteException extends RuntimeException {

    public CambioSueldoPendienteException(String message) {
        super(message);
    }
}
