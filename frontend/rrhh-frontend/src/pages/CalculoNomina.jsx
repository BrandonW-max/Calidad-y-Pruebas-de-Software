import { useState, useEffect, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { Volume2 } from "lucide-react";
import PayrollAudioModal from "../components/PayrollAudioModal";
import AccessibleDialog from "../components/AccessibleDialog";
import { speakText, stopSpeech, buildGeneralPayrollSummaryNarrative } from "../utils/speech";
import {
  calcularNomina,
  getNominasPorPeriodo,
  enviarComprobante,
  descargarComprobante,
  ajustarNomina,
  getVersionesNomina,
  listarHistorialAjustes,
  listarDetalleHorasExtra,
  obtenerUmbralHorasDobles,
  getDepartamentos,
  listarEmpleados,
} from "../services/api";

const getBonif = (n) =>
  (Number(n.bonifFamiliar)      || 0) +
  (Number(n.bonifTurnoNocturno) || 0) +
  (Number(n.bonifGuardia)       || 0) +
  (Number(n.bonifRiesgo)        || 0);

const getDesc = (n) =>
  (Number(n.descuentoTardanzas) || 0) +
  (Number(n.descuentoLey)       || 0);

const MESES = [
  { v: "01", l: "Enero" }, { v: "02", l: "Febrero" }, { v: "03", l: "Marzo" },
  { v: "04", l: "Abril" }, { v: "05", l: "Mayo" },    { v: "06", l: "Junio" },
  { v: "07", l: "Julio" }, { v: "08", l: "Agosto" },  { v: "09", l: "Septiembre" },
  { v: "10", l: "Octubre" }, { v: "11", l: "Noviembre" }, { v: "12", l: "Diciembre" },
];

const anioActual = new Date().getFullYear();
const ANIOS = [anioActual - 1, anioActual, anioActual + 1];

export default function CalculoNomina() {
  const [mes,    setMes]    = useState(String(new Date().getMonth() + 1).padStart(2, "0"));
  const [anio,   setAnio]   = useState(String(anioActual));
  const [loading, setLoading] = useState(false);
  const [calculando, setCalculando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [periodoCalculado, setPeriodoCalculado] = useState(false);
  // Empleados con nómina PAGADA en el período: no se pueden recalcular
  const [pagadosIds, setPagadosIds] = useState(() => new Set());
  const [verificando, setVerificando] = useState(false);
  const [historial, setHistorial] = useState([]);
  const [mensaje,   setMensaje]   = useState("");
  const [error,     setError]     = useState("");
  const [enviando,  setEnviando]  = useState({});
  const [departamentos, setDepartamentos] = useState([]);
  const [empleados, setEmpleados] = useState([]);
  const [filtroDepartamento, setFiltroDepartamento] = useState("");
  const [filtroEmpleado, setFiltroEmpleado] = useState("");

  useEffect(() => {
    getDepartamentos().then(setDepartamentos).catch(() => {});
    listarEmpleados(0, 200)
      .then(d => setEmpleados((d?.content ?? []).filter(e => e.estado === "ACTIVO")))
      .catch(() => {});
  }, []);
  const [guardandoAjuste, setGuardandoAjuste] = useState(false);
  const [errorModal, setErrorModal] = useState({ open: false, message: "" });
  const closeErrorModal = () => setErrorModal({ open: false, message: "" });
  const [previewNomina, setPreviewNomina] = useState(null);
  const [audioModal, setAudioModal] = useState({ open: false, nomina: null, periodoLabel: "" });
  const [narrandoGeneral, setNarrandoGeneral] = useState(false);
  const [modalEstructura, setModalEstructura] = useState(null);
  const [versionesNomina, setVersionesNomina] = useState([]);
  const [mostrarHistorialAjustes, setMostrarHistorialAjustes] = useState(false);
  const [historialAjustes, setHistorialAjustes] = useState(null);
  const [mostrarDetalleHorasExtra, setMostrarDetalleHorasExtra] = useState(false);
  const [detalleHorasExtra, setDetalleHorasExtra] = useState(null);
  const [umbralHorasDobles, setUmbralHorasDobles] = useState(null);
  const [horasExtraError, setHorasExtraError] = useState(false);
  const fmtFechaDetalle = (f) => (f ? f.split("-").reverse().join("/") : "");

  const handleHistorialAjustes = async () => {
    const abrir = !mostrarHistorialAjustes;
    setMostrarHistorialAjustes(abrir);
    if (abrir) {
      setHistorialAjustes(null);
      try {
        setHistorialAjustes(await listarHistorialAjustes());
      } catch {
        setHistorialAjustes([]);
      }
    }
  };
  const [datosBase, setDatosBase] = useState({
    sueldoBase: 0, horasTrabajadas: 0, horasExtra: 0, ajusteHorasExtra: 0,
    minutosLate: 0,
    tipoPension: "ONP", cantidadGuardias: 0,
  });

  const cargarVersiones = async (idNomina) => {
    try {
      setVersionesNomina(await getVersionesNomina(idNomina));
    } catch {
      setVersionesNomina([]);
    }
  };

  const abrirDetalleHorasExtra = async () => {
    setMostrarDetalleHorasExtra(true);
    setDetalleHorasExtra(null);
    try {
      setDetalleHorasExtra(await listarDetalleHorasExtra(modalEstructura.idEmpleado, modalEstructura.periodo));
    } catch {
      setDetalleHorasExtra([]);
      falloHorasExtra("No se pudo cargar el detalle de horas extra por turno.");
    }
  };

  const abrirEstructura = async (n) => {
    cargarVersiones(n.idNomina);
    setModalEstructura(n);
    setDatosBase({
      sueldoBase:       Number(n.sueldoContrato ?? n.sueldoBase ?? 0),
      horasTrabajadas:  Math.round(Number(n.totalHorasTrabajadas ?? 0) * 10) / 10,
      horasExtra:       Math.round(Number(n.totalHorasExtraAsistencia ?? n.totalHorasExtra ?? 0) * 10) / 10,
      ajusteHorasExtra: Math.round((Number(n.totalHorasExtra ?? 0) - Number(n.totalHorasExtraAsistencia ?? n.totalHorasExtra ?? 0)) * 10) / 10,
      minutosLate:      n.totalMinutosTardanza ?? 0,
      tipoPension:      n.tipoPensionAplicada ?? "ONP",
      cantidadGuardias: n.cantidadGuardias ?? 0,
    });
    // El detalle por turno alimenta la vista previa de horas extra
    setDetalleHorasExtra(null);
    setUmbralHorasDobles(null);
    setHorasExtraError(false);
    const [detalle, umbral] = await Promise.allSettled([
      listarDetalleHorasExtra(n.idEmpleado, n.periodo),
      obtenerUmbralHorasDobles(),
    ]);
    setDetalleHorasExtra(detalle.status === "fulfilled" ? detalle.value : []);
    if (umbral.status === "fulfilled") setUmbralHorasDobles(umbral.value.umbralHorasDobles);
    const fallosDetalle = detalle.status === "rejected";
    const fallosUmbral = umbral.status === "rejected";
    if (fallosDetalle && fallosUmbral) {
      falloHorasExtra("No se pudo cargar el detalle de horas extra por turno ni el umbral de horas dobles. Revise la conexión con el servidor y vuelva a abrir el formulario.");
    } else if (fallosDetalle) {
      falloHorasExtra("No se pudo cargar el detalle de horas extra por turno. Cierre y vuelva a abrir el formulario.");
    } else if (fallosUmbral) {
      falloHorasExtra("No se pudo cargar el umbral de horas dobles. Cierre y vuelva a abrir el formulario.");
    }
  };

  const falloHorasExtra = (message) => {
    setHorasExtraError(true);
    setErrorModal({ open: true, message });
  };

  const periodo = `${anio}-${mes}`;
  const hoy = new Date();

  const esPeriodoFuturo = (mesSeleccionado = mes, anioSeleccionado = anio) =>
    Number(anioSeleccionado) > hoy.getFullYear() ||
    (Number(anioSeleccionado) === hoy.getFullYear() && Number(mesSeleccionado) > hoy.getMonth() + 1);

  // Mes actual seleccionado → cálculo ilimitado (provisional)
  const esMesActual = Number(mes) === hoy.getMonth() + 1 && Number(anio) === hoy.getFullYear();

  // Mes anterior seleccionado Y hoy es día 1 → ventana de cierre final
  const fechaPrevia = new Date(hoy.getFullYear(), hoy.getMonth() - 1, 1);
  const esMesPrevioY1ro =
    Number(mes) === fechaPrevia.getMonth() + 1 &&
    Number(anio) === fechaPrevia.getFullYear() &&
    hoy.getDate() === 1;

  // En ventana de recálculo: mes actual O cierre del mes anterior
  const enVentanaRecalculo = esMesActual || esMesPrevioY1ro;

  // Fuera de la ventana solo se recalcula a un empleado concreto; el período completo queda cerrado
  const recalculoIndividual = !!filtroEmpleado;

  // "Calcular" bloqueado si: futuro, O ya calculado y fuera de ventana (salvo recálculo de un empleado)
  // Con nóminas pagadas: si se recalcula a un empleado, solo bloquea si es él; si no, bloquea el período
  const hayPagos = recalculoIndividual ? pagadosIds.has(Number(filtroEmpleado)) : pagadosIds.size > 0;
  const calcularBloqueado = esPeriodoFuturo()
    || (periodoCalculado && ((!enVentanaRecalculo && !recalculoIndividual) || hayPagos));

  // "Cargar" bloqueado si aún no hay nómina calculada
  const cargarBloqueado = !periodoCalculado;

  const cambiarPeriodo = (campo, valor) => {
    if (campo === "mes") setMes(valor);
    else setAnio(valor);
    setResultado(null);
    setPeriodoCalculado(false);
    setMensaje("");
    setError("");
  };

  const verificarPeriodo = useCallback(async (m, a) => {
    if (esPeriodoFuturo(m, a)) return;
    setVerificando(true);
    try {
      const data = await getNominasPorPeriodo(m, a);
      if (Array.isArray(data) && data.length > 0) {
        const byEmployee = {};
        for (const n of data) {
          if (!byEmployee[n.idEmpleado] || n.idNomina > byEmployee[n.idEmpleado].idNomina)
            byEmployee[n.idEmpleado] = n;
        }
        const deduped = Object.values(byEmployee)
          .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity));
        setResultado(deduped);
        setPeriodoCalculado(true);
        setPagadosIds(new Set(deduped.filter(n => n.estadoPago === "PAGADA").map(n => Number(n.idEmpleado))));
        setMensaje(`Período ${a}-${m} ya calculado — mostrando ${deduped.length} registros.`);
      } else {
        setPeriodoCalculado(false);
        setPagadosIds(new Set());
        setResultado(null);
      }
    } catch {
      setPeriodoCalculado(false);
    } finally {
      setVerificando(false);
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    verificarPeriodo(mes, anio);
  }, [mes, anio]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleCalcular = async () => {
    setCalculando(true);
    setError("");
    setMensaje("");
    const filtro = filtroEmpleado
      ? { idEmpleado: Number(filtroEmpleado) }
      : { idDepartamento: filtroDepartamento ? Number(filtroDepartamento) : null };
    try {
      let data;
      try {
        data = await calcularNomina(mes, anio, filtro);
      } catch (err) {
        // Hay un cambio de sueldo posterior al inicio del período: se calcula solo si RRHH lo confirma
        if (err.errorCode !== "CAMBIO_SUELDO_PENDIENTE") throw err;
        if (!window.confirm(`${err.message}\n\n¿Calcular de todas formas?`)) return;
        data = await calcularNomina(mes, anio, { ...filtro, confirmarCambiosSueldo: true });
      }
      if (Array.isArray(data)) {
        setResultado([...data].sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity)));
        setHistorial(h => {
          const entry = { ...data, periodo, timestamp: new Date().toISOString() };
          return [entry, ...h.filter(x => x.periodo !== periodo)].slice(0, 10);
        });
        setMensaje("Nómina calculada correctamente (" + data.length + " empleados)");
      } else {
        throw new Error(data?.message || "Respuesta inválida");
      }
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al calcular la nómina. Verifique el período." });
    } finally {
      setCalculando(false);
    }
  };

  const handleCargarPeriodo = async () => {
    setLoading(true);
    setError("");
    setMensaje("");
    try {
      const data = await getNominasPorPeriodo(mes, anio);
      if (Array.isArray(data) && data.length > 0) {
        const byEmployee = {};
        for (const n of data) {
          if (!byEmployee[n.idEmpleado] || n.idNomina > byEmployee[n.idEmpleado].idNomina) {
            byEmployee[n.idEmpleado] = n;
          }
        }
        const deduped = Object.values(byEmployee)
          .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity));
        setResultado(deduped);
        setMensaje(`Se cargaron ${deduped.length} registros del período ${periodo}`);
      } else {
        setError(`No hay nóminas registradas para ${periodo}. Calcule primero.`);
      }
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al cargar el período" });
    } finally {
      setLoading(false);
    }
  };

  const handleEnviarComprobante = async (idNomina) => {
    setEnviando(v => ({ ...v, [idNomina]: true }));
    setMensaje("");
    setError("");
    try {
      await enviarComprobante(idNomina);
      setMensaje(`Comprobante enviado correctamente (Nómina #${idNomina})`);
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al enviar el comprobante" });
    } finally {
      setEnviando(v => ({ ...v, [idNomina]: false }));
    }
  };

  const handleDescargar = async (idNomina) => {
    try {
      const blob = await descargarComprobante(idNomina);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `comprobante-nomina-${idNomina}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setError("Error al descargar el comprobante.");
    }
  };

  const reemplazarNomina = (actualizada) => {
    setResultado(prev => {
      if (!prev) return prev;
      const lista = Array.isArray(prev) ? prev : [prev];
      const nuevaLista = lista.map(n => n.idNomina === actualizada.idNomina ? actualizada : n);
      return Array.isArray(prev) ? nuevaLista : nuevaLista[0];
    });
    setModalEstructura(actualizada);
  };

  const handleGuardarAjuste = async () => {
    if (!modalEstructura?.idNomina) return;
    setGuardandoAjuste(true);
    setMensaje("");
    setError("");
    try {
      const actualizada = await ajustarNomina(modalEstructura.idNomina, {
        sueldoBase: datosBase.sueldoBase,
        totalHorasTrabajadas: datosBase.horasTrabajadas,
        totalHorasExtra: datosBase.ajusteHorasExtra,
        totalMinutosTardanza: datosBase.minutosLate,
        cantidadGuardias: datosBase.cantidadGuardias,
        tipoPensionAplicada: datosBase.tipoPension,
      });
      reemplazarNomina(actualizada);
      cargarVersiones(actualizada.idNomina);
      listarDetalleHorasExtra(modalEstructura.idEmpleado, modalEstructura.periodo)
        .then(setDetalleHorasExtra)
        .catch(() => falloHorasExtra("No se pudo actualizar el detalle de horas extra por turno."));
      setDatosBase(p => ({
        ...p,
        sueldoBase: Number(actualizada.sueldoBase ?? p.sueldoBase),
        horasTrabajadas: Number(actualizada.totalHorasTrabajadas ?? p.horasTrabajadas),
        horasExtra: Number(actualizada.totalHorasExtraAsistencia ?? p.horasExtra),
        ajusteHorasExtra: Math.round((Number(actualizada.totalHorasExtra ?? 0) - Number(actualizada.totalHorasExtraAsistencia ?? 0)) * 10) / 10,
        minutosLate: actualizada.totalMinutosTardanza ?? p.minutosLate,
        cantidadGuardias: actualizada.cantidadGuardias ?? p.cantidadGuardias,
        tipoPension: actualizada.tipoPensionAplicada ?? p.tipoPension,
      }));
      setMensaje(`Nómina #${actualizada.idNomina} ajustada correctamente. Neto: ${fmtS(actualizada.sueldoNeto)}`);
    } catch (err) {
      setErrorModal({ open: true, message: err.message || "Error al guardar el ajuste de nómina." });
    } finally {
      setGuardandoAjuste(false);
    }
  };

  const fmtS = (v) => v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const resultList = resultado
    ? (Array.isArray(resultado) ? [...resultado] : [resultado])
        .sort((a, b) => (a.idNomina ?? Infinity) - (b.idNomina ?? Infinity))
    : [];

  const totalNeto  = resultList.reduce((sum, n) => sum + Number(n.sueldoNeto ?? 0), 0);
  const totalBruto = resultList.reduce((sum, n) => sum + Number(n.sueldoBruto ?? n.sueldoBase ?? 0), 0);

  return (
    <div className="dashboard rrhh-accessible">
      <Sidebar rol="RRHH" />

      <main className="main-content rrhh-accessible" id="main-content" tabIndex="-1">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Cálculo de Nómina</span>
            <h1>Cálculo de Nómina</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Gestiona los pagos mensuales y descuentos de los empleados.
            </p>
          </div>
        </header>

        <div className="nomina-layout">

          {/* ── COLUMNA IZQUIERDA ── */}
          <div className="nomina-left">


            {mensaje && <div className="alert alert-floating alert-success" role="status" aria-live="polite">
              <span>✅ {mensaje}</span>
              <button type="button" className="alert-dismiss" aria-label="Cerrar mensaje de éxito" onClick={() => setMensaje("")}>×</button>
            </div>}
            {error && <div className="alert alert-floating alert-warning" role="alert">
              <span>⚠️ {error}</span>
              <button type="button" className="alert-dismiss" aria-label="Cerrar mensaje de error" onClick={() => setError("")}>×</button>
            </div>}

            <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)", background: "linear-gradient(135deg, #f0fdf4 0%, #eff6ff 100%)", display: "flex", alignItems: "center", gap: 12 }}>
                <div style={{ width: 40, height: 40, borderRadius: 10, background: "#22c55e", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20, flexShrink: 0 }}>
                  📅
                </div>
                <div>
                  <h2 style={{ margin: 0, fontSize: 16 }}>Configuración del Período</h2>
                  <p style={{ margin: 0, fontSize: 12, color: "var(--text-3)" }}>Seleccione el mes y año para calcular la nómina</p>
                </div>
              </div>

              <div style={{ padding: "16px 20px 0" }}>
                <div className="form-grid" style={{ gap: 12, marginBottom: 12 }}>
                  <div className="form-group">
                    <label htmlFor="nomina-periodo-mes">Mes</label>
                    <select id="nomina-periodo-mes" value={mes} onChange={e => cambiarPeriodo("mes", e.target.value)}>
                      {MESES.map(m => <option key={m.v} value={m.v} disabled={esPeriodoFuturo(m.v, anio)}>{m.l}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label htmlFor="nomina-periodo-anio">Año</label>
                    <select id="nomina-periodo-anio" value={anio} onChange={e => cambiarPeriodo("anio", e.target.value)}>
                      {ANIOS.map(a => <option key={a} value={a} disabled={esPeriodoFuturo(mes, a)}>{a}</option>)}
                    </select>
                  </div>
                </div>

                <div style={{ background: "linear-gradient(90deg, #eff6ff 0%, #f0fdf4 100%)", border: "1px solid #bfdbfe", borderRadius: 10, padding: "10px 14px", marginBottom: 14, display: "flex", alignItems: "center", gap: 10 }}>
                  <span style={{ fontSize: 20 }}>📆</span>
                  <div>
                    <div style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em" }}>Período seleccionado</div>
                    <div style={{ fontSize: 15, fontWeight: 700, color: "var(--primary)" }}>{MESES.find(m => m.v === mes)?.l} {anio}</div>
                  </div>
                </div>
              </div>

              <div style={{ padding: "0 20px 14px", display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: 10 }}>
                <div>
                  <label htmlFor="nomina-filtro-departamento" className="nomina-filter-label">Departamento</label>
                  <select id="nomina-filtro-departamento" value={filtroDepartamento} onChange={e => setFiltroDepartamento(e.target.value)} disabled={calculando || !!filtroEmpleado} style={{ width: "100%" }}>
                    <option value="">Todos</option>
                    {departamentos.map(d => <option key={d.idDpto} value={d.idDpto}>{d.nombre}</option>)}
                  </select>
                </div>
                <div>
                  <label htmlFor="nomina-filtro-empleado" className="nomina-filter-label">Empleado</label>
                  <select id="nomina-filtro-empleado" value={filtroEmpleado} onChange={e => setFiltroEmpleado(e.target.value)} disabled={calculando} style={{ width: "100%" }}>
                    <option value="">Todos</option>
                    {empleados.map(e => <option key={e.idEmpleado} value={e.idEmpleado}>{e.nombres} {e.apellidos}</option>)}
                  </select>
                </div>
              </div>

              {/* Aviso contextual según estado del período */}
              {periodoCalculado && !enVentanaRecalculo && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#fef9c3", border: "1px solid #fde047",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>🔒</span>
                  {recalculoIndividual ? (
                    <span>Este período <strong>ya fue calculado y cerrado</strong>. Puede recalcular solo al empleado seleccionado.</span>
                  ) : (
                    <span>Este período <strong>ya fue calculado y cerrado</strong>. Use "Cargar Período" para ver los datos, o seleccione un empleado para recalcularlo.</span>
                  )}
                </div>
              )}
              {esMesPrevioY1ro && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#fef3c7", border: "1px solid #f59e0b",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>⚠️</span>
                  <span><strong>Ventana de cierre:</strong> hoy es el último día para recalcular la nómina de este período.</span>
                </div>
              )}
              {esMesActual && (
                <div style={{
                  margin: "0 20px 12px", padding: "10px 14px", borderRadius: 8,
                  background: "#eff6ff", border: "1px solid #93c5fd",
                  display: "flex", alignItems: "center", gap: 8, fontSize: 13,
                }}>
                  <span style={{ fontSize: 18 }}>📊</span>
                  <span>Mes en curso — puede calcular nómina provisional sin límite.</span>
                </div>
              )}

              <div style={{ padding: "0 20px 20px", display: "flex", gap: 10 }}>
                <button
                  className="btn btn-success"
                  onClick={handleCalcular}
                  disabled={calculando || verificando || calcularBloqueado}
                  title={calcularBloqueado && !esPeriodoFuturo()
                    ? (hayPagos ? "Hay nóminas pagadas en este cálculo: no se pueden recalcular." : "Período cerrado. Seleccione un empleado para recalcularlo, o use Cargar Período.")
                    : ""}
                  style={{ flex: 1, opacity: calcularBloqueado ? 0.45 : 1, cursor: calcularBloqueado ? "not-allowed" : "pointer" }}
                >
                  {calculando ? "Calculando..." : calcularBloqueado ? "🔒 Cálculo bloqueado" : "⚙️ Calcular Nómina"}
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={handleCargarPeriodo}
                  disabled={loading || verificando || cargarBloqueado}
                  title={cargarBloqueado ? "Calcule la nómina primero para poder cargarla." : ""}
                  style={{ flex: 1, opacity: cargarBloqueado ? 0.45 : 1, cursor: cargarBloqueado ? "not-allowed" : "pointer" }}
                >
                  {loading || verificando ? "Cargando..." : cargarBloqueado ? "📋 Sin datos aún" : "📋 Cargar Período"}
                </button>
                <button
                  className="btn btn-secondary"
                  onClick={handleHistorialAjustes}
                  style={{ flex: 1 }}
                >
                  {mostrarHistorialAjustes ? "📜 Ocultar historial de ajustes" : "📜 Historial de ajustes"}
                </button>
              </div>
            </div>

            {resultList.length > 0 && (
              <div className="section-card asistencia-panel">
                <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px", display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: 10 }}>
                  <h2 style={{ margin: 0, border: "none", padding: 0 }}>
                    Resultados — {MESES.find(m => m.v === mes)?.l} {anio}
                  </h2>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    style={{ display: "inline-flex", alignItems: "center", gap: 6, borderColor: "#3b82f6", color: "#1d4ed8" }}
                    onClick={() => {
                      if (narrandoGeneral) {
                        stopSpeech();
                        setNarrandoGeneral(false);
                      } else {
                        const mLabel = MESES.find(m => m.v === mes)?.l ?? mes;
                        const texto = buildGeneralPayrollSummaryNarrative(resultList, `${mLabel} ${anio}`);
                        setNarrandoGeneral(true);
                        speakText(texto, {
                          onEnd: () => setNarrandoGeneral(false),
                          onError: () => setNarrandoGeneral(false),
                        });
                      }
                    }}
                    title="Narración auditiva del total de la nómina para personas con discapacidad visual"
                  >
                    <Volume2 size={15} />
                    <span>{narrandoGeneral ? "⏹ Detener audio" : "🔊 Escuchar resumen del período"}</span>
                  </button>
                </div>
                <div className="table-wrapper" style={{ marginBottom: 0, overflowX: "auto", paddingBottom: 14, ...(resultList.length > 15 ? { maxHeight: 690, overflowY: "auto" } : {}) }}>
                  <table style={{ minWidth: 700 }}>
                    <thead>
                      <tr>
                        <th style={{ textAlign: "center" }}>Empleado</th>
                        <th style={{ textAlign: "center" }}>Sueldo Devengado</th>
                        <th style={{ textAlign: "center" }}>Bonificaciones</th>
                        <th style={{ textAlign: "center" }}>Descuentos</th>
                        <th style={{ textAlign: "center" }}>Sueldo Neto</th>
                        <th style={{ textAlign: "center" }}>Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {resultList.map((n, i) => (
                        <tr key={n.idNomina ?? i}>
                          <td style={{ textAlign: "center" }}>
                            <button type="button" className="table-row-action"
                              aria-label={`Ver estructura de nómina de ${n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? `empleado ${n.idEmpleado ?? ""}`}`}
                              onClick={() => abrirEstructura(n)}>
                              <strong>{n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? (n.idEmpleado ? `Emp. #${n.idEmpleado}` : "—")}</strong>
                            </button>
                          </td>
                          <td style={{ textAlign: "center" }}>{fmtS(n.sueldoBase ?? n.sueldo)}</td>
                          <td style={{ textAlign: "center", color: "var(--success)" }}>{fmtS(getBonif(n))}</td>
                          <td style={{ textAlign: "center", color: "#ef4444" }}>{fmtS(getDesc(n))}</td>
                          <td style={{ textAlign: "center", fontWeight: 700, color: "#0f172a" }}>{fmtS(n.sueldoNeto)}</td>
                          <td style={{ textAlign: "center" }}>
                            <div style={{ display: "flex", gap: "4px", justifyContent: "center" }}>
                              <button className="btn btn-secondary btn-sm"
                                title="Escuchar boleta accesible por voz (ODS 10)"
                                aria-label={`Escuchar boleta de nómina de ${n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? `empleado ${n.idEmpleado ?? ""}`}`}
                                onClick={() => {
                                  const mLabel = MESES.find(m => m.v === mes)?.l ?? mes;
                                  setAudioModal({ open: true, nomina: n, periodoLabel: `${mLabel} ${anio}` });
                                }}>
                                <Volume2 size={13} aria-hidden="true" />
                              </button>
                              <button className="btn btn-secondary btn-sm"
                                title="Vista previa detallada"
                                aria-label={`Ver vista previa detallada de nómina de ${n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? `empleado ${n.idEmpleado ?? ""}`}`}
                                onClick={() => setPreviewNomina(n)}>
                                <span aria-hidden="true">👁</span>
                              </button>
                              <button className="btn btn-secondary btn-sm"
                                onClick={() => handleDescargar(n.idNomina)}>
                                ⬇ PDF
                              </button>
                              <button className="btn btn-primary btn-sm"
                                onClick={() => handleEnviarComprobante(n.idNomina)}
                                aria-label={`${enviando[n.idNomina] ? "Enviando comprobante de" : "Enviar comprobante por correo de"} ${n.nombreEmpleado ?? n.empleadoNombre ?? n.nombres ?? `empleado ${n.idEmpleado ?? ""}`}`}
                                aria-busy={Boolean(enviando[n.idNomina])}
                                disabled={enviando[n.idNomina]}>
                                {enviando[n.idNomina] ? "Enviando…" : "📧 Email"}
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot>
                      <tr style={{ background: "#f8fafc", fontWeight: 600 }}>
                        <td colSpan={1} style={{ padding: "10px 12px", fontSize: "13px" }}>
                          TOTALES ({resultList.length} empleados)
                        </td>
                        <td style={{ textAlign: "center", padding: "10px 12px" }}>{fmtS(totalBruto)}</td>
                        <td></td>
                        <td></td>
                        <td style={{ textAlign: "center", padding: "10px 12px", color: "var(--primary)" }}>{fmtS(totalNeto)}</td>
                        <td></td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>
            )}

            {mostrarHistorialAjustes && (
              <div className="section-card asistencia-panel">
                <div className="section-header asistencia-panel-header" style={{ marginBottom: "14px" }}>
                  <h2 style={{ margin: 0, border: "none", padding: 0 }}>📜 Historial de Ajustes Manuales</h2>
                </div>
                <div className="table-wrapper" style={{ overflowX: "auto" }}>
                  {historialAjustes === null ? (
                    <p style={{ padding: 14 }}>Cargando...</p>
                  ) : historialAjustes.length === 0 ? (
                    <div className="empty-state"><p>No hay ajustes reemplazados todavía.</p></div>
                  ) : (
                    <table>
                      <thead>
                        <tr>
                          <th style={{ textAlign: "center" }}>Periodo</th>
                          <th style={{ textAlign: "center" }}>Empleado</th>
                          <th style={{ textAlign: "center" }}>Versión</th>
                          <th style={{ textAlign: "center" }}>Horas extra</th>
                          <th style={{ textAlign: "center" }}>Tardanza (min)</th>
                          <th style={{ textAlign: "center" }}>Sueldo neto</th>
                          <th style={{ textAlign: "center" }}>Reemplazado el</th>
                        </tr>
                      </thead>
                      <tbody>
                        {historialAjustes.map(h => (
                          <tr key={h.id}>
                            <td style={{ textAlign: "center" }}>{h.periodo}</td>
                            <td>{h.empleado}</td>
                            <td style={{ textAlign: "center" }}>{h.version}</td>
                            <td style={{ textAlign: "center" }}>{Number(h.totalHorasExtra ?? 0)} h</td>
                            <td style={{ textAlign: "center" }}>{h.totalMinutosTardanza ?? 0}</td>
                            <td style={{ textAlign: "center" }}>{fmtS(h.sueldoNeto)}</td>
                            <td style={{ textAlign: "center" }}>{h.reemplazadoEl ? new Date(h.reemplazadoEl).toLocaleString("es-PE") : "—"}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              </div>
            )}

            {resultList.length === 0 && (
              <div className="section-card">
                <div className="empty-state">
                  <div className="empty-state-icon">📊</div>
                  <p>Seleccione un período y calcule o cargue la nómina para ver los resultados.</p>
                </div>
              </div>
            )}
          </div>

          {/* ── COLUMNA DERECHA ── */}
          <div className="nomina-right">
            <div className="section-card">
              <h2>Estado del Período</h2>
              <div className="nomina-status-item">
                <span>Período actual:</span>
                <strong>{MESES.find(m => m.v === mes)?.l} {anio}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total empleados:</span>
                <strong>{resultList.length > 0 ? resultList.length : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Total neto a pagar:</span>
                <strong style={{ color: "var(--primary)" }}>{resultList.length > 0 ? fmtS(totalNeto) : "—"}</strong>
              </div>
              <div className="nomina-status-item">
                <span>Estado:</span>
                <span className={`badge ${resultado ? "badge-success" : "badge-gray"}`}>
                  {resultado ? "Calculado" : "Pendiente"}
                </span>
              </div>
            </div>

            {historial.length > 0 && (
              <div className="section-card">
                <h2>Historial Reciente</h2>
                {historial.map((h, i) => (
                  <div key={i} className="nomina-hist-item">
                    <span className="nomina-hist-periodo">{h.periodo}</span>
                    <span className="badge badge-success" style={{ fontSize: "11px" }}>Calculado</span>
                  </div>
                ))}
              </div>
            )}

            <div className="section-card">
              <h2>Guía Rápida</h2>
              <ol style={{ margin: 0, paddingLeft: "18px", fontSize: "13px", color: "var(--text-2)", lineHeight: "1.9" }}>
                <li>Seleccione mes y año del período.</li>
                <li>Presione <strong>Calcular Nómina</strong> para procesar.</li>
                <li>Revise los resultados en la tabla.</li>
                <li>Descargue o envíe comprobantes individualmente.</li>
              </ol>
            </div>
          </div>

        </div>
      </main>

      {/* ── MODAL VISTA PREVIA NÓMINA ── */}
      {previewNomina && (() => {
        const n = previewNomina;
        const base  = Number(n.sueldoBase ?? n.sueldo ?? 0);
        const bonifs = [
          { label: "Bonif. Familiar",       val: n.bonifFamiliar },
          { label: "Bonif. Turno Nocturno", val: n.bonifTurnoNocturno },
          { label: "Bonif. Guardia",        val: n.bonifGuardia },
          { label: "Bonif. Riesgo",         val: n.bonifRiesgo },
        ];
        const descs = [
          { label: "Descuento Tardanzas", val: n.descuentoTardanzas },
          { label: "Descuento de Ley",    val: n.descuentoLey },
        ];
        const totalBonif = getBonif(n);
        const totalDesc  = getDesc(n);
        const neto       = Number(n.sueldoNeto ?? 0);
        const mesLabel   = MESES.find(m => m.v === (n.mes ?? mes))?.l ?? n.mes ?? mes;
        return (
          <AccessibleDialog className="modal-overlay" labelledBy="nomina-preview-title" onClose={() => setPreviewNomina(null)}>
            <div className="modal-box" style={{ maxWidth: 500 }}>
              <div className="modal-header">
                <div>
                  <h3 id="nomina-preview-title">📊 Vista Previa — Nómina #{n.idNomina}</h3>
                  <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13 }}>
                    {n.nombreEmpleado ?? n.empleadoNombre ?? `Empleado #${n.idEmpleado}`} — {mesLabel} {n.anio ?? anio}
                  </p>
                </div>
                <button type="button" className="modal-close" aria-label="Cerrar vista previa de nómina" onClick={() => setPreviewNomina(null)}>×</button>
              </div>
              <div className="modal-body" style={{ display: "flex", flexDirection: "column", gap: 14 }}>
                <div style={{ display: "flex", justifyContent: "space-between", padding: "10px 0", borderBottom: "1px solid var(--border)" }}>
                  <span style={{ fontWeight: 600 }}>Sueldo Devengado</span>
                  <strong>{fmtS(base)}</strong>
                </div>
                {n.sueldoContrato != null && (
                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: "var(--text-2)", marginTop: -8 }}>
                    <span>Sueldo del contrato (referencia)</span>
                    <span>{fmtS(n.sueldoContrato)}</span>
                  </div>
                )}
                <div>
                  <div style={{ fontWeight: 700, color: "#16a34a", marginBottom: 8, fontSize: 13 }}>+ Bonificaciones</div>
                  {bonifs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#16a34a" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Bonificaciones</span>
                    <span style={{ color: "#16a34a" }}>{fmtS(totalBonif)}</span>
                  </div>
                </div>
                <div>
                  <div style={{ fontWeight: 700, color: "#ef4444", marginBottom: 8, fontSize: 13 }}>— Descuentos</div>
                  {descs.filter(r => Number(r.val)).map(r => (
                    <div key={r.label} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, marginBottom: 4, paddingLeft: 12 }}>
                      <span style={{ color: "var(--text-2)" }}>{r.label}</span>
                      <span style={{ color: "#ef4444" }}>{fmtS(r.val)}</span>
                    </div>
                  ))}
                  <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 700, borderTop: "1px dashed var(--border)", paddingTop: 6, marginTop: 4 }}>
                    <span>Total Descuentos</span>
                    <span style={{ color: "#ef4444" }}>{fmtS(totalDesc)}</span>
                  </div>
                </div>
                <div style={{ background: "#f0fdf4", borderRadius: 10, padding: "12px 16px", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontWeight: 700, fontSize: 15 }}>Sueldo Neto a Pagar</span>
                  <span style={{ fontWeight: 800, fontSize: 18, color: "#15803d" }}>{fmtS(neto)}</span>
                </div>
              </div>
              <div className="modal-footer" style={{ display: "flex", justifyContent: "space-between" }}>
                <button
                  type="button"
                  className="btn btn-primary"
                  style={{ display: "inline-flex", alignItems: "center", gap: 6, background: "#0284c7" }}
                  onClick={() => {
                    const mLabel = MESES.find(m => m.v === mes)?.l ?? mes;
                    setAudioModal({ open: true, nomina: n, periodoLabel: `${mLabel} ${anio}` });
                  }}
                >
                  <Volume2 size={16} />
                  <span>Escuchar en Voz Alta</span>
                </button>
                <button className="btn btn-secondary" onClick={() => setPreviewNomina(null)}>Cerrar</button>
              </div>
            </div>
          </AccessibleDialog>
        );
      })()}

      {/* ── MODAL ESTRUCTURA SALARIAL ── */}
      {modalEstructura && (() => {
        const n   = modalEstructura;
        const db  = datosBase;
        const fmt = (v) => `S/ ${Number(v || 0).toLocaleString("es-PE", { minimumFractionDigits: 2 })}`;
        const mesLabel = MESES.find(m => m.v === mes)?.l ?? mes;

        const esClinico  = n.esEmpleadoClinico  === true;
        const esNocturno = n.tieneHorarioNocturno === true;

        const valorHora = db.sueldoBase > 0 ? db.sueldoBase / 30 / 8 : 0;
        const valorMin  = valorHora / 60;
        // Igual que el backend (RNF46): cada turno y cada ajuste paga 1.5x hasta el umbral de horas dobles y 2.0x después
        const horasExtraListas = detalleHorasExtra !== null && umbralHorasDobles !== null;
        const horasExtraNoDisponibles = horasExtraError;
        const horasExtraTotal = db.horasExtra + db.ajusteHorasExtra;
        const horasAjusteGuardadas = (detalleHorasExtra ?? [])
          .filter(h => h.tipo === "AJUSTE_MANUAL")
          .reduce((s, h) => s + Number(h.horas || 0), 0);
        // Horas de ajuste escritas en el formulario y aún no guardadas: son un bloque nuevo, como en el backend
        const horasAjusteNuevas = Math.max(0, Math.round((db.ajusteHorasExtra - horasAjusteGuardadas) * 100) / 100);
        const bloquesHorasExtra = [
          ...(detalleHorasExtra ?? []).map(h => Number(h.horas || 0)),
          horasAjusteNuevas,
        ].filter(h => h > 0);
        const montoHorasExtra = !horasExtraListas || horasExtraNoDisponibles ? 0 : bloquesHorasExtra.reduce(
          (s, h) => s + valorHora * (Math.min(h, umbralHorasDobles) * 1.5 + Math.max(0, h - umbralHorasDobles) * 2.0),
          0);
        // La asignación familiar la calcula el backend desde la familia del empleado (no es editable aquí)
        const bFam    = Number(n.bonifFamiliar ?? 0);
        // Igual que el backend: nocturno, riesgo y cargo sobre el sueldo devengado del período; el cargo viene calculado
        const sueldoDevengado = Number(n.sueldoBase ?? 0);
        const bNoct   = esNocturno ? sueldoDevengado * 0.15 : 0;
        const bGuard  = esClinico  ? db.cantidadGuardias * 50 : 0;
        const bRiesgo = esClinico  ? sueldoDevengado * 0.10 : 0;
        const bCargo  = Number(n.bonifCargo ?? 0);
        const descTard = valorMin * db.minutosLate;
        const bruto   = sueldoDevengado + montoHorasExtra + bFam + bNoct + bGuard + bRiesgo + bCargo;
        const tasa    = db.tipoPension === "AFP" ? 0.12 : 0.13;
        const descLey = bruto * tasa;
        const neto    = Math.max(0, bruto - descTard - descLey);
        // bCargo eliminado — no corresponde a concepto legal peruano

        const r1 = (v) => Math.round(Number(v ?? 0) * 10) / 10;
        const resetearValores = () => setDatosBase({
          sueldoBase:       Number(n.sueldoContrato ?? n.sueldoBase ?? 0),
          horasTrabajadas:  r1(n.totalHorasTrabajadas),
          horasExtra:       r1(n.totalHorasExtraAsistencia ?? n.totalHorasExtra),
          ajusteHorasExtra: r1(Number(n.totalHorasExtra ?? 0) - Number(n.totalHorasExtraAsistencia ?? n.totalHorasExtra ?? 0)),
          minutosLate:      n.totalMinutosTardanza ?? 0,
          tipoPension:      n.tipoPensionAplicada ?? "ONP",
          cantidadGuardias: n.cantidadGuardias ?? 0,
        });

        const InputField = ({ label, field, step = "0.01", parse, disabled, onInfo }) => (
          <div style={{ marginBottom: 10 }}>
            <label htmlFor={`nomina-${n.idNomina}-${field}`} style={{ fontSize: 10, fontWeight: 700, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.06em", display: "block", marginBottom: 3 }}>{label}</label>
            <div style={{ position: "relative" }}>
              <input
                id={`nomina-${n.idNomina}-${field}`}
                type="number" min="0" step={step}
                value={db[field]}
                onChange={e => setDatosBase(p => ({ ...p, [field]: parse ? parse(e.target.value) : Number(e.target.value) }))}
                disabled={disabled}
                style={{ width: "100%", padding: onInfo ? "7px 34px 7px 10px" : "7px 10px", border: "1px solid #64748b", borderRadius: 8, fontSize: 13, background: disabled ? "#f3f4f6" : "#fff", color: disabled ? "#9ca3af" : "#111827", boxSizing: "border-box" }}
              />
              {onInfo && (
                <button type="button" onClick={onInfo} aria-label={`Ver desglose por horario de ${label}`}
                  style={{ position: "absolute", right: 6, top: "50%", transform: "translateY(-50%)", border: "none", background: "none", cursor: "pointer", fontSize: 15, color: "#2563eb", padding: 2 }}>ⓘ</button>
              )}
            </div>
          </div>
        );

        const ToggleGroup = ({ label, field, options }) => (
          <div style={{ marginBottom: 10 }}>
            <fieldset style={{ border: 0, padding: 0, margin: 0, minWidth: 0 }}>
              <legend style={{ fontSize: 10, fontWeight: 700, color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.06em", display: "block", marginBottom: 3 }}>{label}</legend>
              <div style={{ display: "flex", gap: 6 }}>
              {options.map(({ val, label: lbl }) => (
                <button type="button" key={String(val)} aria-pressed={db[field] === val} onClick={() => setDatosBase(p => ({ ...p, [field]: val }))}
                  style={{ flex: 1, padding: "7px 0", border: `1.5px solid ${db[field] === val ? "#2563eb" : "#64748b"}`, borderRadius: 8, background: db[field] === val ? "#eff6ff" : "#fff", color: db[field] === val ? "#2563eb" : "#6b7280", fontWeight: 600, fontSize: 13, cursor: "pointer" }}>
                  {lbl}
                </button>
              ))}
              </div>
            </fieldset>
          </div>
        );

        const CalcRow = ({ label, formula, val, tipo = "bonif" }) => {
          const isDesc    = tipo === "desc";
          const isBlocked = tipo === "blocked";
          if (isBlocked) return (
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 11px", background: "#f3f4f6", borderLeft: "3px solid #d1d5db", borderRadius: 8, marginBottom: 5, opacity: 0.6 }}>
              <div>
                <div style={{ fontSize: 12, fontWeight: 600, color: "#9ca3af", display: "flex", alignItems: "center", gap: 5 }}>
                  <span>🔒</span> {label}
                </div>
                <div style={{ fontSize: 10, color: "#c0c4cc", marginTop: 1 }}>{formula}</div>
              </div>
              <span style={{ fontWeight: 700, fontSize: 13, color: "#c0c4cc", flexShrink: 0, marginLeft: 8 }}>No aplica</span>
            </div>
          );
          return (
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "8px 11px", background: isDesc ? "#fff5f5" : "#f0fdf4", borderLeft: `3px solid ${isDesc ? "#dc2626" : "#16a34a"}`, borderRadius: 8, marginBottom: 5 }}>
              <div>
                <div style={{ fontSize: 12, fontWeight: 600, color: isDesc ? "#7f1d1d" : "#14532d" }}>{label}</div>
                <div style={{ fontSize: 10, color: "#9ca3af", marginTop: 1 }}>{formula}</div>
              </div>
              <span style={{ fontWeight: 700, fontSize: 13, color: isDesc ? "#dc2626" : "#16a34a", flexShrink: 0, marginLeft: 8 }}>
                {isDesc ? "−" : "+"} {fmt(val)}
              </span>
            </div>
          );
        };

        return (
          <AccessibleDialog className="modal-overlay" labelledBy="nomina-estructura-title" onClose={() => setModalEstructura(null)}>
            <div className="modal-box" style={{ maxWidth: 1040, padding: 0, overflow: "hidden", borderRadius: 16 }}>

              {/* Header */}
              <div style={{ background: "linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%)", color: "#fff", padding: "18px 26px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                  <div>
                    <div style={{ fontSize: 10, textTransform: "uppercase", letterSpacing: "0.12em", opacity: 0.6, marginBottom: 4 }}>Estructura Salarial · Simulador</div>
                    <h3 id="nomina-estructura-title" style={{ margin: 0, fontSize: 17, fontWeight: 700 }}>{n.nombreEmpleado ?? `Empleado #${n.idEmpleado}`}</h3>
                    <div style={{ marginTop: 3, opacity: 0.72, fontSize: 12 }}>Período: {mesLabel} {anio} · Nómina #{n.idNomina}</div>
                  </div>
                  <button type="button" aria-label="Cerrar estructura salarial" onClick={() => setModalEstructura(null)} style={{ background: "rgba(255,255,255,0.18)", border: "none", color: "#fff", width: 30, height: 30, borderRadius: 7, cursor: "pointer", fontSize: 17, display: "flex", alignItems: "center", justifyContent: "center" }}>×</button>
                </div>
              </div>

              {/* Body: two columns */}
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 250px", gridTemplateRows: "minmax(0, 1fr)", height: "70vh", overflow: "hidden" }}>

                {/* LEFT: Datos ingresados (editable) */}
                <div style={{ padding: "18px 20px", borderRight: "1px solid #e5e7eb", overflowY: "auto", background: "#fff" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 14 }}>
                    <div style={{ width: 3, height: 14, background: "#2563eb", borderRadius: 2 }} />
                    <span style={{ fontWeight: 700, fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em", color: "#2563eb" }}>Datos Ingresados / Registrados</span>
                  </div>

                  <InputField label="Sueldo Base del contrato (S/)" field="sueldoBase" step="1" disabled />
                  {datosBase.horasTrabajadas <= 0 && datosBase.horasExtra + datosBase.ajusteHorasExtra <= 0 && (
                    <div style={{ background: "#fffbeb", border: "1px solid #fcd34d", borderRadius: 6, padding: "8px 10px", fontSize: 12, color: "#92400e", marginBottom: 10 }}>
                      ⚠️ Sin asistencias registradas en el período: el ajuste pagará el sueldo completo del contrato. Confirme antes de guardar.
                    </div>
                  )}
                  <InputField label="Horas Trabajadas" field="horasTrabajadas" step="0.5" />
                  <InputField label="Horas Extra (asistencia)" field="horasExtra" step="0.5" disabled onInfo={abrirDetalleHorasExtra} />
                  <InputField label="Horas Extra a sumar (ajuste)" field="ajusteHorasExtra" step="0.5" />
                  <InputField label="Tardanzas (minutos)" field="minutosLate" step="1" parse={v => Math.max(0, parseInt(v) || 0)} />
                  <InputField label="Cantidad de Guardias" field="cantidadGuardias" step="1" parse={v => Math.max(0, parseInt(v) || 0)} disabled={!esClinico} />

                  <ToggleGroup label="Tipo de Pensión" field="tipoPension"
                    options={[
                      { val: "ONP", label: "ONP  (13%)" },
                      { val: "AFP", label: "AFP  (12%)" },
                    ]}
                  />

                  <button onClick={resetearValores}
                    style={{ width: "100%", marginTop: 6, padding: "7px 0", border: "1px solid #64748b", borderRadius: 8, background: "#f9fafb", color: "#6b7280", fontWeight: 600, fontSize: 11, cursor: "pointer" }}>
                    ↺ Restablecer valores originales
                  </button>
                </div>

                {/* RIGHT: Cálculos automáticos (live) */}
                <div style={{ padding: "18px 20px", overflowY: "auto", background: "#f8fafc" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 14 }}>
                    <div style={{ width: 3, height: 14, background: "#16a34a", borderRadius: 2 }} />
                    <span style={{ fontWeight: 700, fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em", color: "#16a34a" }}>Cálculos Automáticos</span>
                  </div>

                  <div style={{ fontSize: 9, fontWeight: 700, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6 }}>Bonificaciones</div>
                  <CalcRow label="Horas Extra"
                    formula={horasExtraNoDisponibles
                      ? "No disponible: error al cargar el detalle por turno"
                      : !horasExtraListas
                      ? "Cargando detalle por turno…"
                      : `${horasExtraTotal}h por turno: 1.5x hasta ${umbralHorasDobles}h y 2.0x después`}
                    val={montoHorasExtra} />
                  <CalcRow label="Bonif. Familiar"
                    formula={Number(n.cantidadHijos) > 0
                      ? `10% de la RMV, una sola vez (${n.cantidadHijos} hijo(s) elegible(s))`
                      : "No aplica (sin hijos elegibles validados)"}
                    val={bFam} />
                  {esNocturno
                    ? <CalcRow label="Bonif. Nocturna" formula={`15% × ${fmt(sueldoDevengado)} devengado (turno nocturno)`} val={bNoct} />
                    : <CalcRow label="Bonif. Nocturna" formula="Solo para empleados con turno nocturno (≥ 19:00)" tipo="blocked" />}
                  {esClinico
                    ? <CalcRow label="Bonif. Guardia" formula={`${db.cantidadGuardias} guardia(s) × S/ 50.00`} val={bGuard} />
                    : <CalcRow label="Bonif. Guardia" formula="Solo para departamentos clínicos" tipo="blocked" />}
                  {esClinico
                    ? <CalcRow label="Bonif. Riesgo" formula={`10% × ${fmt(sueldoDevengado)} devengado`} val={bRiesgo} />
                    : <CalcRow label="Bonif. Riesgo" formula="Solo para departamentos clínicos" tipo="blocked" />}
                  {bCargo > 0
                    ? <CalcRow label="Bonif. Cargo" formula="20% × sueldo devengado (roles RRHH y GERENCIA)" val={bCargo} />
                    : <CalcRow label="Bonif. Cargo" formula="Solo para roles RRHH y GERENCIA" tipo="blocked" />}

                  <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 11px", background: "#dcfce7", borderRadius: 8, margin: "8px 0 12px", fontWeight: 700, fontSize: 12 }}>
                    <span style={{ color: "#15803d" }}>Total Bruto</span>
                    <span style={{ color: "#15803d" }}>{fmt(bruto)}</span>
                  </div>

                  <div style={{ fontSize: 9, fontWeight: 700, color: "#9ca3af", textTransform: "uppercase", letterSpacing: "0.08em", marginBottom: 6 }}>Descuentos</div>
                  <CalcRow label={`Desc. de Ley (${db.tipoPension})`}
                    formula={`${(tasa * 100).toFixed(0)}% × ${fmt(bruto)} (bruto)`}
                    val={descLey} tipo="desc" />
                  <CalcRow label="Desc. Tardanzas"
                    formula={`${db.minutosLate} min × ${fmt(valorMin)}/min`}
                    val={descTard} tipo="desc" />

                  <div style={{ display: "flex", justifyContent: "space-between", padding: "8px 11px", background: "#fee2e2", borderRadius: 8, marginTop: 8, fontWeight: 700, fontSize: 12 }}>
                    <span style={{ color: "#b91c1c" }}>Total Descuentos</span>
                    <span style={{ color: "#b91c1c" }}>− {fmt(descTard + descLey)}</span>
                  </div>

                  <div style={{ background: "linear-gradient(135deg, #1e40af 0%, #2563eb 100%)", borderRadius: 12, padding: "15px 18px", marginTop: 12, display: "flex", justifyContent: "space-between", alignItems: "center", boxShadow: "0 4px 14px rgba(37,99,235,0.25)" }}>
                    <div>
                      <div style={{ color: "rgba(255,255,255,0.7)", fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em" }}>Sueldo Neto</div>
                      <div style={{ color: "rgba(255,255,255,0.5)", fontSize: 10, marginTop: 3 }}>{fmt(bruto)} − {fmt(descTard + descLey)}</div>
                    </div>
                    <div style={{ color: "#fff", fontWeight: 800, fontSize: 26, lineHeight: 1 }}>{fmt(neto)}</div>
                  </div>
                </div>

                {/* RIGHT 2: Historial de versiones de la nómina */}
                <div style={{ padding: "18px 16px", borderLeft: "1px solid #e5e7eb", overflowY: "auto", background: "#fff" }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 14 }}>
                    <div style={{ width: 3, height: 14, background: "#7c3aed", borderRadius: 2 }} />
                    <span style={{ fontWeight: 700, fontSize: 10, textTransform: "uppercase", letterSpacing: "0.08em", color: "#7c3aed" }}>Versiones</span>
                  </div>

                  {versionesNomina.length === 0 && (
                    <div style={{ fontSize: 12, color: "#6b7280" }}>Sin versiones registradas.</div>
                  )}

                  {versionesNomina.map(v => (
                    <div key={v.version} style={{ border: "1px solid #e5e7eb", borderRadius: 8, padding: "10px 12px", marginBottom: 8, background: v.vigente ? "#eff6ff" : "#fff" }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", fontSize: 12, fontWeight: 700 }}>
                        <span>Versión {v.version}</span>
                        {v.vigente && (
                          <span style={{ fontSize: 9, fontWeight: 700, textTransform: "uppercase", background: "#2563eb", color: "#fff", borderRadius: 4, padding: "2px 6px" }}>Vigente</span>
                        )}
                      </div>
                      <div style={{ fontSize: 11, color: "#6b7280", marginTop: 3 }}>
                        {v.reemplazadoEl
                          ? `Reemplazada el ${new Date(v.reemplazadoEl).toLocaleString("es-PE")}`
                          : "Versión actual"}
                      </div>
                      <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: "3px 8px", marginTop: 8, fontSize: 11, fontVariantNumeric: "tabular-nums" }}>
                        <span style={{ color: "#6b7280" }}>Sueldo base</span><span>{fmt(v.sueldoBase)}</span>
                        <span style={{ color: "#6b7280" }}>Horas extra</span><span>{Number(v.totalHorasExtra || 0)} h</span>
                        <span style={{ color: "#6b7280" }}>Tardanza</span><span>{v.totalMinutosTardanza || 0} min</span>
                        <span style={{ color: "#6b7280" }}>Desc. tardanzas</span><span>{fmt(v.descuentoTardanzas)}</span>
                        <span style={{ color: "#6b7280" }}>Desc. ley</span><span>{fmt(v.descuentoLey)}</span>
                        <span style={{ color: "#6b7280" }}>Bruto</span><span>{fmt(v.sueldoBruto)}</span>
                        <span style={{ fontWeight: 700 }}>Neto</span><span style={{ fontWeight: 700 }}>{fmt(v.sueldoNeto)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Footer */}
              <div style={{ padding: "12px 24px", borderTop: "1px solid #e5e7eb", display: "flex", justifyContent: "flex-end", background: "#fff" }}>
                <button className="btn btn-primary" onClick={() => handleGuardarAjuste()} disabled={guardandoAjuste} style={{ marginRight: 10 }}>
                  {guardandoAjuste ? "Guardando..." : "Guardar cambios"}
                </button>
                <button className="btn btn-secondary" onClick={() => setModalEstructura(null)}>Cerrar</button>
              </div>
            </div>
          </AccessibleDialog>
        );
      })()}

      {/* ══ MODAL: DESGLOSE DE HORAS EXTRA ══ */}
      {mostrarDetalleHorasExtra && (
        <AccessibleDialog className="modal-overlay" labelledBy="nomina-horas-extra-title" onClose={() => setMostrarDetalleHorasExtra(false)}>
          <div className="modal-box" style={{ maxWidth: 680 }}>
            <h3 id="nomina-horas-extra-title" style={{ marginTop: 0 }}>Horas extra por horario</h3>
            {detalleHorasExtra === null ? (
              <p>Cargando…</p>
            ) : detalleHorasExtra.length === 0 ? (
              <p>Sin horas extra liquidadas en el período.</p>
            ) : (
              <div style={{ overflowX: "auto" }}>
                <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
                  <thead>
                    <tr style={{ textAlign: "left", borderBottom: "1px solid #e5e7eb" }}>
                      <th style={{ padding: "6px 8px" }}>Fechas</th>
                      <th style={{ padding: "6px 8px" }}>Entrada</th>
                      <th style={{ padding: "6px 8px" }}>Salida</th>
                      <th style={{ padding: "6px 8px", textAlign: "right" }}>Horas extra</th>
                      <th style={{ padding: "6px 8px" }}>Nota</th>
                    </tr>
                  </thead>
                  <tbody>
                    {detalleHorasExtra.map((h, i) => (
                      <tr key={i} style={{ borderBottom: "1px solid #f3f4f6" }}>
                        <td style={{ padding: "6px 8px" }}>
                          {h.tipo === "AJUSTE_MANUAL"
                            ? "Ajuste manual"
                            : h.fechaFin && h.fechaFin !== h.fechaInicio
                              ? `${fmtFechaDetalle(h.fechaInicio)} – ${fmtFechaDetalle(h.fechaFin)}`
                              : fmtFechaDetalle(h.fechaInicio)}
                        </td>
                        <td style={{ padding: "6px 8px" }}>{h.horaEntrada ? h.horaEntrada.slice(0, 5) : "—"}</td>
                        <td style={{ padding: "6px 8px" }}>{h.horaSalida ? h.horaSalida.slice(0, 5) : "—"}</td>
                        <td style={{ padding: "6px 8px", textAlign: "right", fontVariantNumeric: "tabular-nums" }}>{Number(h.horas).toFixed(2)}</td>
                        <td style={{ padding: "6px 8px", color: "#6b7280" }}>{h.tieneCorrecciones ? "Corregido" : (h.motivo ?? "")}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <div style={{ marginTop: 14, textAlign: "right" }}>
              <button type="button" className="btn btn-secondary" onClick={() => setMostrarDetalleHorasExtra(false)}>Cerrar</button>
            </div>
          </div>
        </AccessibleDialog>
      )}

      {/* ══ MODAL: ERROR ══ */}
      {errorModal.open && (
        <AccessibleDialog className="modal-overlay" labelledBy="nomina-error-title" describedBy="nomina-error-message" onClose={closeErrorModal}>
          <div className="modal-box" style={{ maxWidth: 420 }}>
            <div className="modal-header">
              <div><h3 id="nomina-error-title">⚠️ Error de validación</h3></div>
              <button type="button" className="modal-close" aria-label="Cerrar error" onClick={closeErrorModal}>×</button>
            </div>
            <div className="modal-body">
              <p id="nomina-error-message" style={{ fontSize: 14, color: "var(--text-1)", margin: 0 }}>{errorModal.message}</p>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-primary" onClick={closeErrorModal}>Entendido</button>
            </div>
          </div>
        </AccessibleDialog>
      )}

      {/* Modal de Lectura Fácil y Asistente por Voz ODS 10 */}
      <PayrollAudioModal
        nomina={audioModal.nomina}
        periodoLabel={audioModal.periodoLabel}
        isOpen={audioModal.open}
        onClose={() => setAudioModal({ open: false, nomina: null, periodoLabel: "" })}
      />
    </div>
  );
}
