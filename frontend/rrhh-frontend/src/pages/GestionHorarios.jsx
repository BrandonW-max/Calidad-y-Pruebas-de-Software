import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getHorarios, crearHorario, actualizarHorario, eliminarHorario } from "../services/api";

const FORM_VACIO = {
  nombreTurno: "",
  horaEntrada: "",
  horaSalida:  "",
  tolerancia:  10,
};

export default function GestionHorarios() {
  const [horarios,  setHorarios]  = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [mensaje,   setMensaje]   = useState("");
  const [mensajeOk, setMensajeOk] = useState(true);

  const [modalForm, setModalForm] = useState({ open: false, modo: "crear", data: null });
  const [form,      setForm]      = useState(FORM_VACIO);
  const [guardando, setGuardando] = useState(false);
  const [modalElim, setModalElim] = useState({ open: false, horario: null });

  useEffect(() => { cargar(); }, []);

  const cargar = async () => {
    setLoading(true);
    try {
      const data = await getHorarios();
      setHorarios(data || []);
    } catch {
      setMensaje("Error al cargar los horarios");
      setMensajeOk(false);
    } finally {
      setLoading(false);
    }
  };

  const abrirCrear = () => {
    setForm(FORM_VACIO);
    setModalForm({ open: true, modo: "crear", data: null });
  };

  const abrirEditar = (h) => {
    setForm({
      nombreTurno: h.nombreTurno ?? "",
      horaEntrada: h.horaEntrada?.slice(0, 5) ?? "",
      horaSalida:  h.horaSalida?.slice(0, 5) ?? "",
      tolerancia:  h.tolerancia ?? 10,
    });
    setModalForm({ open: true, modo: "editar", data: h });
  };

  const cerrarModal = () => setModalForm(m => ({ ...m, open: false }));

  const handleGuardar = async (e) => {
    e.preventDefault();
    setGuardando(true);
    const dto = {
      nombreTurno: form.nombreTurno,
      horaEntrada: form.horaEntrada,
      horaSalida:  form.horaSalida,
      tolerancia:  Number(form.tolerancia),
    };
    try {
      if (modalForm.modo === "crear") {
        await crearHorario(dto);
        setMensaje("Horario creado correctamente");
      } else {
        await actualizarHorario(modalForm.data.idHorario, dto);
        setMensaje("Horario actualizado correctamente");
      }
      setMensajeOk(true);
      cerrarModal();
      cargar();
    } catch {
      setMensaje("Error al guardar el horario");
      setMensajeOk(false);
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async () => {
    try {
      await eliminarHorario(modalElim.horario.idHorario);
      setMensaje("Horario desactivado correctamente");
      setMensajeOk(true);
      setModalElim({ open: false, horario: null });
      cargar();
    } catch {
      setMensaje("Error al desactivar el horario");
      setMensajeOk(false);
      setModalElim({ open: false, horario: null });
    }
  };

  const duracionHoras = (entrada, salida) => {
    if (!entrada || !salida) return null;
    const [h1, m1] = entrada.split(":").map(Number);
    const [h2, m2] = salida.split(":").map(Number);
    let mins = (h2 * 60 + m2) - (h1 * 60 + m1);
    if (mins <= 0) mins += 24 * 60;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return m > 0 ? `${h}h ${m}min` : `${h}h`;
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Gestión de Empleados / Horarios</span>
            <h1>Gestión de Horarios</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Defina los turnos disponibles: nombre, hora de entrada y hora de salida.
              Las fechas de vigencia se gestionan al asignar el turno a un empleado.
            </p>
          </div>
          <button className="btn btn-success" onClick={abrirCrear}>＋ Nuevo Turno</button>
        </header>

        {mensaje && (
          <div className={`alert ${mensajeOk ? "alert-success" : "alert-warning"}`}
            style={{ cursor: "pointer" }} onClick={() => setMensaje("")}>
            {mensajeOk ? "✅" : "⚠️"} {mensaje}
          </div>
        )}

        {/* ── TARJETAS RESUMEN ── */}
        {!loading && horarios.length > 0 && (
          <div style={{ display: "flex", gap: 16, marginBottom: 20, flexWrap: "wrap" }}>
            {horarios.map(h => (
              <div key={h.idHorario} style={{
                background: "#fff", border: "1px solid var(--border)", borderRadius: 12,
                padding: "16px 20px", minWidth: 180, flex: "1 1 180px", maxWidth: 240,
                boxShadow: "0 1px 4px rgba(0,0,0,0.06)",
              }}>
                <div style={{ fontSize: 22, marginBottom: 6 }}>🕐</div>
                <div style={{ fontWeight: 700, fontSize: 14, color: "var(--text-1)", marginBottom: 4 }}>
                  {h.nombreTurno}
                </div>
                <div style={{ fontSize: 13, color: "var(--primary)", fontWeight: 600, marginBottom: 2 }}>
                  {h.horaEntrada?.slice(0,5)} – {h.horaSalida?.slice(0,5)}
                </div>
                <div style={{ fontSize: 11, color: "var(--text-3)" }}>
                  Duración: {duracionHoras(h.horaEntrada?.slice(0,5), h.horaSalida?.slice(0,5)) ?? "—"}
                </div>
                <div style={{ fontSize: 11, color: "var(--text-3)" }}>
                  Tolerancia: {h.tolerancia ?? 10} min
                </div>
              </div>
            ))}
          </div>
        )}

        {/* ── TABLA ── */}
        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div className="table-wrapper" style={{ marginBottom: 0 }}>
            {loading ? (
              <div className="loading-text">Cargando horarios...</div>
            ) : horarios.length === 0 ? (
              <div className="empty-state" style={{ padding: "48px 0" }}>
                <div className="empty-state-icon">🕐</div>
                <p>No hay turnos registrados. Crea el primero.</p>
                <button className="btn btn-success" style={{ marginTop: 16 }} onClick={abrirCrear}>
                  ＋ Crear turno
                </button>
              </div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Nombre del turno</th>
                    <th style={{ textAlign: "center" }}>Hora entrada</th>
                    <th style={{ textAlign: "center" }}>Hora salida</th>
                    <th style={{ textAlign: "center" }}>Duración</th>
                    <th style={{ textAlign: "center" }}>Tolerancia</th>
                    <th style={{ textAlign: "center" }}>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {horarios.map(h => {
                    const dur = duracionHoras(h.horaEntrada?.slice(0,5), h.horaSalida?.slice(0,5));
                    return (
                      <tr key={h.idHorario}>
                        <td style={{ fontWeight: 600 }}>{h.nombreTurno}</td>
                        <td style={{ textAlign: "center" }}>
                          <span style={{
                            background: "#eff6ff", color: "#2563eb",
                            borderRadius: 6, padding: "3px 12px", fontSize: 13, fontWeight: 600,
                          }}>
                            {h.horaEntrada?.slice(0,5) ?? "—"}
                          </span>
                        </td>
                        <td style={{ textAlign: "center" }}>
                          <span style={{
                            background: "#f0fdf4", color: "#16a34a",
                            borderRadius: 6, padding: "3px 12px", fontSize: 13, fontWeight: 600,
                          }}>
                            {h.horaSalida?.slice(0,5) ?? "—"}
                          </span>
                        </td>
                        <td style={{ textAlign: "center", fontSize: 13, color: "var(--text-2)" }}>
                          {dur ?? "—"}
                        </td>
                        <td style={{ textAlign: "center", fontSize: 13, color: "var(--text-2)" }}>
                          {h.tolerancia ?? 10} min
                        </td>
                        <td style={{ textAlign: "center" }}>
                          <div style={{ display: "inline-flex", gap: 6 }}>
                            <button className="btn btn-secondary btn-sm" onClick={() => abrirEditar(h)}>
                              ✏️ Editar
                            </button>
                            <button
                              className="btn btn-sm"
                              style={{ background: "#fee2e2", color: "#dc2626", border: "1px solid #fca5a5" }}
                              onClick={() => setModalElim({ open: true, horario: h })}
                            >
                              🗑
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
          {!loading && horarios.length > 0 && (
            <div className="pagination-bar">
              <div className="pagination-info">{horarios.length} turno(s) registrado(s)</div>
            </div>
          )}
        </div>
      </main>

      {/* ══ MODAL: CREAR / EDITAR ══ */}
      {modalForm.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && cerrarModal()}>
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>{modalForm.modo === "crear" ? "➕ Nuevo Turno" : "✏️ Editar Turno"}</h3>
                <p style={{ margin: 0, fontSize: 13, color: "var(--text-3)" }}>
                  {modalForm.modo === "crear"
                    ? "Define el nombre y el horario del turno."
                    : `Editando: ${modalForm.data?.nombreTurno}`}
                </p>
              </div>
              <button className="modal-close" onClick={cerrarModal}>×</button>
            </div>

            <div className="modal-body">
              {modalForm.modo === "crear" && (
                <div className="alert alert-info" style={{ marginBottom: 16 }}>
                  Las fechas de vigencia (desde / hasta) se configuran al asignar
                  el turno a un empleado desde <strong>Gestión de Empleados</strong>.
                </div>
              )}
              <form id="form-horario-gh" onSubmit={handleGuardar}>
                <div className="form-grid">
                  <div className="form-group form-full">
                    <label>Nombre del turno *</label>
                    <input
                      value={form.nombreTurno}
                      onChange={e => setForm(f => ({ ...f, nombreTurno: e.target.value }))}
                      placeholder="Ej: Turno Mañana, Turno Tarde, Turno Noche..."
                      required
                      autoFocus
                    />
                  </div>
                  <div className="form-group">
                    <label>Hora de entrada *</label>
                    <input
                      type="time"
                      value={form.horaEntrada}
                      onChange={e => setForm(f => ({ ...f, horaEntrada: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Hora de salida *</label>
                    <input
                      type="time"
                      value={form.horaSalida}
                      onChange={e => setForm(f => ({ ...f, horaSalida: e.target.value }))}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label>Tolerancia por llegada tarde (min)</label>
                    <input
                      type="number" min="0" max="60"
                      value={form.tolerancia}
                      onChange={e => setForm(f => ({ ...f, tolerancia: e.target.value }))}
                    />
                  </div>
                  {form.horaEntrada && form.horaSalida && (
                    <div className="form-group" style={{ display: "flex", alignItems: "center" }}>
                      <div style={{
                        background: "#eff6ff", border: "1px solid #bfdbfe",
                        borderRadius: 8, padding: "10px 14px", fontSize: 13,
                        color: "#1e40af", width: "100%",
                      }}>
                        ⏱ Duración del turno:{" "}
                        <strong>
                          {duracionHoras(form.horaEntrada, form.horaSalida)}
                        </strong>
                      </div>
                    </div>
                  )}
                </div>
              </form>
            </div>

            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={cerrarModal} disabled={guardando}>
                Cancelar
              </button>
              <button className="btn btn-success" type="submit" form="form-horario-gh" disabled={guardando}>
                {guardando
                  ? "Guardando..."
                  : modalForm.modo === "crear" ? "✅ Crear Turno" : "💾 Guardar Cambios"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: CONFIRMAR DESACTIVAR ══ */}
      {modalElim.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>🗑 Desactivar Turno</h3></div>
              <button className="modal-close" onClick={() => setModalElim({ open: false, horario: null })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">
                ⚠️ El turno <strong>{modalElim.horario?.nombreTurno}</strong> quedará inactivo
                y no podrá ser asignado a nuevos empleados. Los empleados con este turno activo
                conservarán su asignación hasta que sea cambiada manualmente.
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalElim({ open: false, horario: null })}>
                Cancelar
              </button>
              <button className="btn btn-danger" onClick={handleEliminar}>
                Confirmar desactivar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
