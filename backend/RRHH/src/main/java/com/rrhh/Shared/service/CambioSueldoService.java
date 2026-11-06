package com.rrhh.Shared.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CambioSueldoService {

    @PersistenceContext
    private EntityManager em;

    // Guarda desde qué fecha rige el nuevo sueldo; no registra si el monto no cambió
    public void registrar(Long idEmpleado, LocalDate fechaEfectiva, BigDecimal sueldoAnterior,
                          BigDecimal sueldoNuevo, Long idUsuario) {
        if (sueldoNuevo == null || (sueldoAnterior != null && sueldoAnterior.compareTo(sueldoNuevo) == 0)) {
            return;
        }
        em.createNativeQuery("""
            INSERT INTO cambio_sueldo (id_empleado, fecha_efectiva, sueldo_anterior, sueldo_nuevo, registrado_por)
            VALUES (:idEmpleado, :fechaEfectiva, :sueldoAnterior, :sueldoNuevo, :idUsuario)
            """)
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("fechaEfectiva", fechaEfectiva)
            .setParameter("sueldoAnterior", sueldoAnterior)
            .setParameter("sueldoNuevo", sueldoNuevo)
            .setParameter("idUsuario", idUsuario)
            .executeUpdate();
    }

    // Hay un cambio que rige después del inicio del período: el período no puede calcularse sin confirmación
    public boolean tieneCambioPosteriorAl(Long idEmpleado, LocalDate inicioPeriodo) {
        Number total = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM cambio_sueldo WHERE id_empleado = :idEmpleado AND fecha_efectiva > :inicio")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("inicio", inicioPeriodo)
            .getSingleResult();
        return total.longValue() > 0;
    }
}
