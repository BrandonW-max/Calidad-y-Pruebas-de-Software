package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.DesactivarEmpleadoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DesactivarEmpleadoUseCaseTest {

    private IEmpleadoRepository empleadoRepository;
    private IContratoRepository contratoRepository;
    private IUsuarioGestionRepository usuarioRepository;
    private AuditoriaEmpleadoService auditoriaEmpleadoService;
    private JpaUsuarioRepository jpaUsuarioRepository;
    private DesactivarEmpleadoUseCase useCase;

    @BeforeEach
    void setUp() {
        empleadoRepository = mock(IEmpleadoRepository.class);
        contratoRepository = mock(IContratoRepository.class);
        usuarioRepository = mock(IUsuarioGestionRepository.class);
        auditoriaEmpleadoService = mock(AuditoriaEmpleadoService.class);
        jpaUsuarioRepository = mock(JpaUsuarioRepository.class);

        useCase = new DesactivarEmpleadoUseCase(
                empleadoRepository, contratoRepository, usuarioRepository,
                auditoriaEmpleadoService, jpaUsuarioRepository
        );
    }

    @Test
    @DisplayName("Desactivar empleado activo exitosamente")
    void desactivarEmpleado_Exitoso() {
        Empleado emp = new Empleado();
        emp.setIdEmpleado(10L);
        emp.setNombres("Luis");
        emp.setApellidos("Ramos");
        emp.setEstado("ACTIVO");

        UsuarioGestion usuario = new UsuarioGestion();
        usuario.setActivo(true);
        usuario.setNombreUsuario("luis.ramos");

        UsuarioModel usuarioModel = new UsuarioModel();

        Contrato contrato = new Contrato();
        contrato.setEstado("ACTIVO");
        contrato.setCargo("Enfermero");

        when(empleadoRepository.findById(10L)).thenReturn(emp);
        when(empleadoRepository.save(any())).thenReturn(emp);
        when(usuarioRepository.findByEmpleadoId(10L)).thenReturn(usuario);
        when(jpaUsuarioRepository.findByEmpleadoIdEmpleado(10L)).thenReturn(Optional.of(usuarioModel));
        when(contratoRepository.findContratoActivoByEmpleado(10L)).thenReturn(contrato);

        DesactivarEmpleadoRequestDTO dto = new DesactivarEmpleadoRequestDTO();
        dto.setMotivo("Renuncia voluntaria");

        EmpleadoResponseDTO res = useCase.ejecutar(10L, dto);

        assertNotNull(res);
        assertTrue(res.isSuccess());
        assertEquals("DESVINCULADO", emp.getEstado());
        verify(empleadoRepository).save(emp);
        verify(usuarioRepository).save(usuario);
        verify(contratoRepository).save(contrato);
        verify(auditoriaEmpleadoService).registrar(eq(10L), any(), any(), any(), contains("Renuncia voluntaria"));
    }

    @Test
    @DisplayName("Desactivar empleado no existente lanza excepción")
    void desactivarEmpleado_NoExiste() {
        when(empleadoRepository.findById(99L)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class, () ->
                useCase.ejecutar(99L, new DesactivarEmpleadoRequestDTO())
        );
    }

    @Test
    @DisplayName("Desactivar empleado ya inactivo lanza excepción")
    void desactivarEmpleado_YaInactivo() {
        Empleado emp = new Empleado();
        emp.setIdEmpleado(10L);
        emp.setEstado("DESVINCULADO");

        when(empleadoRepository.findById(10L)).thenReturn(emp);

        assertThrows(IllegalStateException.class, () ->
                useCase.ejecutar(10L, new DesactivarEmpleadoRequestDTO())
        );
    }
}

