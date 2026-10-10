package com.rrhh.Modulos.CU3_Nomina;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.factory.NominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.services.NominaService;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.export.NominaComprobantePDFGenerator;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS – CU3: Cálculo de Nómina (NominaService)
 * Tipos: Unitarias, de Límite
 * Framework: JUnit 5 + Mockito (incluido en spring-boot-starter-test 3.5.0)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU3 - NominaService: Cálculo y Upsert de Nómina")
class NominaServiceUpsertTest {

    @Mock INominaRepository nominaRepository;
    @Mock IDetalleNominaRepository detalleNominaRepository;
    @Mock IHistorialRepository historialRepository;
    @Mock NominaComprobantePDFGenerator comprobantePDFGenerator;
    @Mock JpaEmpleadoRepository jpaEmpleadoRepository;
    @Mock JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    @Mock JpaContratoRepository jpaContratoRepository;
    @Mock JpaFamiliaInfoRepository jpaFamiliaInfoRepository;

    NominaService service;

    private static final Long EMPLEADO_ID = 1L;
    private static final Long USUARIO_ID  = 99L;

    private static final LocalDate INICIO_ABRIL = LocalDate.of(2025, 4, 1);
    private static final LocalDate FIN_ABRIL    = LocalDate.of(2025, 4, 30);
    private static final String PERIODO_ABRIL   = "2025-04";

    private static final LocalDate INICIO_MAYO = LocalDate.of(2025, 5, 1);
    private static final LocalDate FIN_MAYO    = LocalDate.of(2025, 5, 31);
    private static final String PERIODO_MAYO   = "2025-05";

    @BeforeEach
    void setUp() {
        RRHHProperties props = new RRHHProperties();
        props.setSalarioMinimo(new BigDecimal("1025.00"));
        props.setTasaHoraExtra1_5(new BigDecimal("1.5"));
        props.setTasaHoraExtra2_0(new BigDecimal("2.0"));
        props.setUmbralHorasDobles(2);
        props.setRmv(new BigDecimal("1025.00"));
        props.setBonifFamiliar(new BigDecimal("0.10"));
        props.setBonifNocturno(new BigDecimal("0.35"));
        props.setBonifGuardia(new BigDecimal("10.00"));
        props.setBonifRiesgo(new BigDecimal("0.10"));
        props.setBonifCargo(new BigDecimal("0.10"));
        props.setTasaDescuentoLey(new BigDecimal("0.13"));
        props.setTasaDescuentoAfp(new BigDecimal("0.10"));

        service = new NominaService(
                props,
                nominaRepository,
                detalleNominaRepository,
                historialRepository,
                new NominaFactory(),
                comprobantePDFGenerator,
                jpaEmpleadoRepository,
                jpaRegistroAsistenciaRepository,
                jpaContratoRepository,
                jpaFamiliaInfoRepository
        );
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private EmpleadoModel empleadoActivo() {
        EmpleadoModel e = new EmpleadoModel();
        e.setIdEmpleado(EMPLEADO_ID);
        e.setNombres("Juan");
        e.setApellidos("Perez");
        e.setEstado("ACTIVO");
        return e;
    }

    private ContratoModel contratoActivo() {
        ContratoModel c = new ContratoModel();
        c.setSueldo(new BigDecimal("2000.00"));
        c.setTipoPension("ONP");
        return c;
    }

    private RegistroAsistenciaModel registroNormal(LocalDate fecha) {
        RegistroAsistenciaModel r = new RegistroAsistenciaModel();
        r.setFecha(fecha);
        r.setHoraEntrada(LocalTime.of(8, 0));
        r.setHoraSalida(LocalTime.of(17, 0));
        r.setMinutosTardanza(0);
        r.setMinutosExtra(0);
        r.setTipoRegistro("ORIGINAL");
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(EMPLEADO_ID);
        r.setEmpleado(emp);
        return r;
    }

    /**
     * Configura mocks comunes para calcularNomina en un período dado.
     * Usa findByEmpleadoIdEmpleadoInAndFechaBetween (consulta bulk) y
     * findByEmpleadoIdEmpleadoInAndActivoTrue (familia bulk), conforme al código productivo.
     */
    private void configurarMocksCalculo(LocalDate inicio, LocalDate fin) {
        EmpleadoModel emp = empleadoActivo();
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(emp));
        // El servicio usa la versión bulk (In)
        when(jpaRegistroAsistenciaRepository
                .findByEmpleadoIdEmpleadoInAndFechaBetween(anyList(), eq(inicio), eq(fin)))
                .thenReturn(List.of(registroNormal(inicio)));
        when(jpaContratoRepository
                .findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
                .thenReturn(List.of(contratoActivoConEmpleado()));
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(anyList()))
                .thenReturn(List.of());
        // historialRepository.save es void → no necesita doNothing (lenient)
    }

