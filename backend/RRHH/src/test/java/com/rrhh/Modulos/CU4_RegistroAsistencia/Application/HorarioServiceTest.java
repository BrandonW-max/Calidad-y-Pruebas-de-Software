package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HorarioServiceTest {

    private EntityManager em;
    private HorarioService service;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        service = new HorarioService();
        ReflectionTestUtils.setField(service, "em", em);
    }

    @Test
    @DisplayName("Listar horarios activos")
    void listar_Exitoso() {
        TypedQuery<HorarioModel> query = mock(TypedQuery.class);
        HorarioModel h = new HorarioModel();
        h.setIdHorario(1);
        h.setNombreTurno("Mañana");

        when(em.createQuery(anyString(), eq(HorarioModel.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(h));

        List<HorarioModel> resultado = service.listar();
        assertEquals(1, resultado.size());
        assertEquals("Mañana", resultado.get(0).getNombreTurno());
    }

    @Test
    @DisplayName("Buscar horario por ID")
    void buscarPorId_Exitoso() {
        HorarioModel h = new HorarioModel();
        h.setIdHorario(1);
        when(em.find(HorarioModel.class, 1)).thenReturn(h);

        HorarioModel resultado = service.buscarPorId(1);
        assertNotNull(resultado);
        assertEquals(1, resultado.getIdHorario());
    }

    @Test
    @DisplayName("Guardar nuevo horario")
    void guardar_NuevoHorario() {
        Horario dto = new Horario();
        dto.setNombreTurno("Tarde");
        dto.setHoraEntrada(LocalTime.of(14, 0));
        dto.setHoraSalida(LocalTime.of(22, 0));

        when(em.merge(any(HorarioModel.class))).thenAnswer(i -> {
            HorarioModel m = i.getArgument(0);
            m.setIdHorario(2);
            return m;
        });

        HorarioModel resultado = service.guardar(dto);
        assertNotNull(resultado);
        assertEquals(2, resultado.getIdHorario());
        assertEquals("Tarde", resultado.getNombreTurno());
    }

    @Test
    @DisplayName("Guardar actualizar horario existente")
    void guardar_ActualizarHorario() {
        Horario dto = new Horario();
        dto.setIdHorario(1);
        dto.setNombreTurno("Mañana Modificado");
        dto.setHoraEntrada(LocalTime.of(8, 0));
        dto.setHoraSalida(LocalTime.of(16, 0));

        HorarioModel existente = new HorarioModel();
        existente.setIdHorario(1);

        when(em.find(HorarioModel.class, 1)).thenReturn(existente);
        when(em.merge(any(HorarioModel.class))).thenAnswer(i -> i.getArgument(0));

        HorarioModel resultado = service.guardar(dto);
        assertEquals("Mañana Modificado", resultado.getNombreTurno());
    }

    @Test
    @DisplayName("Eliminar horario existente")
    void eliminar_Exitoso() {
        HorarioModel existente = new HorarioModel();
        existente.setIdHorario(1);
        existente.setActivo(true);

        when(em.find(HorarioModel.class, 1)).thenReturn(existente);
        when(em.merge(any(HorarioModel.class))).thenAnswer(i -> i.getArgument(0));

        service.eliminar(1);
        assertFalse(existente.getActivo());
    }

    @Test
    @DisplayName("Eliminar horario no encontrado lanza excepción")
    void eliminar_NoEncontrado() {
        when(em.find(HorarioModel.class, 99)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99));
    }

    @Test
    @DisplayName("Asignar horario a empleado")
    void asignarHorario_Exitoso() {
        Query updateQuery = mock(Query.class);
        when(em.createQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);
        when(updateQuery.executeUpdate()).thenReturn(1);

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(10L);

        HorarioModel hor = new HorarioModel();
        hor.setIdHorario(3);
        hor.setNombreTurno("Noche");
        hor.setHoraEntrada(LocalTime.of(22, 0));
        hor.setHoraSalida(LocalTime.of(6, 0));

        when(em.find(EmpleadoModel.class, 10L)).thenReturn(emp);
        when(em.find(HorarioModel.class, 3)).thenReturn(hor);

        Map<String, Object> result = service.asignarHorario(10L, 3, LocalDate.now(), null, false);
        assertNotNull(result);
        assertEquals(10L, result.get("idEmpleado"));
        assertEquals(3, result.get("idHorario"));
        verify(em).persist(any(AsignacionHorarioModel.class));
    }

    @Test
    @DisplayName("Listar asignaciones por empleado")
    void listarAsignacionesPorEmpleado_Exitoso() {
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        Object[] fila = new Object[]{1, 2, "Turno A", LocalTime.of(8, 0), LocalTime.of(16, 0), LocalDate.now(), null, false, true};
        when(query.getResultList()).thenReturn(Collections.singletonList(fila));

        List<Map<String, Object>> result = service.listarAsignacionesPorEmpleado(10L);
        assertEquals(1, result.size());
        assertEquals("Turno A", result.get(0).get("nombreTurno"));
    }

    @Test
    @DisplayName("Obtener empleados por horario")
    void getEmpleadosPorHorario_Exitoso() {
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        Object[] fila = new Object[]{10L, "Carlos", "Soto", LocalDate.now()};
        when(query.getResultList()).thenReturn(Collections.singletonList(fila));

        List<Map<String, Object>> result = service.getEmpleadosPorHorario(2);
        assertEquals(1, result.size());
        assertEquals("Carlos", result.get(0).get("nombres"));
    }

    @Test
    @DisplayName("Obtener resumen de asignaciones activas")
    void getAsignacionesActivasResumen_Exitoso() {
        TypedQuery<Object[]> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(Object[].class))).thenReturn(query);

        Object[] fila = new Object[]{10L, 2, "Turno B", LocalTime.of(9, 0), LocalTime.of(18, 0)};
        when(query.getResultList()).thenReturn(Collections.singletonList(fila));

        List<Map<String, Object>> result = service.getAsignacionesActivasResumen();
        assertEquals(1, result.size());
        assertEquals("Turno B", result.get(0).get("nombreTurno"));
    }
}
