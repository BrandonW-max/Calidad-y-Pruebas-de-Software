package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.BuscarEmpleadoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuscarEmpleadoUseCaseTest {

    private IEmpleadoRepository empleadoRepository;
    private IContratoRepository contratoRepository;
    private IUsuarioGestionRepository usuarioRepository;
    private BuscarEmpleadoUseCase useCase;

    @BeforeEach
    void setUp() {
        empleadoRepository = mock(IEmpleadoRepository.class);
        contratoRepository = mock(IContratoRepository.class);
        usuarioRepository = mock(IUsuarioGestionRepository.class);
        useCase = new BuscarEmpleadoUseCase(empleadoRepository, contratoRepository, usuarioRepository);
    }

    private Empleado crearEmpleado(Long id, String nombres, String apellidos) {
        Empleado e = new Empleado();
        e.setIdEmpleado(id);
        e.setNombres(nombres);
        e.setApellidos(apellidos);
        e.setNumeroDi("12345678");
        e.setCorreo("test@correo.com");
        e.setTelefono("987654321");
        e.setEstado("ACTIVO");
        e.setDireccion("Av. Principal 123");
        return e;
    }

    @Test
    @DisplayName("Listar todos los empleados")
    void listar_Exitoso() {
        Empleado e = crearEmpleado(1L, "Juan", "Perez");
        Contrato c = new Contrato();
        c.setCargo("Analista");
        c.setSueldo(new BigDecimal("3000"));

        UsuarioGestion u = new UsuarioGestion();
        u.setNombreUsuario("juan.perez");
        u.setNombreRol("EMPLEADO");

        when(empleadoRepository.findAll()).thenReturn(List.of(e));
        when(contratoRepository.findContratoActivoByEmpleado(1L)).thenReturn(c);
        when(usuarioRepository.findByEmpleadoId(1L)).thenReturn(u);

        List<BuscarEmpleadoResponseDTO> resultado = useCase.listar();
        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombres());
        assertEquals("Analista", resultado.get(0).getCargo());
        assertEquals("juan.perez", resultado.get(0).getNombreUsuario());
    }

    @Test
    @DisplayName("Buscar por filtro simple")
    void buscarPorFiltro_Exitoso() {
        Empleado e = crearEmpleado(2L, "Maria", "Gomez");
        when(empleadoRepository.buscarPorFiltro("Maria")).thenReturn(List.of(e));

        List<BuscarEmpleadoResponseDTO> resultado = useCase.buscarPorFiltro("Maria");
        assertEquals(1, resultado.size());
        assertEquals("Maria", resultado.get(0).getNombres());
    }

    @Test
    @DisplayName("Buscar avanzado con filtros")
    void buscarAvanzado_Exitoso() {
        Empleado e = crearEmpleado(3L, "Pedro", "Castillo");
        when(empleadoRepository.buscarAvanzado("ACTIVO", 1, "Medico")).thenReturn(List.of(e));

        List<BuscarEmpleadoResponseDTO> resultado = useCase.buscarAvanzado("activo", 1, "Medico");
        assertEquals(1, resultado.size());
        assertEquals("Pedro", resultado.get(0).getNombres());
    }

    @Test
    @DisplayName("Listar y buscar con paginación")
    void metodosConPaginacion() {
        Empleado e = crearEmpleado(1L, "Ana", "Torres");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empleado> page = new PageImpl<>(List.of(e), pageable, 1);

        when(empleadoRepository.findAll(pageable)).thenReturn(page);
        when(empleadoRepository.buscarPorFiltro("Ana", pageable)).thenReturn(page);
        when(empleadoRepository.buscarAvanzado("ACTIVO", 2, "Enfermera", pageable)).thenReturn(page);

        Page<BuscarEmpleadoResponseDTO> p1 = useCase.listar(pageable);
        assertEquals(1, p1.getTotalElements());

        Page<BuscarEmpleadoResponseDTO> p2 = useCase.buscarPorFiltro("Ana", pageable);
        assertEquals(1, p2.getTotalElements());

        Page<BuscarEmpleadoResponseDTO> p3 = useCase.buscarAvanzado("ACTIVO", 2, "Enfermera", pageable);
        assertEquals(1, p3.getTotalElements());
    }
}

