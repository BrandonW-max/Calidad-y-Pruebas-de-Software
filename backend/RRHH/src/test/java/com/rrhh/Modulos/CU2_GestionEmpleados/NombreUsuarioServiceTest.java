package com.rrhh.Modulos.CU2_GestionEmpleados;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.NombreUsuarioService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * PRUEBAS UNITARIAS Y DE LÍMITE – CU2: Generación de Nombre de Usuario
 * Framework: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU2 - NombreUsuarioService: Generación de usuario e-mail institucional")
class NombreUsuarioServiceTest {

    @Mock
    IUsuarioGestionRepository usuarioRepository;

    NombreUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new NombreUsuarioService(usuarioRepository);
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-U11: Nombre usuario simple – primer intento disponible")
    void generarNombreUsuario_primerIntentoDisponible() {
        when(usuarioRepository.existsByNombreUsuario("carlos.gomez")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Carlos", "Gomez");

        assertThat(username).isEqualTo("carlos.gomez");
    }

    @Test
    @DisplayName("TC-CU2-U12: Nombre usuario con segundo apellido cuando primer combo está tomado")
    void generarNombreUsuario_conSegundoApellido_cuandoPrimeroOcupado() {
        when(usuarioRepository.existsByNombreUsuario("carlos.gomez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("carlos.gomezramos")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Carlos", "Gomez Ramos");

        assertThat(username).isEqualTo("carlos.gomezramos");
    }

    @Test
    @DisplayName("TC-CU2-U13: Generación de correo institucional a partir del nombre de usuario")
    void generarCorreoInstitucional_valido() {
        when(usuarioRepository.existsByCorreoInst("carlos.gomez@hospitalsangabriel.com")).thenReturn(false);

        String correo = service.generarCorreoInstitucional("carlos.gomez");

        assertThat(correo).isEqualTo("carlos.gomez@hospitalsangabriel.com");
    }

    @Test
    @DisplayName("TC-CU2-U14: Correo duplicado → agrega sufijo numérico")
    void generarCorreoInstitucional_duplicado_agregaSufijo() {
        when(usuarioRepository.existsByCorreoInst("carlos.gomez@hospitalsangabriel.com")).thenReturn(true);
        when(usuarioRepository.existsByCorreoInst("carlos.gomez1@hospitalsangabriel.com")).thenReturn(false);

        String correo = service.generarCorreoInstitucional("carlos.gomez");

        assertThat(correo).isEqualTo("carlos.gomez1@hospitalsangabriel.com");
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-L07: LÍMITE – nombre con una sola palabra → solo primer nombre")
    void generarNombreUsuario_soloUnNombre() {
        when(usuarioRepository.existsByNombreUsuario("ana.perez")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Ana", "Perez");

        assertThat(username).isEqualTo("ana.perez");
    }

    @Test
    @DisplayName("TC-CU2-L08: LÍMITE – nombre con tildes → se limpian para el correo")
    void generarCorreoInstitucional_nombreConTildes_seLimpian() {
        when(usuarioRepository.existsByCorreoInst(anyString())).thenReturn(false);

        String correo = service.generarCorreoInstitucional("maría.lópez");

        assertThat(correo).doesNotContain("á").doesNotContain("ó");
        assertThat(correo).endsWith("@hospitalsangabriel.com");
    }

    @Test
    @DisplayName("TC-CU2-L09: LÍMITE – todos los usernames base tomados → añade contador")
    void generarNombreUsuario_todosTomados_usaContador() {
        when(usuarioRepository.existsByNombreUsuario("juan.perez")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juan.perezgarcia")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgarcia")).thenReturn(true);
        when(usuarioRepository.existsByNombreUsuario("juancarlos.perezgarcia1")).thenReturn(false);

        String username = service.generarNombreUsuarioUnico("Juan Carlos", "Perez Garcia");

        assertThat(username).isEqualTo("juancarlos.perezgarcia1");
    }
}
