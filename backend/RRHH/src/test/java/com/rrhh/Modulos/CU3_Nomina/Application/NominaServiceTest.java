package com.rrhh.Modulos.CU3_Nomina.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaVersionDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.factory.INominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.services.NominaService;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.export.NominaComprobantePDFGenerator;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaNominaVersionRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaAjusteNominaManualRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import com.rrhh.Shared.persistence.RolModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.AjusteNominaManualModel;
import com.rrhh.Shared.persistence.NominaVersionModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("NominaService - Cálculo y ajuste de nómina (RF17, RF20)")
class NominaServiceTest {

    @Mock private RRHHProperties rrhhProperties;
    @Mock private INominaRepository nominaRepository;
    @Mock private IDetalleNominaRepository detalleNominaRepository;
    @Mock private IHistorialRepository historialRepository;
    @Mock private INominaFactory nominaFactory;
    @Mock private NominaComprobantePDFGenerator comprobantePDFGenerator;
    @Mock private JpaEmpleadoRepository jpaEmpleadoRepository;
    @Mock private JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    @Mock private JpaContratoRepository jpaContratoRepository;
    @Mock private JpaFamiliaInfoRepository jpaFamiliaInfoRepository;
    @Mock private JpaUsuarioRepository jpaUsuarioRepository;
    @Mock private JpaNominaVersionRepository jpaNominaVersionRepository;
    @Mock private JpaAjusteNominaManualRepository jpaAjusteNominaManualRepository;
    @Mock private com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaNominaVersionAnteriorRepository jpaNominaVersionAnteriorRepository;
    @Mock private com.rrhh.Modulos.CU3_Nomina.Application.services.LiquidacionHorasExtraService liquidacionHorasExtra;
    @Mock private com.rrhh.Shared.service.CambioSueldoService cambioSueldo;

    @InjectMocks
    private NominaService nominaService;

    private static final Long   ID_USUARIO_RRHH = 1L;
    private static final Integer ID_NOMINA      = 10;

    private static final BigDecimal SUELDO_MINIMO = new BigDecimal("1130.00");
    private static final BigDecimal SUELDO_VALIDO = new BigDecimal("2500.00");

    private static final String MSG_FECHA_INVALIDA   = "La fecha de inicio no puede ser mayor a la fecha fin.";
    private static final String MSG_MES_FUTURO       = "No se puede calcular la nómina de meses futuros.";
    private static final String MSG_SIN_EMPLEADOS    =
            "No se encontraron empleados activos para el periodo seleccionado. " +
            "No es posible realizar el calculo de nomina.";
    private static final String MSG_SUELDO_CERO      = "El sueldo base debe ser mayor a cero.";
    private static final String MSG_SUELDO_MINIMO    = "El sueldo base debe ser mayor o igual al salario minimo legal";

    @BeforeEach
    void setUp() {
        when(rrhhProperties.getSalarioMinimo()).thenReturn(SUELDO_MINIMO);
        when(rrhhProperties.getTasaDescuentoLey()).thenReturn(new BigDecimal("0.13"));
        when(rrhhProperties.getTasaDescuentoAfp()).thenReturn(new BigDecimal("0.10"));
        when(rrhhProperties.getBonifFamiliar()).thenReturn(new BigDecimal("0.10"));
        when(rrhhProperties.getBonifNocturno()).thenReturn(new BigDecimal("0.15"));
        when(rrhhProperties.getBonifGuardia()).thenReturn(new BigDecimal("15.00"));
        when(rrhhProperties.getBonifRiesgo()).thenReturn(new BigDecimal("0.05"));
        when(rrhhProperties.getBonifCargo()).thenReturn(new BigDecimal("0.20"));
        when(rrhhProperties.getRmv()).thenReturn(new BigDecimal("1130.00"));
        when(rrhhProperties.getTasaHoraExtra1_5()).thenReturn(new BigDecimal("1.5"));
        when(rrhhProperties.getTasaHoraExtra2_0()).thenReturn(new BigDecimal("2.0"));
        when(rrhhProperties.getUmbralHorasDobles()).thenReturn(2);
        when(liquidacionHorasExtra.horasTotales(any(), any())).thenReturn(BigDecimal.ZERO);
        when(liquidacionHorasExtra.horasAsistencia(any(), any())).thenReturn(BigDecimal.ZERO);
    }

