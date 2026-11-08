package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.NombreUsuarioService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NombreUsuarioServiceTest {

    private IUsuarioGestionRepository usuarioRepository;
    private NombreUsuarioService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(IUsuarioGestionRepository.class);
        service = new NombreUsuarioService(usuarioRepository);
    }

    @Test
    @DisplayName("Genera username base 1 si no existe en BD")
    void generarNombreUsuario_Base1Disponible() {
        when(usuarioRepository.existsByNombreUsuario("juan.perez")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Juan Carlos", "Perez Gomez");
        assertEquals("juan.perez", username);
    }

    @Test
    @DisplayName("Genera username base 2 si base 1 ya existe")
    void generarNombreUsuario_Base2Disponible() {
        when(usuarioRepository.existsByNombreUsuario("juan.perez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juan.perezgomez")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Juan", "Perez Gomez");
        assertEquals("juan.perezgomez", username);
    }

    @Test
    @DisplayName("Genera username base 3 si base 1 y 2 ya existen")
    void generarNombreUsuario_Base3Disponible() {
        when(usuarioRepository.existsByNombreUsuario("juan.perez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juan.perezgomez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgomez")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Juan Carlos", "Perez Gomez");
        assertEquals("juancarlos.perezgomez", username);
    }

    @Test
    @DisplayName("Genera username con sufijo numérico si todas las bases existen")
    void generarNombreUsuario_ConSufijoNumerico() {
        when(usuarioRepository.existsByNombreUsuario("juan.perez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juan.perezgomez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgomez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgomez1")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgomez2")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Juan Carlos", "Perez Gomez");
        assertEquals("juancarlos.perezgomez2", username);
    }

    @Test
    @DisplayName("Genera username con un solo nombre y apellido")
    void generarNombreUsuario_SoloUnNombreYApellido() {
        when(usuarioRepository.existsByNombreUsuario("ana.lopez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("ana.lopez1")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Ana", "Lopez");
        assertEquals("ana.lopez1", username);
    }

    @Test
    @DisplayName("Genera correo institucional único")
    void generarCorreoInstitucional_SinColision() {
        when(usuarioRepository.existsByCorreoInst("juan.perez@hospitalsangabriel.com")).thenReturn(false);

        String correo = service.generarCorreoInstitucional("juan.perez");
        assertEquals("juan.perez@hospitalsangabriel.com", correo);
    }

    @Test
    @DisplayName("Genera correo institucional con número si colisiona")
    void generarCorreoInstitucional_ConColision() {
        when(usuarioRepository.existsByCorreoInst("juan.perez@hospitalsangabriel.com")).thenReturn(true);
        when(usuarioRepository.existsByCorreoInst("juan.perez1@hospitalsangabriel.com")).thenReturn(false);

        String correo = service.generarCorreoInstitucional("juan.perez");
        assertEquals("juan.perez1@hospitalsangabriel.com", correo);
    }

    @Test
    @DisplayName("Manejo de nulos y caracteres especiales en nombres y correos")
    void manejoCaracteresEspeciales() {
        when(usuarioRepository.existsByNombreUsuario(anyString())).thenReturn(false);
        when(usuarioRepository.existsByCorreoInst(anyString())).thenReturn(false);

        String username = service.generarNombreUsuarioUnico(null, "");
        assertNotNull(username);

        String correo = service.generarCorreoInstitucional("María José Ñañez");
        assertTrue(correo.contains("@hospitalsangabriel.com"));
        assertFalse(correo.contains("ñ"));
        assertFalse(correo.contains("í"));
    }
}

