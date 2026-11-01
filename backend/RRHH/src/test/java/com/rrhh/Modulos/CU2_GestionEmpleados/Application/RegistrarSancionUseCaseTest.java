package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.RegistrarSancionUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegistrarSancionUseCase - Validaciones de sanción (PI2-07, PI2-08)")
class RegistrarSancionUseCaseTest {

    @Mock private IEmpleadoRepository empleadoRepository;
    @Mock private ISancionRepository sancionRepository;
    @Mock private IUsuarioGestionRepository usuarioRepository;
    @Mock private IContratoRepository contratoRepository;
    @Mock private AuditoriaEmpleadoService auditoriaEmpleadoService;

    @InjectMocks
    private RegistrarSancionUseCase useCase;

    private static final Long ID_EMPLEADO = 1L;
    private static final String JUSTIFICACION = "Llegadas tardias reiteradas";

    private Empleado empleado;

    @BeforeEach
    void setUp() {
        empleado = new Empleado();
        empleado.setIdEmpleado(ID_EMPLEADO);
        empleado.setNombres("Juan");
        empleado.setApellidos("Perez");
        empleado.setEstado("ACTIVO");
    }

    private RegistrarSancionRequestDTO sancion(String tipo, Integer dias, String justificacion) {
        RegistrarSancionRequestDTO dto = new RegistrarSancionRequestDTO();
        dto.setTipoSancion(tipo);
        dto.setDiasSuspension(dias);
        dto.setJustificacion(justificacion);
        return dto;
    }

    @Test
    @DisplayName("PI2-08: SUSPENSION con 0 días → 'Los dias de suspension deben ser mayor a cero' y no guarda")
    void suspensionConCeroDias_RechazaYNoGuarda() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, sancion("SUSPENSION", 0, JUSTIFICACION)));

        assertEquals("Los dias de suspension deben ser mayor a cero", ex.getMessage());
        verify(sancionRepository, never()).save(any());
    }

    @Test
    @DisplayName("PI2-07: SUSPENSION con 2 días → 'Sanción registrada correctamente' y bloquea acceso")
    void suspensionConDosDias_RegistraYBloquea() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(usuarioRepository.findByEmpleadoId(ID_EMPLEADO)).thenReturn(new UsuarioGestion());
        when(empleadoRepository.save(any(Empleado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, sancion("SUSPENSION", 2, JUSTIFICACION));

        assertTrue(resultado.isSuccess());
        assertEquals("Sanción registrada correctamente", resultado.getMessage());
        assertEquals("SUSPENDIDO", empleado.getEstado());
        verify(sancionRepository).save(argThatBloquea(true));
    }

    @Test
    @DisplayName("AMONESTACION ignora los días de suspensión: con 0 días también se registra")
    void amonestacionConCeroDias_SeRegistra() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(sancionRepository.save(any(Sancion.class))).thenAnswer(inv -> inv.getArgument(0));

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, sancion("Amonestacion", 0, JUSTIFICACION));

        assertTrue(resultado.isSuccess());
        verify(sancionRepository).save(argThatBloquea(false));
    }

    @Test
    @DisplayName("Justificación vacía → 'La justificación es obligatoria' y no guarda")
    void justificacionVacia_RechazaYNoGuarda() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, sancion("Amonestacion", 0, "   ")));

        assertEquals("La justificación es obligatoria", ex.getMessage());
        verify(sancionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Empleado desvinculado no puede recibir sanciones")
    void empleadoDesvinculado_RechazaSancion() {
        empleado.setEstado("DESVINCULADO");
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, sancion("Amonestacion", 0, JUSTIFICACION)));

        assertEquals("Error: el empleado esta inactivo", ex.getMessage());
        verify(sancionRepository, never()).save(any());
    }

    private static Sancion argThatBloquea(boolean bloquea) {
        return org.mockito.ArgumentMatchers.argThat(s -> s != null && bloquea == Boolean.TRUE.equals(s.getBloqueaAcceso()));
    }
}
