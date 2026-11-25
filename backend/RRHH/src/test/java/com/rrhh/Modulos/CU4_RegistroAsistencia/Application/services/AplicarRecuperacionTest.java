package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("AsistenciaService.aplicarRecuperacion - varios planes pendientes a la vez")
class AplicarRecuperacionTest {

    private static final Long ID_EMPLEADO = 1L;
    private static final LocalDate FECHA_SALIDA = LocalDate.of(2026, 10, 20);

    private AsistenciaService servicio;
    private Query query;

    @BeforeEach
    void setUp() throws Exception {
        servicio = new AsistenciaService(
                mock(JpaRegistroAsistenciaNominaRepository.class),
                mock(JpaEmpleadoRepository.class),
                mock(IHistorialRepository.class));
        EntityManager em = mock(EntityManager.class);
        query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        var campo = AsistenciaService.class.getDeclaredField("em");
        campo.setAccessible(true);
        campo.set(servicio, em);
    }

    private void planesPendientes(Object[]... planes) {
        when(query.getResultList()).thenReturn(new ArrayList<Object>(Arrays.asList(planes)));
    }

    private int aplicar(int minutosDespuesDeSalida) throws Exception {
        Method metodo = AsistenciaService.class.getDeclaredMethod(
                "aplicarRecuperacion", Long.class, LocalDate.class, int.class);
        metodo.setAccessible(true);
        return (int) metodo.invoke(servicio, ID_EMPLEADO, FECHA_SALIDA, minutosDespuesDeSalida);
    }

    @Test
    @DisplayName("Un solo plan: los minutos se descuentan del plan hasta agotarlo")
    void unSoloPlan_SeDescuentaHastaAgotarlo() throws Exception {
        planesPendientes(new Object[]{1, 30, "TARDANZA"});

        assertEquals(30, aplicar(90));
    }

    @Test
    @DisplayName("Dos planes de tardanza: los 90 minutos cubren 30 + 60 y no quedan horas extra")
    void dosPlanesTardanza_CubrenAmbos() throws Exception {
        planesPendientes(
                new Object[]{1, 30, "TARDANZA"},
                new Object[]{2, 60, "TARDANZA"});

        // Lo esperado: el tiempo después de la salida cubre primero el plan más antiguo y luego el siguiente
        assertEquals(90, aplicar(90));
    }
}