    // -----------------------------------------------------------------------
    // Test 36 — RF20: fechaInicio > fechaFin → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 36 — fechaInicio posterior a fechaFin → IllegalArgumentException")
    void fechaInicioMayorFechaFin_LanzaExcepcion() {
        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 2, 1),
                null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));

        assertEquals(MSG_FECHA_INVALIDA, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 37 — RF20: mes futuro → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 37 — Período en mes futuro → IllegalArgumentException")
    void mesFuturo_LanzaExcepcion() {
        LocalDate futuro = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(
                futuro, futuro.plusDays(27), null, null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));

        assertEquals(MSG_MES_FUTURO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 38 — RF20: sin empleados activos → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 38 — Sin empleados activos en el período → IllegalStateException")
    void sinEmpleadosActivos_LanzaExcepcion() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        LocalDate fin    = inicio.plusDays(27);
        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);

        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));

        assertTrue(ex.getMessage().contains("No se encontraron empleados activos"));
    }

    @Test
    @DisplayName("Periodo anterior a la contratación del empleado → IllegalStateException")
    void periodoAnteriorAContratacion_LanzaExcepcion() {
        LocalDate inicio = LocalDate.now().minusMonths(3).withDayOfMonth(1);
        LocalDate fin    = inicio.plusDays(27);

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(9L);
        empleado.setEstado("ACTIVO");
        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setFechaInicio(LocalDate.now().minusMonths(1).withDayOfMonth(1));
        empleado.setContratos(List.of(contrato));

        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));

        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));

        assertTrue(ex.getMessage().contains("No se encontraron empleados activos"));
    }

    // -----------------------------------------------------------------------
    // Test 39 — RF17: nómina no encontrada → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 39 — Nómina no encontrada al ajustar → RecursoNoEncontradoException")
    void ajuste_NominaNoExiste_LanzaExcepcion() {
        when(nominaRepository.buscarPorId(999)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> nominaService.ajustarNomina(999, new AjustarNominaRequestDTO(), ID_USUARIO_RRHH));
    }

    // -----------------------------------------------------------------------
    // Test 40 — RF17: sueldo = 0 → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 40 — Sueldo base igual a cero → IllegalArgumentException")
    void ajuste_SueldoMenorOIgualCero_LanzaExcepcion() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setIdEmpleado(1L);
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));
        ContratoModel contrato = new ContratoModel();
        contrato.setSueldo(BigDecimal.ZERO);
        when(jpaContratoRepository.findFirstByEmpleadoIdEmpleadoAndEstado(eq(1L), eq("ACTIVO")))
                .thenReturn(Optional.of(contrato));

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH));

        assertEquals(MSG_SUELDO_CERO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 41 — RF17: sueldo < mínimo → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 41 — Sueldo base menor al mínimo legal → IllegalArgumentException")
    void ajuste_SueldoMenorAlMinimo_LanzaExcepcion() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setIdEmpleado(1L);
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));
        ContratoModel contrato = new ContratoModel();
        contrato.setSueldo(new BigDecimal("500.00"));
        when(jpaContratoRepository.findFirstByEmpleadoIdEmpleadoAndEstado(eq(1L), eq("ACTIVO")))
                .thenReturn(Optional.of(contrato));

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH));

        assertTrue(ex.getMessage().contains(MSG_SUELDO_MINIMO));
    }

    // -----------------------------------------------------------------------
    // Test 42 — RF17: ajuste válido → estadoPago = AJUSTADA
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 42 — Ajuste válido → nómina con estadoPago=AJUSTADA")
    void ajuste_Valido_EstadoAJUSTADA() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setIdEmpleado(1L);
        nomina.setVersion(1);
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));

        ContratoModel contrato = new ContratoModel();
        contrato.setSueldo(new BigDecimal("2000.00"));
        contrato.setTipoPension("ONP");
        when(jpaContratoRepository.findFirstByEmpleadoIdEmpleadoAndEstado(eq(1L), eq("ACTIVO")))
                .thenReturn(Optional.of(contrato));

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(1L);
        empleado.setNombres("Juan");
        empleado.setApellidos("Perez");
        when(jpaEmpleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));

        Nomina nominaGuardada = nominaConId(ID_NOMINA);
        nominaGuardada.setIdEmpleado(1L);
        nominaGuardada.setEstadoPago("AJUSTADA");
        nominaGuardada.setVersion(2);
        when(nominaRepository.guardar(any())).thenReturn(nominaGuardada);

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();
        req.setSueldoBase(SUELDO_VALIDO);
        req.setTipoPensionAplicada("ONP");

        NominaResponseDTO resultado = nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH);

        assertEquals("AJUSTADA", resultado.getEstadoPago());
    }

    @Test
    @DisplayName("RF45 — Ajuste guarda la versión anterior antes de sobrescribir la nómina")
    void ajuste_GuardaVersionAnteriorAntesDeSobrescribir() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setIdEmpleado(1L);
        nomina.setVersion(1);
        nomina.setSueldoNeto(new BigDecimal("1500.00"));
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));

        ContratoModel contrato = new ContratoModel();
        contrato.setSueldo(new BigDecimal("2000.00"));
        contrato.setTipoPension("ONP");
        when(jpaContratoRepository.findFirstByEmpleadoIdEmpleadoAndEstado(eq(1L), eq("ACTIVO")))
                .thenReturn(Optional.of(contrato));

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(1L);
        when(jpaEmpleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(nominaRepository.guardar(any())).thenReturn(nomina);

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();
        req.setTipoPensionAplicada("ONP");

        nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH);

        ArgumentCaptor<NominaVersionModel> captor = ArgumentCaptor.forClass(NominaVersionModel.class);
        verify(jpaNominaVersionRepository).save(captor.capture());
        NominaVersionModel anterior = captor.getValue();
        assertEquals(ID_NOMINA, anterior.getIdNomina());
        assertEquals(1, anterior.getVersion());
        assertEquals(new BigDecimal("1500.00"), anterior.getSueldoNeto());
        assertEquals(ID_USUARIO_RRHH, anterior.getReemplazadoPor());
    }

    @Test
    @DisplayName("Ajuste manual guarda sus valores para que un recálculo los reaplique")
    void ajuste_GuardaValoresManualesParaRecalculo() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setIdEmpleado(1L);
        nomina.setPeriodo("2026-05");
        nomina.setVersion(1);
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));

        ContratoModel contrato = new ContratoModel();
        contrato.setSueldo(new BigDecimal("2000.00"));
        contrato.setTipoPension("ONP");
        when(jpaContratoRepository.findFirstByEmpleadoIdEmpleadoAndEstado(eq(1L), eq("ACTIVO")))
                .thenReturn(Optional.of(contrato));

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(1L);
        when(jpaEmpleadoRepository.findById(1L)).thenReturn(Optional.of(empleado));
        when(nominaRepository.guardar(any())).thenReturn(nomina);

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();
        req.setTotalHorasExtra(new BigDecimal("4"));
        req.setTotalMinutosTardanza(15);
        req.setTipoPensionAplicada("ONP");

        nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH);

        ArgumentCaptor<AjusteNominaManualModel> captor = ArgumentCaptor.forClass(AjusteNominaManualModel.class);
        verify(jpaAjusteNominaManualRepository).save(captor.capture());
        AjusteNominaManualModel guardado = captor.getValue();
        assertEquals(1L, guardado.getIdEmpleado());
        assertEquals("2026-05", guardado.getPeriodo());
        assertEquals(0, new BigDecimal("4.00").compareTo(guardado.getTotalHorasExtra()));
        assertEquals(15, guardado.getTotalMinutosTardanza());
    }

    @Test
    @DisplayName("RF45 — Listar versiones: la vigente primero y luego las anteriores")
    void listarVersiones_VigenteYAnteriores() {
        Nomina nomina = nominaConId(ID_NOMINA);
        nomina.setVersion(2);
        nomina.setSueldoNeto(new BigDecimal("1800.00"));
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(nomina));

        NominaVersionModel anterior = new NominaVersionModel();
        anterior.setIdNomina(ID_NOMINA);
        anterior.setVersion(1);
        anterior.setSueldoNeto(new BigDecimal("1500.00"));
        when(jpaNominaVersionRepository.findByIdNominaOrderByVersionDesc(ID_NOMINA)).thenReturn(List.of(anterior));

        List<NominaVersionDTO> versiones = nominaService.listarVersiones(ID_NOMINA);

        assertEquals(2, versiones.size());
        assertEquals(2, versiones.get(0).getVersion());
        assertTrue(versiones.get(0).isVigente());
        assertEquals(1, versiones.get(1).getVersion());
        assertFalse(versiones.get(1).isVigente());
        assertEquals(new BigDecimal("1500.00"), versiones.get(1).getSueldoNeto());
    }

    // -----------------------------------------------------------------------
    // VL-CU3-19/20 — turno nocturno: entrada 19:00 vs 18:59
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("VL-CU3-19 — Entrada 19:00 → bonificación nocturna > 0")
    void turnoNocturno_EntradaA1900_ActivaBonificacion() {
        BigDecimal bono = calcularBonifNocturnaCon(LocalTime.of(19, 0));

        assertTrue(bono.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("VL-CU3-20 — Entrada 18:59 → bonificación nocturna = 0 (turno diurno)")
    void turnoDiurno_EntradaA1859_NoActivaBonificacion() {
        BigDecimal bono = calcularBonifNocturnaCon(LocalTime.of(18, 59));

        assertEquals(0, BigDecimal.ZERO.compareTo(bono));
    }

    private BigDecimal calcularBonifNocturnaCon(LocalTime horaEntrada) {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusDays(27);

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(5L);
        empleado.setNombres("Turno");
        empleado.setApellidos("Prueba");
        empleado.setEstado("ACTIVO");

        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setSueldo(SUELDO_VALIDO);
        contrato.setTipoPension("ONP");
        contrato.setEstado("ACTIVO");
        contrato.setFechaInicio(inicio.minusMonths(1));
        empleado.setContratos(List.of(contrato));

        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(inicio.plusDays(1));
        registro.setHoraEntrada(horaEntrada);
        registro.setHoraSalida(LocalTime.of(23, 0));
        registro.setTipoRegistro("ORIGINAL");
        registro.setMinutosTardanza(0);
        registro.setMinutosExtra(0);

        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));
        when(jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
            .thenReturn(List.of(contrato));
        when(jpaRegistroAsistenciaRepository.findByEmpleadoIdEmpleadoInAndFechaBetween(anyList(), any(), any()))
            .thenReturn(List.of(registro));
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(anyList())).thenReturn(List.of());
        when(nominaFactory.crearNominaBase()).thenReturn(new Nomina());
        when(nominaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);
        List<NominaResponseDTO> resultado = nominaService.calcularNomina(req, ID_USUARIO_RRHH);

        return resultado.get(0).getBonifTurnoNocturno();
    }

    // -----------------------------------------------------------------------
    // Riesgo: recalcular un período con nómina ya PAGADA no debe borrarla
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Recalcular un período con nómina PAGADA lanza excepción y no elimina nada")
    void recalculo_NominaPagada_NoSeBorra() {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusDays(27);
        String periodo = java.time.YearMonth.from(inicio).toString();

        Nomina pagada = nominaConId(77);
        pagada.setPeriodo(periodo);
        pagada.setEstadoPago("PAGADA");
        pagada.setIdEmpleado(6L);
        when(nominaRepository.buscarPorPeriodo(periodo)).thenReturn(List.of(pagada));

        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(6L);
        empleado.setNombres("Pagado");
        empleado.setApellidos("Prueba");
        empleado.setEstado("ACTIVO");

        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setSueldo(SUELDO_VALIDO);
        contrato.setTipoPension("ONP");
        contrato.setEstado("ACTIVO");
        contrato.setFechaInicio(inicio.minusMonths(1));
        empleado.setContratos(List.of(contrato));

        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));
        when(jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
            .thenReturn(List.of(contrato));
        when(jpaRegistroAsistenciaRepository.findByEmpleadoIdEmpleadoInAndFechaBetween(anyList(), any(), any()))
            .thenReturn(List.of());
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(anyList())).thenReturn(List.of());
        when(nominaFactory.crearNominaBase()).thenReturn(new Nomina());
        when(nominaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);
        assertThrows(RuntimeException.class, () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));
        verify(nominaRepository, never()).eliminarPorPeriodo(anyString());
        verify(nominaRepository, never()).guardarVersionAnterior(anyString());
    }

    @Test
    @DisplayName("Ajustar una nómina PAGADA lanza excepción y no la modifica")
    void ajuste_NominaPagada_LanzaExcepcion() {
        Nomina pagada = nominaConId(ID_NOMINA);
        pagada.setIdEmpleado(6L);
        pagada.setPeriodo("2026-05");
        pagada.setEstadoPago("PAGADA");
        when(nominaRepository.buscarPorId(ID_NOMINA)).thenReturn(Optional.of(pagada));

        AjustarNominaRequestDTO req = new AjustarNominaRequestDTO();
        req.setTotalHorasExtra(new BigDecimal("2"));
        req.setTipoPensionAplicada("ONP");

        assertThrows(IllegalStateException.class, () -> nominaService.ajustarNomina(ID_NOMINA, req, ID_USUARIO_RRHH));
        verify(nominaRepository, never()).guardar(any());
        verify(jpaAjusteNominaManualRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambio de sueldo posterior al inicio del período: no calcula sin confirmación")
    void cambioSueldoPosterior_SinConfirmacion_Bloquea() {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusDays(27);
        when(cambioSueldo.tieneCambioPosteriorAl(eq(6L), any())).thenReturn(true);
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(6L);
        empleado.setNombres("Cambio");
        empleado.setApellidos("Sueldo");
        empleado.setEstado("ACTIVO");
        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setSueldo(SUELDO_VALIDO);
        contrato.setTipoPension("ONP");
        contrato.setEstado("ACTIVO");
        contrato.setFechaInicio(inicio.minusMonths(1));
        empleado.setContratos(List.of(contrato));
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));
        when(jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
            .thenReturn(List.of(contrato));

        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);
        assertThrows(com.rrhh.Shared.exception.CambioSueldoPendienteException.class,
                () -> nominaService.calcularNomina(req, ID_USUARIO_RRHH));
        verify(nominaRepository, never()).eliminarPorPeriodo(anyString());
    }

    @Test
    @DisplayName("Cambio de sueldo posterior al inicio del período: con confirmación sí calcula")
    void cambioSueldoPosterior_ConConfirmacion_Calcula() {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusDays(27);
        when(cambioSueldo.tieneCambioPosteriorAl(eq(6L), any())).thenReturn(true);
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(6L);
        empleado.setNombres("Cambio");
        empleado.setApellidos("Sueldo");
        empleado.setEstado("ACTIVO");
        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setSueldo(SUELDO_VALIDO);
        contrato.setTipoPension("ONP");
        contrato.setEstado("ACTIVO");
        contrato.setFechaInicio(inicio.minusMonths(1));
        empleado.setContratos(List.of(contrato));
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));
        when(jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
            .thenReturn(List.of(contrato));
        when(jpaRegistroAsistenciaRepository.findByEmpleadoIdEmpleadoInAndFechaBetween(anyList(), any(), any()))
            .thenReturn(List.of());
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(anyList())).thenReturn(List.of());
        when(nominaFactory.crearNominaBase()).thenReturn(new Nomina());
        when(nominaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        CalcularNominaRequestDTO req = new CalcularNominaRequestDTO(inicio, fin, null, null);
        req.setConfirmarCambiosSueldo(true);
        assertEquals(1, nominaService.calcularNomina(req, ID_USUARIO_RRHH).size());
    }

    @Test
    @DisplayName("RNF46: horas extra por turno, 1.5x hasta el umbral y 2.0x sobre el umbral de cada turno")
    void horasExtraPorTurno_DoblanSoloSobreElUmbralDeCadaTurno() {
        Nomina sinExtras = calcularNominaDeEmpleado(List.of(), null, List.of());
        Nomina conExtras = calcularNominaDeEmpleado(List.of(new BigDecimal("10.00"), new BigDecimal("3.00")), null, List.of());

        // Valor hora 2500/30/8 = 10.4167. Turno 1: 2h×1.5 + 8h×2.0 = 19h. Turno 2: 2h×1.5 + 1h×2.0 = 5h. Total 24h = 250.00
        assertEquals(0, new BigDecimal("250.00").compareTo(conExtras.getSueldoBruto().subtract(sinExtras.getSueldoBruto())));
    }

    @Test
    @DisplayName("RNF101: bonificación por cargo solo para roles RRHH y GERENCIA")
    void bonifCargo_SoloRolesRrhhYGerencia() {
        assertTrue(calcularNominaDeEmpleado(List.of(), "RRHH", List.of()).getBonifCargo().signum() > 0);
        assertTrue(calcularNominaDeEmpleado(List.of(), "GERENCIA", List.of()).getBonifCargo().signum() > 0);
        assertEquals(0, calcularNominaDeEmpleado(List.of(), "EMPLEADO", List.of()).getBonifCargo().signum());
        assertEquals(0, calcularNominaDeEmpleado(List.of(), null, List.of()).getBonifCargo().signum());
    }

    @Test
    @DisplayName("Asignación familiar: 10% de la RMV una sola vez aunque haya varios hijos")
    void asignacionFamiliar_UnaSolaVezAunqueHayVariosHijos() {
        Nomina nomina = calcularNominaDeEmpleado(List.of(), null, List.of(hijoElegible(), hijoElegible()));

        assertEquals(2, nomina.getCantidadHijos());
        assertEquals(0, new BigDecimal("113.00").compareTo(nomina.getBonifFamiliar()));
    }

    private FamiliaInfoModel hijoElegible() {
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(6L);
        FamiliaInfoModel hijo = new FamiliaInfoModel();
        hijo.setEmpleado(empleado);
        hijo.setParentesco("HIJO");
        hijo.setElegibleAsignacionFamiliar(true);
        hijo.setValidado(true);
        return hijo;
    }

    // Corre calcularNomina para un empleado con sueldo 2500 y devuelve la nómina guardada
    private Nomina calcularNominaDeEmpleado(List<BigDecimal> horasPorTurno, String nombreRol, List<FamiliaInfoModel> familia) {
        LocalDate inicio = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate fin = inicio.plusDays(27);
        when(cambioSueldo.tieneCambioPosteriorAl(eq(6L), any())).thenReturn(false);
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setIdEmpleado(6L);
        empleado.setNombres("Horas");
        empleado.setApellidos("Extra");
        empleado.setEstado("ACTIVO");
        ContratoModel contrato = new ContratoModel();
        contrato.setEmpleado(empleado);
        contrato.setSueldo(SUELDO_VALIDO);
        contrato.setTipoPension("ONP");
        contrato.setEstado("ACTIVO");
        contrato.setFechaInicio(inicio.minusMonths(1));
        empleado.setContratos(List.of(contrato));
        when(jpaEmpleadoRepository.findByEstado("ACTIVO")).thenReturn(List.of(empleado));
        when(jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(anyList(), eq("ACTIVO")))
            .thenReturn(List.of(contrato));
        when(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(anyList())).thenReturn(familia);
        // Un día trabajado para que el sueldo devengado sea mayor a cero
        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(inicio.plusDays(1));
        registro.setHoraEntrada(LocalTime.of(8, 0));
        registro.setHoraSalida(LocalTime.of(16, 0));
        registro.setTipoRegistro("ORIGINAL");
        registro.setMinutosTardanza(0);
        registro.setMinutosExtra(0);
        when(jpaRegistroAsistenciaRepository.findByEmpleadoIdEmpleadoInAndFechaBetween(anyList(), any(), any()))
            .thenReturn(List.of(registro));
        if (nombreRol != null) {
            RolModel rol = new RolModel();
            rol.setNombreRol(nombreRol);
            UsuarioModel usuario = new UsuarioModel();
            usuario.setEmpleado(empleado);
            usuario.setRol(rol);
            when(jpaUsuarioRepository.findByEmpleadoIdEmpleadoIn(anyCollection())).thenReturn(List.of(usuario));
        }
        when(liquidacionHorasExtra.horasPorTurno(eq(6L), any())).thenReturn(horasPorTurno);
        when(nominaFactory.crearNominaBase()).thenAnswer(inv -> new Nomina());
        when(nominaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        nominaService.calcularNomina(new CalcularNominaRequestDTO(inicio, fin, null, null), ID_USUARIO_RRHH);

        ArgumentCaptor<Nomina> captor = ArgumentCaptor.forClass(Nomina.class);
        verify(nominaRepository, atLeastOnce()).guardar(captor.capture());
        return captor.getValue();
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private Nomina nominaConId(Integer id) {
        Nomina n = new Nomina();
        n.setIdNomina(id);
        return n;
    }
}
