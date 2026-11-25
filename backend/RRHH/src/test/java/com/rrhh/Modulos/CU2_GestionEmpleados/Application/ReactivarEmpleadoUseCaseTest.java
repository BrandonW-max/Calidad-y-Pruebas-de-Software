package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.ReactivarEmpleadoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactivarEmpleadoUseCase - RF32: reinicio de 2FA al reactivar")
class ReactivarEmpleadoUseCaseTest {

    private static final Long ID_EMPLEADO = 7L;

    @Mock IEmpleadoRepository empleadoRepository;
    @Mock ISancionRepository sancionRepository;
    @Mock IUsuarioGestionRepository usuarioRepository;
    @Mock IContratoRepository contratoRepository;
    @Mock AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock JpaUsuarioRepository jpaUsuarioRepository;

    @Test
    @DisplayName("Reactivar empleado reinicia su 2FA: desactivado, sin secreto, sin intentos ni bloqueo")
    void reactivar_ReiniciaConfiguracion2FA() {
        ReactivarEmpleadoUseCase useCase = new ReactivarEmpleadoUseCase(
                empleadoRepository, sancionRepository, usuarioRepository, contratoRepository,
                auditoriaEmpleadoService, jpaUsuarioRepository);

        Empleado empleado = new Empleado();
        empleado.setIdEmpleado(ID_EMPLEADO);
        empleado.setEstado("DESVINCULADO");

        UsuarioModel usuario = new UsuarioModel();
        usuario.setDosfaActivo(true);
        usuario.setDosfaSecret("SECRETO");
        usuario.setIntentosFalla2fa(3);
        usuario.setBloqueo2faHasta(LocalDateTime.now().plusMinutes(5));

        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sancionRepository.findActivasConBloqueoByEmpleado(ID_EMPLEADO)).thenReturn(List.of());
        when(usuarioRepository.findByEmpleadoId(ID_EMPLEADO)).thenReturn(new UsuarioGestion());
        when(jpaUsuarioRepository.findByEmpleadoIdEmpleado(ID_EMPLEADO)).thenReturn(Optional.of(usuario));

        useCase.ejecutar(ID_EMPLEADO, null);

        assertFalse(usuario.getDosfaActivo());
        assertNull(usuario.getDosfaSecret());
        assertEquals(0, usuario.getIntentosFalla2fa());
        assertNull(usuario.getBloqueo2faHasta());
        verify(jpaUsuarioRepository).save(usuario);
    }
}
