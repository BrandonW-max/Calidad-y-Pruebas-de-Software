import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import {
  listarEmpleados,
  buscarEmpleados,
  buscarEmpleadosAvanzado,
  registrarEmpleado,
  actualizarEmpleado,
  desactivarEmpleado,
  reactivarEmpleado,
  registrarSancion,
  registrarAscenso,
  cambioSalarial,
} from "../services/api";

const FORM_VACIO = {
  nombres: "", apellidos: "", docIdentidad: "DNI", numeroDi: "",
  fechaNac: "", sexo: "", estadoCivil: "", direccion: "",
  correo: "", telefono: "", cargo: "", tipoContrato: "",
  fechaInicio: "", fechaFin: "", sueldo: "", idDpto: "", contrasenia: "",
};

export default function GestionEmpleados() {
  const [empleados, setEmpleados]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState("");
  const [mensaje, setMensaje]       = useState("");
  const [tabActiva, setTabActiva]   = useState("lista");

  const [filtroTexto, setFiltroTexto]   = useState("");
  const [filtroEstado, setFiltroEstado] = useState("");
  const [filtroCargo, setFiltroCargo]   = useState("");

  const [form, setForm]           = useState(FORM_VACIO);
  const [sancionForm, setSancionForm] = useState({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
  const [ascensoForm, setAscensoForm] = useState({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });

  const [modalDesactivar, setModalDesactivar] = useState({ open: false, id: null, motivo: "" });
  const [modalReactivar,  setModalReactivar]  = useState({ open: false, id: null });
  const [modalEditar, setModalEditar]         = useState(null);

  useEffect(() => { cargarEmpleados(); }, []);

  const cargarEmpleados = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await listarEmpleados();
      setEmpleados(data.content ?? data);
    } catch {
      setError("No se pudo cargar la lista de empleados");
    } finally {
      setLoading(false);
    }
  };

  const handleBuscar = async () => {
    setLoading(true);
    setError("");
    try {
      let data;
      if (filtroTexto) {
        data = await buscarEmpleados(filtroTexto);
      } else {
        data = await buscarEmpleadosAvanzado({ estado: filtroEstado, cargo: filtroCargo });
      }
      setEmpleados(data.content ?? data);
    } catch {
      setError("Error al buscar empleados");
    } finally {
      setLoading(false);
    }
  };

  const handleRegistrar = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarEmpleado({
        ...form,
        sueldo: parseFloat(form.sueldo),
        idDpto: parseInt(form.idDpto),
        fechaNac: form.fechaNac || null,
        fechaInicio: form.fechaInicio || null,
        fechaFin: form.fechaFin || null,
      });
      setMensaje("Empleado registrado correctamente");
      setForm(FORM_VACIO);
      cargarEmpleados();
      setTabActiva("lista");
    } catch {
      setMensaje("Error al registrar empleado. Verifique los datos.");
    }
  };

  const handleDesactivar = (id) => setModalDesactivar({ open: true, id, motivo: "" });

  const confirmarDesactivar = async () => {
    if (!modalDesactivar.motivo.trim()) return;
    try {
      await desactivarEmpleado(modalDesactivar.id, modalDesactivar.motivo);
      setMensaje("Empleado desactivado correctamente");
      setModalDesactivar({ open: false, id: null, motivo: "" });
      cargarEmpleados();
    } catch {
      setMensaje("Error al desactivar el empleado");
      setModalDesactivar({ open: false, id: null, motivo: "" });
    }
  };

  const handleReactivar = (id) => setModalReactivar({ open: true, id });

  const confirmarReactivar = async () => {
    try {
      await reactivarEmpleado(modalReactivar.id, "Reactivación manual");
      setMensaje("Empleado reactivado correctamente");
      setModalReactivar({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al reactivar el empleado");
      setModalReactivar({ open: false, id: null });
    }
  };

  const handleGuardarEdicion = async (e) => {
    e.preventDefault();
    if (!modalEditar) return;
    try {
      await actualizarEmpleado(modalEditar.idEmpleado, {
        cargo: modalEditar.cargo,
        correo: modalEditar.correo,
        telefono: modalEditar.telefono,
        direccion: modalEditar.direccion,
        tipoContrato: modalEditar.tipoContrato,
        sueldo: parseFloat(modalEditar.sueldo),
        idDpto: parseInt(modalEditar.idDpto),
      });
      setMensaje("Datos del empleado actualizados correctamente");
      setModalEditar(null);
      cargarEmpleados();
    } catch {
      setMensaje("Error al actualizar el empleado. Verifique los datos.");
    }
  };

  const handleSancion = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarSancion(sancionForm.idEmpleado, {
        tipoSancion: sancionForm.tipoSancion,
        diasSuspension: parseInt(sancionForm.diasSuspension),
        justificacion: sancionForm.justificacion,
      });
      setMensaje("Sanción registrada correctamente");
      setSancionForm({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
    } catch {
      setMensaje("Error al registrar la sanción");
    }
  };

  const handleAscenso = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarAscenso(ascensoForm.idEmpleado, { nuevoCargo: ascensoForm.nuevoCargo });
      await cambioSalarial(ascensoForm.idEmpleado, { nuevoSueldo: parseFloat(ascensoForm.nuevoSueldo) });
      setMensaje("Ascenso y cambio salarial registrados correctamente");
      setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar el ascenso");
    }
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">RRHH / Gestión de Empleados</span>
            <h1>Gestión de Empleados</h1>
          </div>
          <span className="role-pill role-pill-rrhh">ECU-02</span>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ── TAB BAR ── */}
        <div className="tab-bar">
          <button className={`tab ${tabActiva === "lista"    ? "active" : ""}`} onClick={() => setTabActiva("lista")}>
            👥 Lista de Empleados
          </button>
          <button className={`tab ${tabActiva === "registrar" ? "active" : ""}`} onClick={() => setTabActiva("registrar")}>
            ➕ Registrar Empleado
          </button>
          <button className={`tab ${tabActiva === "sancion"  ? "active" : ""}`} onClick={() => setTabActiva("sancion")}>
            🚫 Sanciones
          </button>
          <button className={`tab ${tabActiva === "ascenso"  ? "active" : ""}`} onClick={() => setTabActiva("ascenso")}>
            📈 Ascensos / Sueldo
          </button>
        </div>

        {/* ══ LISTA Y BÚSQUEDA ══ */}
        {tabActiva === "lista" && (
          <div className="section-card">
            <h2>👥 Buscar y Listar Empleados</h2>
            <div className="toolbar">
              <input
                type="text"
                placeholder="Buscar por DNI o nombre..."
                value={filtroTexto}
                onChange={(e) => setFiltroTexto(e.target.value)}
                style={{ minWidth: "200px" }}
              />
              <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
                <option value="">Todos los estados</option>
                <option value="ACTIVO">Activo</option>
                <option value="INACTIVO">Inactivo</option>
              </select>
              <input
                type="text"
                placeholder="Filtrar por cargo..."
                value={filtroCargo}
                onChange={(e) => setFiltroCargo(e.target.value)}
              />
              <button className="btn btn-primary" onClick={handleBuscar}>🔍 Buscar</button>
              <button className="btn btn-secondary" onClick={() => { setFiltroTexto(""); setFiltroEstado(""); setFiltroCargo(""); cargarEmpleados(); }}>
                Limpiar
              </button>
            </div>

            <div className="table-wrapper">
              {loading ? (
                <div className="loading-text">Cargando empleados...</div>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>DNI / Documento</th>
                      <th>Apellidos y Nombres</th>
                      <th>Departamento</th>
                      <th>Cargo</th>
                      <th>Estado</th>
                      <th>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {empleados.length === 0 ? (
                      <tr>
                        <td colSpan={6}>
                          <div className="empty-state">
                            <div className="empty-state-icon">🔍</div>
                            <p>No se encontraron resultados</p>
                          </div>
                        </td>
                      </tr>
                    ) : empleados.map((emp) => (
                      <tr key={emp.idEmpleado}>
                        <td><strong>{emp.numeroDi}</strong></td>
                        <td>{emp.apellidos}, {emp.nombres}</td>
                        <td>{emp.departamento ?? "—"}</td>
                        <td>{emp.cargo}</td>
                        <td>
                          <span className={`badge ${emp.estado === "ACTIVO" ? "badge-success" : "badge-danger"}`}>
                            {emp.estado}
                          </span>
                        </td>
                        <td style={{ display: "flex", gap: "6px" }}>
                          <button className="btn btn-secondary btn-sm" onClick={() => setModalEditar({ ...emp })}>
                            ✏️ Editar
                          </button>
                          {emp.estado === "ACTIVO" ? (
                            <button className="btn btn-danger btn-sm" onClick={() => handleDesactivar(emp.idEmpleado)}>
                              Desactivar
                            </button>
                          ) : (
                            <button className="btn btn-success btn-sm" onClick={() => handleReactivar(emp.idEmpleado)}>
                              Reactivar
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* ══ REGISTRAR EMPLEADO ══ */}
        {tabActiva === "registrar" && (
          <div className="section-card">
            <h2>➕ Registrar Nuevo Empleado</h2>
            <div className="alert alert-info">
              Complete todos los campos obligatorios marcados con *. El sistema verificará la duplicidad del número de documento.
            </div>
            <form onSubmit={handleRegistrar}>
              <div className="form-grid">
                <div className="form-group">
                  <label>Tipo Documento *</label>
                  <select value={form.docIdentidad} onChange={(e) => setForm({ ...form, docIdentidad: e.target.value })}>
                    <option value="DNI">DNI</option>
                    <option value="CE">Carnet de Extranjería</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Número Documento *</label>
                  <input value={form.numeroDi} onChange={(e) => setForm({ ...form, numeroDi: e.target.value })} placeholder="Ej: 12345678" required />
                </div>
                <div className="form-group">
                  <label>Nombres *</label>
                  <input value={form.nombres} onChange={(e) => setForm({ ...form, nombres: e.target.value })} placeholder="Nombres completos" required />
                </div>
                <div className="form-group">
                  <label>Apellidos *</label>
                  <input value={form.apellidos} onChange={(e) => setForm({ ...form, apellidos: e.target.value })} placeholder="Apellidos completos" required />
                </div>
                <div className="form-group">
                  <label>Fecha de Nacimiento</label>
                  <input type="date" value={form.fechaNac} onChange={(e) => setForm({ ...form, fechaNac: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Sexo</label>
                  <select value={form.sexo} onChange={(e) => setForm({ ...form, sexo: e.target.value })}>
                    <option value="">Seleccione</option>
                    <option value="M">Masculino</option>
                    <option value="F">Femenino</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Correo Electrónico</label>
                  <input type="email" value={form.correo} onChange={(e) => setForm({ ...form, correo: e.target.value })} placeholder="correo@ejemplo.com" />
                </div>
                <div className="form-group">
                  <label>Teléfono</label>
                  <input value={form.telefono} onChange={(e) => setForm({ ...form, telefono: e.target.value })} placeholder="999 999 999" />
                </div>
                <div className="form-group">
                  <label>Cargo *</label>
                  <input value={form.cargo} onChange={(e) => setForm({ ...form, cargo: e.target.value })} placeholder="Ej: Enfermero, Médico..." required />
                </div>
                <div className="form-group">
                  <label>Tipo de Contrato</label>
                  <select value={form.tipoContrato} onChange={(e) => setForm({ ...form, tipoContrato: e.target.value })}>
                    <option value="">Seleccione</option>
                    <option value="PLAZO_FIJO">Plazo fijo</option>
                    <option value="INDEFINIDO">Indefinido</option>
                    <option value="SERVICIOS">Por servicio</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Fecha Inicio Contrato</label>
                  <input type="date" value={form.fechaInicio} onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Sueldo Base (S/) *</label>
                  <input type="number" step="0.01" min="0" value={form.sueldo} onChange={(e) => setForm({ ...form, sueldo: e.target.value })} placeholder="0.00" required />
                </div>
                <div className="form-group">
                  <label>ID Departamento *</label>
                  <input type="number" value={form.idDpto} onChange={(e) => setForm({ ...form, idDpto: e.target.value })} placeholder="Ej: 1" required />
                </div>
                <div className="form-group">
                  <label>Contraseña Inicial</label>
                  <input type="password" value={form.contrasenia} onChange={(e) => setForm({ ...form, contrasenia: e.target.value })} placeholder="Contraseña de acceso al sistema" />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-success">✅ Registrar Empleado</button>
                <button type="button" className="btn btn-secondary" onClick={() => setForm(FORM_VACIO)}>
                  Limpiar formulario
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ══ SANCIONES ══ */}
        {tabActiva === "sancion" && (
          <div className="section-card">
            <h2>🚫 Registrar Sanción</h2>
            <div className="alert alert-warning">
              ⚠️ La justificación es obligatoria. Si la sanción incluye días de suspensión, el acceso al sistema quedará restringido durante ese periodo.
            </div>
            <form onSubmit={handleSancion}>
              <div className="form-grid">
                <div className="form-group">
                  <label>ID Empleado *</label>
                  <input
                    type="number"
                    value={sancionForm.idEmpleado}
                    onChange={(e) => setSancionForm({ ...sancionForm, idEmpleado: e.target.value })}
                    placeholder="Ingrese el ID del empleado"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Tipo de Sanción *</label>
                  <select
                    value={sancionForm.tipoSancion}
                    onChange={(e) => setSancionForm({ ...sancionForm, tipoSancion: e.target.value })}
                    required
                  >
                    <option value="">Seleccione el tipo</option>
                    <option value="VERBAL">Amonestación verbal</option>
                    <option value="ESCRITA">Amonestación escrita</option>
                    <option value="SUSPENSION">Suspensión (días)</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Días de Suspensión</label>
                  <input
                    type="number"
                    min="0"
                    value={sancionForm.diasSuspension}
                    onChange={(e) => setSancionForm({ ...sancionForm, diasSuspension: e.target.value })}
                    placeholder="0 si no aplica"
                  />
                </div>
                <div className="form-group form-full">
                  <label>Justificación *</label>
                  <textarea
                    value={sancionForm.justificacion}
                    onChange={(e) => setSancionForm({ ...sancionForm, justificacion: e.target.value })}
                    placeholder="Describa detalladamente el motivo de la sanción..."
                    required
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-danger">🚫 Registrar Sanción</button>
                <button type="button" className="btn btn-secondary" onClick={() => setSancionForm({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" })}>
                  Limpiar
                </button>
              </div>
            </form>
          </div>
        )}

        {/* ══ ASCENSO / CAMBIO SALARIAL ══ */}
        {tabActiva === "ascenso" && (
          <div className="section-card">
            <h2>📈 Registrar Ascenso / Cambio Salarial</h2>
            <div className="alert alert-info">
              El cambio de cargo y sueldo quedará registrado en el historial del empleado para auditoría.
            </div>
            <form onSubmit={handleAscenso}>
              <div className="form-grid">
                <div className="form-group">
                  <label>ID Empleado *</label>
                  <input
                    type="number"
                    value={ascensoForm.idEmpleado}
                    onChange={(e) => setAscensoForm({ ...ascensoForm, idEmpleado: e.target.value })}
                    placeholder="Ingrese el ID del empleado"
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Nuevo Cargo</label>
                  <input
                    value={ascensoForm.nuevoCargo}
                    onChange={(e) => setAscensoForm({ ...ascensoForm, nuevoCargo: e.target.value })}
                    placeholder="Ej: Jefe de Área, Especialista..."
                  />
                </div>
                <div className="form-group">
                  <label>Nuevo Sueldo (S/)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={ascensoForm.nuevoSueldo}
                    onChange={(e) => setAscensoForm({ ...ascensoForm, nuevoSueldo: e.target.value })}
                    placeholder="0.00"
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">💾 Guardar Cambios</button>
                <button type="button" className="btn btn-secondary" onClick={() => setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" })}>
                  Limpiar
                </button>
              </div>
            </form>
          </div>
        )}
      </main>

      {/* ── MODAL EDITAR ── */}
      {modalEditar && (
        <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && setModalEditar(null)}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div>
                <h3>✏️ Editar Empleado</h3>
                <p>{modalEditar.apellidos}, {modalEditar.nombres} — {modalEditar.numeroDi}</p>
              </div>
              <button className="modal-close" onClick={() => setModalEditar(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info" style={{ marginBottom: "16px" }}>
                Los campos DNI, nombres y apellidos no son editables según política del sistema.
              </div>
              <form id="form-editar" onSubmit={handleGuardarEdicion}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Cargo</label>
                    <input value={modalEditar.cargo ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, cargo: e.target.value })} placeholder="Cargo actual" />
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select value={modalEditar.tipoContrato ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, tipoContrato: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="PLAZO_FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="SERVICIOS">Por servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Sueldo Base (S/)</label>
                    <input type="number" step="0.01" min="0" value={modalEditar.sueldo ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, sueldo: e.target.value })} placeholder="0.00" />
                  </div>
                  <div className="form-group">
                    <label>ID Departamento</label>
                    <input type="number" value={modalEditar.idDpto ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, idDpto: e.target.value })} placeholder="Ej: 1" />
                  </div>
                  <div className="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" value={modalEditar.correo ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, correo: e.target.value })} placeholder="correo@ejemplo.com" />
                  </div>
                  <div className="form-group">
                    <label>Teléfono</label>
                    <input value={modalEditar.telefono ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, telefono: e.target.value })} placeholder="999 999 999" />
                  </div>
                  <div className="form-group form-full">
                    <label>Dirección</label>
                    <input value={modalEditar.direccion ?? ""} onChange={(e) => setModalEditar({ ...modalEditar, direccion: e.target.value })} placeholder="Dirección del empleado" />
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalEditar(null)}>Cancelar</button>
              <button className="btn btn-success" type="submit" form="form-editar">💾 Guardar Cambios</button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL DESACTIVAR ── */}
      {modalDesactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>Desactivar Empleado</h3>
                <p>Esta acción bloqueará el acceso del empleado al sistema.</p>
              </div>
              <button className="modal-close" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning" style={{ marginBottom: "14px" }}>
                ⚠️ El historial del empleado se conservará. El acceso al sistema quedará bloqueado durante el período de desactivación.
              </div>
              <div className="form-group">
                <label>Motivo de desactivación *</label>
                <textarea
                  rows={3}
                  value={modalDesactivar.motivo}
                  onChange={(e) => setModalDesactivar({ ...modalDesactivar, motivo: e.target.value })}
                  placeholder="Describa el motivo de la desactivación..."
                  autoFocus
                />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>Cancelar</button>
              <button className="btn btn-danger" onClick={confirmarDesactivar} disabled={!modalDesactivar.motivo.trim()}>
                Confirmar Desactivación
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── MODAL REACTIVAR ── */}
      {modalReactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div>
                <h3>Reactivar Empleado</h3>
                <p>El empleado recuperará acceso al sistema.</p>
              </div>
              <button className="modal-close" onClick={() => setModalReactivar({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: "14px", color: "#475569" }}>
                ¿Confirma la reactivación del empleado? Recuperará acceso al sistema con su rol asignado previamente.
              </p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalReactivar({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-success" onClick={confirmarReactivar}>✅ Confirmar Reactivación</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