    /** ContratoModel con referencia al empleado para que el groupingBy funcione */
    private ContratoModel contratoActivoConEmpleado() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(EMPLEADO_ID);
        ContratoModel c = new ContratoModel();
        c.setSueldo(new BigDecimal("2000.00"));
        c.setTipoPension("ONP");
        c.setEmpleado(emp);
        return c;
    }

    /** Simula que el repositorio guarda y asigna el id dado */
    private void configurarGuardarConId(Integer idNomina) {
        when(nominaRepository.guardar(any(Nomina.class))).thenAnswer(inv -> {
            Nomina n = inv.getArgument(0);
            n.setIdNomina(idNomina);
            return n;
        });
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU3-U01: Primer cálculo → crea 1 nómina por empleado con versión 1")
    void primerCalculo_creaUnaNominaPorEmpleado_conVersion1() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL);
        configurarGuardarConId(101);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(101);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository).guardar(captor.capture());
        assertThat(captor.getValue().getVersion()).isEqualTo(1);
        assertThat(captor.getValue().getIdNomina()).isEqualTo(101L);
    }

    @Test
    @DisplayName("TC-CU3-U02: Segundo cálculo del mismo período → el servicio borra y recrea")
    void segundoCalculo_mismoPeriodo_llamaEliminarPeriodo() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL);
        configurarGuardarConId(101);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);
        service.calcularNomina(request, USUARIO_ID);

        // Verifica que se eliminaron las nóminas previas del período
        verify(nominaRepository).eliminarPorPeriodo(PERIODO_ABRIL);
    }

    @Test
    @DisplayName("TC-CU3-U03: Cálculo de período diferente → crea ID nuevo (idNomina null al guardar)")
    void calculoPeriodoDiferente_creaIdNuevoSinReutilizar() {
        configurarMocksCalculo(INICIO_MAYO, FIN_MAYO);
        configurarGuardarConId(202);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_MAYO, FIN_MAYO, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(202);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository).guardar(captor.capture());
        assertThat(captor.getValue().getIdNomina()).isEqualTo(202L);
        assertThat(captor.getValue().getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-CU3-U04: El repositorio rechaza duplicados empleado+período → lanza excepción")
    void repositorio_rechazaDuplicadoEmpleadoPeriodo_lanzaExcepcion() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL);
        when(nominaRepository.guardar(any(Nomina.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key value violates unique constraint \"uq_nomina_empleado_periodo\""));

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);

        assertThatThrownBy(() -> service.calcularNomina(request, USUARIO_ID))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("uq_nomina_empleado_periodo");
    }

    @Test
    @DisplayName("TC-CU3-U05: buscarPorPeriodo devuelve solo las nóminas del período exacto")
    void buscarPorPeriodo_devuelveSoloNominasDelPeriodoExacto() {
        Nomina nominaAbril = new Nomina();
        nominaAbril.setIdNomina(101);
        nominaAbril.setIdEmpleado(EMPLEADO_ID);

        when(nominaRepository.buscarPorPeriodo(PERIODO_ABRIL)).thenReturn(List.of(nominaAbril));
        when(jpaEmpleadoRepository.findAllById(anyList())).thenReturn(List.of(empleadoActivo()));

        List<NominaResponseDTO> resultados = service.buscarPorPeriodo(PERIODO_ABRIL);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIdNomina()).isEqualTo(101);
        verify(nominaRepository).buscarPorPeriodo(PERIODO_ABRIL);
        verify(nominaRepository, never()).buscarPorPeriodo(PERIODO_MAYO);
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE (Boundary / Edge Cases)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU3-L01: LÍMITE – fecha inicio mayor que fecha fin → IllegalArgumentException")
    void calcularNomina_fechaInicioMayorQueFin_lanzaExcepcion() {
        LocalDate inicio = LocalDate.of(2025, 4, 30);
        LocalDate fin    = LocalDate.of(2025, 4, 1);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(inicio, fin, null, null);

        assertThatThrownBy(() -> service.calcularNomina(request, USUARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha de inicio no puede ser mayor");
    }

    @Test
    @DisplayName("TC-CU3-L02: LÍMITE – período futuro → IllegalArgumentException")
    void calcularNomina_periodoFuturo_lanzaExcepcion() {
        LocalDate inicio = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        LocalDate fin    = inicio.withDayOfMonth(inicio.lengthOfMonth());

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(inicio, fin, null, null);

        assertThatThrownBy(() -> service.calcularNomina(request, USUARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("meses futuros");
    }

    @Test
    @DisplayName("TC-CU3-L03: LÍMITE – sin empleados activos → IllegalStateException")
    void calcularNomina_sinEmpleadosActivos_lanzaExcepcion() {
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of());

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);

        assertThatThrownBy(() -> service.calcularNomina(request, USUARIO_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se encontraron empleados activos");
    }

    @Test
    @DisplayName("TC-CU3-L04: LÍMITE – periodo mismo mes (inicio == fin) → cálculo válido de 1 día")
    void calcularNomina_unSoloDia_valido() {
        LocalDate diaUnico = LocalDate.of(2025, 3, 15);
        configurarMocksCalculo(diaUnico, diaUnico);
        configurarGuardarConId(50);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                diaUnico, diaUnico, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados).hasSize(1);
    }

    @Test
    @DisplayName("TC-CU3-L05: LÍMITE – nómina con 0 minutos tardanza no genera descuento")
    void calcularNomina_sinTardanzas_descuentoEsCero() {
        configurarMocksCalculo(INICIO_ABRIL, FIN_ABRIL);
        configurarGuardarConId(101);

        CalcularNominaRequestDTO request = new CalcularNominaRequestDTO(
                INICIO_ABRIL, FIN_ABRIL, null, null);
        List<NominaResponseDTO> resultados = service.calcularNomina(request, USUARIO_ID);

        assertThat(resultados.get(0).getDescuentoTardanzas())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
