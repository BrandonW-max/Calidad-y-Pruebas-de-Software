package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.CierreJornadaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CierreJornadaServiceTest {

    private JpaRegistroAsistenciaNominaRepository repository;
    private JpaEmpleadoRepository empleadoRepository;
    private EntityManager em;
    private CierreJornadaService service;

    @BeforeEach
    void setUp() {
        repository = mock(JpaRegistroAsistenciaNominaRepository.class);
        empleadoRepository = mock(JpaEmpleadoRepository.class);
        em = mock(EntityManager.class);

        service = new CierreJornadaService(repository, empleadoRepository);
        ReflectionTestUtils.setField(service, "em", em);
    }

    private void mockHorarioVigente(LocalTime salida) {
        TypedQuery<AsignacionHorarioModel> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(AsignacionHorarioModel.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);

        HorarioModel h = new HorarioModel();
        h.setHoraSalida(salida);

        AsignacionHorarioModel asig = new AsignacionHorarioModel();
        asig.setHorario(h);

        when(query.getResultList()).thenReturn(List.of(asig));
    }

    @Test
    @DisplayName("Cerrar jornadas abiertas al final del día")
    void cerrarJornadasAbiertas_Exitoso() {
        mockHorarioVigente(LocalTime.of(18, 0));

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        RegistroAsistenciaModel reg = new RegistroAsistenciaModel();
        reg.setEmpleado(emp);
        reg.setHoraEntrada(LocalTime.of(9, 0));

        when(repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(List.of(reg));
        when(empleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(emp));
        when(repository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(eq(1), any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(Optional.of(reg));

        service.cerrarJornadasAbiertas();

        assertEquals(LocalTime.of(18, 0), reg.getHoraSalida());
        assertEquals("CIERRE_AUTOMATICO", reg.getTipoUltimoRegistro());
        verify(repository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    @DisplayName("Registrar inasistencias cuando empleado no marcó entrada")
    void registrarInasistencias_CuandoNoMarcoEntrada() {
        mockHorarioVigente(LocalTime.of(17, 0));

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(2L);

        when(repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(List.of());
        when(empleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(emp));
        when(repository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(eq(2), any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(Optional.empty());

        service.cerrarJornadasAbiertas();

        verify(repository).saveAll(argThat(list -> {
            List<RegistroAsistenciaModel> regs = (List<RegistroAsistenciaModel>) list;
            return regs.stream().anyMatch(r -> "INASISTENCIA".equals(r.getEstado()));
        }));
    }

    @Test
    @DisplayName("Auto salida por horario si pasaron 15 minutos")
    void autoSalidaPorHorario_Exitoso() {
        mockHorarioVigente(LocalTime.of(8, 0)); // límite 8:15, ahora siempre es posterior

        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        RegistroAsistenciaModel reg = new RegistroAsistenciaModel();
        reg.setEmpleado(emp);
        reg.setHoraEntrada(LocalTime.of(7, 0));

        when(repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(List.of(reg));

        service.autoSalidaPorHorario();

        if (LocalTime.now().isAfter(LocalTime.of(8, 15))) {
            assertEquals(LocalTime.of(8, 0), reg.getHoraSalida());
            assertEquals("SALIDA_AUTOMATICA", reg.getTipoUltimoRegistro());
            verify(repository).saveAll(anyList());
        }
    }
}

