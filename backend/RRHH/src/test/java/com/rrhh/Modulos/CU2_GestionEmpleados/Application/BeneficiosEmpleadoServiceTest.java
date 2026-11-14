package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.FamiliarRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.PerfilNominaRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.BeneficiosEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoDerechoHabientesRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;
import com.rrhh.Shared.persistence.EmpleadoDerechoHabientesModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BeneficiosEmpleadoServiceTest {

    private JpaEmpleadoRepository empleados;
    private JpaEmpleadoDerechoHabientesRepository perfiles;
    private JpaFamiliaInfoRepository familiares;
    private BeneficiosEmpleadoService service;

    @BeforeEach
    void setUp() {
        empleados = mock(JpaEmpleadoRepository.class);
        perfiles = mock(JpaEmpleadoDerechoHabientesRepository.class);
        familiares = mock(JpaFamiliaInfoRepository.class);
        service = new BeneficiosEmpleadoService(empleados, perfiles, familiares);
    }

    @Test
    @DisplayName("Obtener perfil y familiares de un empleado existente")
    void obtenerBeneficios_EmpleadoExistente() {
        EmpleadoDerechoHabientesModel perfil = new EmpleadoDerechoHabientesModel();
        perfil.setRegimenPension("ONP");
        perfil.setRegimenSalud("ESSALUD");

        FamiliaInfoModel fam = new FamiliaInfoModel();
        fam.setIdFamiliaInfo(1);
        fam.setNombres("Carlos");
        fam.setParentesco("HIJO");

        when(perfiles.findByEmpleadoIdEmpleado(1L)).thenReturn(Optional.of(perfil));
        when(familiares.findByEmpleadoIdEmpleadoAndActivoTrue(1L)).thenReturn(List.of(fam));

        Map<String, Object> result = service.obtener(1L);
        assertNotNull(result);
        assertTrue(result.containsKey("perfil"));
        assertTrue(result.containsKey("familiares"));
    }

    @Test
    @DisplayName("Guardar perfil con régimen AFP")
    void guardarPerfil_AfpExitoso() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        PerfilNominaRequestDTO dto = new PerfilNominaRequestDTO();
        dto.regimenPension = "AFP";
        dto.afp = "PRIMA";
        dto.cuspp = "1234567890";
        dto.tipoComisionAfp = "FLUJO";
        dto.regimenSalud = "ESSALUD";

        when(empleados.findById(1L)).thenReturn(Optional.of(emp));
        when(perfiles.findByEmpleadoIdEmpleado(1L)).thenReturn(Optional.empty());
        when(perfiles.save(any(EmpleadoDerechoHabientesModel.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = service.guardarPerfil(1L, dto);
        assertEquals("AFP", result.get("regimenPension"));
        assertEquals("PRIMA", result.get("afp"));
    }

    @Test
    @DisplayName("Guardar perfil lanza excepción si el empleado no existe")
    void guardarPerfil_EmpleadoNoExiste() {
        when(empleados.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () ->
                service.guardarPerfil(99L, new PerfilNominaRequestDTO())
        );
    }

    @Test
    @DisplayName("Agregar familiar exitosamente")
    void agregarFamiliar_Exitoso() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        FamiliarRequestDTO dto = new FamiliarRequestDTO();
        dto.nombres = "Mateo";
        dto.apellidos = "Perez";
        dto.parentesco = "HIJO";

        when(empleados.findById(1L)).thenReturn(Optional.of(emp));
        when(familiares.save(any(FamiliaInfoModel.class))).thenAnswer(i -> {
            FamiliaInfoModel f = i.getArgument(0);
            f.setIdFamiliaInfo(10);
            return f;
        });

        Map<String, Object> result = service.agregarFamiliar(1L, dto);
        assertEquals(10, result.get("id"));
        assertEquals("Mateo", result.get("nombres"));
    }

    @Test
    @DisplayName("Actualizar familiar existente")
    void actualizarFamiliar_Exitoso() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        FamiliaInfoModel fam = new FamiliaInfoModel();
        fam.setIdFamiliaInfo(5);
        fam.setEmpleado(emp);

        FamiliarRequestDTO dto = new FamiliarRequestDTO();
        dto.nombres = "Mateo Editado";

        when(familiares.findById(5)).thenReturn(Optional.of(fam));
        when(familiares.save(any(FamiliaInfoModel.class))).thenAnswer(i -> i.getArgument(0));

        Map<String, Object> result = service.actualizarFamiliar(1L, 5, dto);
        assertEquals("Mateo Editado", result.get("nombres"));
    }

    @Test
    @DisplayName("Desactivar familiar exitosamente")
    void desactivarFamiliar_Exitoso() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);

        FamiliaInfoModel fam = new FamiliaInfoModel();
        fam.setIdFamiliaInfo(5);
        fam.setEmpleado(emp);
        fam.setActivo(true);

        when(familiares.findById(5)).thenReturn(Optional.of(fam));

        service.desactivarFamiliar(1L, 5);
        assertFalse(fam.getActivo());
        verify(familiares).save(fam);
    }
}

