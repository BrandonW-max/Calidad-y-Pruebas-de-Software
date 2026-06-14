import { useState, useEffect, useRef } from "react";
import * as XLSX from "xlsx";
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
  getDepartamentos,
} from "../services/api";

const FORM_VACIO = {
  nombres: "", apellidos: "", docIdentidad: "DNI", numeroDi: "",
  fechaNac: "", sexo: "", estadoCivil: "", direccion: "",
  correo: "", telefono: "", cargo: "", tipoContrato: "",
  fechaInicio: "", fechaFin: "", sueldo: "", idDpto: "", contrasenia: "",
};

const PAGE_SIZES = [10, 25, 50];


const DEPT_CARGOS = {
  "Recursos Humanos":  ["Especialista RRHH", "Analista RRHH", "Coordinador RRHH", "Asistente RRHH", "Auxiliar RRHH", "Administrador RRHH"],
  "Dirección General": ["Director General", "Subgerente", "Gerente Administrativo", "Asistente de Gerencia"],
  "Emergencia":        ["Médico de Emergencia", "Paramédico", "Triagista", "Técnico de Emergencias"],
  "Enfermería":        ["Enfermera Jefe", "Enfermera", "Enfermero", "Auxiliar de Enfermería"],
  "Medicina":          ["Médico General", "Médico Especialista", "Médico Residente", "Médico de Guardia"],
  "Administración":    ["Administrador", "Administradora", "Recepcionista", "Asistente Administrativo", "Secretaria"],
  "Laboratorio":       ["Técnico de Laboratorio", "Analista de Laboratorio", "Biólogo", "Químico Farmacéutico"],
  "Finanzas":          ["Contador", "Contadora", "Analista Financiero", "Tesorero", "Asistente Contable"],
  "Radiología":        ["Radiólogo", "Técnico en Radiología", "Operador de Equipos de Imagen"],
};

export default function GestionEmpleados() {
  const [empleados,     setEmpleados]     = useState([]);
  const [loading,       setLoading]       = useState(true);
  const [error,         setError]         = useState("");
  const [mensaje,       setMensaje]       = useState("");
  const [totalElements, setTotalElements] = useState(0);
  const [page,          setPage]          = useState(0);
  const [pageSize,      setPageSize]      = useState(10);

  // Departamentos (cargados del backend)
  const [departamentos, setDepartamentos] = useState([]);

  // Filters
  const [searchName,  setSearchName]  = useState("");
  const [filterArea,  setFilterArea]  = useState("");
  const [filterCargo, setFilterCargo] = useState("");
  const [filterEstado, setFilterEstado] = useState("");
  const [filterDocType, setFilterDocType] = useState("");

  // Forms
  const [form,        setForm]        = useState(FORM_VACIO);
  const [sancionForm, setSancionForm] = useState({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
  const [ascensoForm, setAscensoForm] = useState({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
  const [cambioForm,  setCambioForm]  = useState({ idEmpleado: "", nuevoSueldo: "" });

  // Modals
  const [modalNuevo,      setModalNuevo]      = useState(false);
  const [modalEditar,     setModalEditar]     = useState(null);
  const [modalDesactivar, setModalDesactivar] = useState({ open: false, id: null, motivo: "" });
  const [modalReactivar,  setModalReactivar]  = useState({ open: false, id: null });
  const [modalSancion,    setModalSancion]    = useState({ open: false, id: null });
  const [modalAscenso,    setModalAscenso]    = useState({ open: false, id: null });
  const [modalCambio,     setModalCambio]     = useState({ open: false, id: null });

  // Dropdown per row
  const [openDrop, setOpenDrop] = useState(null);
  const dropRef = useRef(null);

  useEffect(() => { cargarEmpleados(); }, [page, pageSize]);

  useEffect(() => {
    getDepartamentos().then(setDepartamentos).catch(() => {});
  }, []);

  useEffect(() => {
    const handler = (e) => {
      if (dropRef.current && !dropRef.current.contains(e.target)) setOpenDrop(null);
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const cargarEmpleados = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await listarEmpleados(page, pageSize);
      if (data.content) {
        setEmpleados(data.content);
        setTotalElements(data.totalElements ?? data.content.length);
      } else {
        setEmpleados(Array.isArray(data) ? data : []);
        setTotalElements(Array.isArray(data) ? data.length : 0);
      }
    } catch {
      setError("No se pudo cargar la lista de empleados");
    } finally {
      setLoading(false);
    }
  };

  const handleBuscar = async () => {
    setLoading(true);
    setError("");
    setPage(0);
    try {
      let data;
      if (searchName) {
        data = await buscarEmpleados(searchName, 0);
      } else {
        const idDpto = filterArea
          ? departamentos.find(d => d.nombre === filterArea)?.idDpto
          : undefined;
        data = await buscarEmpleadosAvanzado({ estado: filterEstado, cargo: filterCargo, idDpto });
      }
      const list = data.content ?? (Array.isArray(data) ? data : []);
      setEmpleados(list);
      setTotalElements(data.totalElements ?? list.length);
    } catch {
      setError("Error al buscar empleados");
    } finally {
      setLoading(false);
    }
  };

  const handleLimpiar = () => {
    setSearchName(""); setFilterArea(""); setFilterCargo(""); setFilterEstado(""); setFilterDocType("");
    setPage(0);
    cargarEmpleados();
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
      setModalNuevo(false);
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar empleado. Verifique los datos.");
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
      setMensaje("Error al actualizar el empleado.");
    }
  };

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

  const handleSancion = async (e) => {
    e.preventDefault();
    try {
      await registrarSancion(sancionForm.idEmpleado, {
        tipoSancion: sancionForm.tipoSancion,
        diasSuspension: parseInt(sancionForm.diasSuspension),
        justificacion: sancionForm.justificacion,
      });
      setMensaje("Sanción registrada correctamente");
      setSancionForm({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
      setModalSancion({ open: false, id: null });
    } catch {
      setMensaje("Error al registrar la sanción");
    }
  };

  const handleAscenso = async (e) => {
    e.preventDefault();
    try {
      await registrarAscenso(ascensoForm.idEmpleado, { nuevoCargo: ascensoForm.nuevoCargo });
      setMensaje("Ascenso registrado correctamente");
      setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
      setModalAscenso({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar el ascenso");
    }
  };

  const handleCambioSalarial = async (e) => {
    e.preventDefault();
    try {
      await cambioSalarial(cambioForm.idEmpleado, { nuevoSueldo: parseFloat(cambioForm.nuevoSueldo) });
      setMensaje("Cambio salarial registrado correctamente");
      setCambioForm({ idEmpleado: "", nuevoSueldo: "" });
      setModalCambio({ open: false, id: null });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar el cambio salarial");
    }
  };

  const openDropMenu = (id) => { setOpenDrop(openDrop === id ? null : id); };

  const totalPages = Math.max(1, Math.ceil(totalElements / pageSize));

  const formatFecha = (str) => {
    if (!str) return "—";
    const d = new Date(str);
    return isNaN(d) ? str : `${String(d.getDate()).padStart(2,"0")}/${String(d.getMonth()+1).padStart(2,"0")}/${d.getFullYear()}`;
  };

  const formatSueldo = (v) =>
    v != null ? `S/ ${Number(v).toLocaleString("es-PE", { minimumFractionDigits: 2 })}` : "—";

  const exportarEmpleados = () => {
    if (!empleados.length) return;
    const data = empleados.map(e => ({
      "ID":             e.idEmpleado ?? "—",
      "Nombre Completo": `${e.nombres ?? ""} ${e.apellidos ?? ""}`.trim(),
      "DNI":            e.numeroDi ?? "—",
      "Cargo":          e.cargo ?? "—",
      "Departamento":           e.departamento ?? "—",
      "Sueldo Base":    e.sueldo != null ? Number(e.sueldo).toFixed(2) : "—",
      "Estado":         e.estado ?? "—",
      "Tipo Contrato":  e.tipoContrato ?? "—",
      "Fecha Ingreso":  formatFecha(e.fechaInicio),
      "Correo":         e.correo ?? "—",
      "Teléfono":       e.telefono ?? "—",
    }));
    const ws = XLSX.utils.json_to_sheet(data);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Empleados");
    XLSX.writeFile(wb, `empleados_${new Date().toISOString().split("T")[0]}.xlsx`);
  };

  return (
    <div className="dashboard">
      <Sidebar rol="RRHH" />

      <main className="main-content">
        <header className="main-header header-rrhh">
          <div className="header-left">
            <span className="page-breadcrumb">Dashboard / Gestión de Empleados</span>
            <h1>Gestión de Empleados</h1>
            <p style={{ margin: 0, fontSize: "13px", color: "var(--text-3)" }}>
              Administre la información de los empleados de la clínica.
            </p>
          </div>
          <div style={{ display: "flex", gap: "10px", position: "relative", top: "10px" }}>
            <button className="btn btn-success" onClick={() => setModalNuevo(true)}>
              ＋ Nuevo Empleado
            </button>
            <button className="btn btn-primary" onClick={exportarEmpleados}>⬇ Exportar</button>
          </div>
        </header>

        {mensaje && <div className="alert alert-success">✅ {mensaje}</div>}
        {error   && <div className="alert alert-warning">⚠️ {error}</div>}

        {/* ── FILTER ROW ── */}
        <div className="filter-row">
          <div className="filter-group">
            <label className="filter-label">Tipo Documento</label>
            <select className="filter-select" value={filterDocType} onChange={e => setFilterDocType(e.target.value)}>
              <option value="">Todos</option>
              <option value="DNI">DNI</option>
              <option value="CE">Carnet Ext.</option>
            </select>
          </div>
          <div className="filter-group">
            <label className="filter-label">Departamento</label>
            <select className="filter-select" value={filterArea} onChange={e => { setFilterArea(e.target.value); setFilterCargo(""); }}>
              <option value="">Todos</option>
              {departamentos.map(d => (
                <option key={d.idDpto} value={d.nombre}>{d.nombre}</option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <label className="filter-label">Cargo</label>
            <select className="filter-select" value={filterCargo} onChange={e => setFilterCargo(e.target.value)}>
              <option value="">Todos</option>
              {(filterArea && DEPT_CARGOS[filterArea]
                ? DEPT_CARGOS[filterArea]
                : [...new Set(empleados.map(e => e.cargo).filter(Boolean))].sort()
              ).map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
          <div className="filter-group">
            <label className="filter-label">Estado</label>
            <select className="filter-select" value={filterEstado} onChange={e => setFilterEstado(e.target.value)}>
              <option value="">Todos</option>
              <option value="ACTIVO">Activo</option>
              <option value="INACTIVO">Inactivo</option>
            </select>
          </div>
          <div className="filter-group filter-search">
            <label className="filter-label">Buscar por nombre</label>
            <div className="filter-search-wrap">
              <input className="filter-input" placeholder="Ingrese nombre" value={searchName}
                onChange={e => setSearchName(e.target.value)}
                onKeyDown={e => e.key === "Enter" && handleBuscar()} />
              <span className="filter-search-icon">🔍</span>
            </div>
          </div>
          <div style={{ display: "flex", alignItems: "flex-end", gap: "8px", position: "relative", top: "-5px" }}>
            <button className="btn btn-primary btn-sm" onClick={handleBuscar}>Buscar</button>
            <button className="btn btn-secondary btn-sm" onClick={handleLimpiar}>☰ Limpiar filtros</button>
          </div>
        </div>

        {/* ── TABLE ── */}
        <div className="section-card" style={{ padding: 0, overflow: "hidden" }}>
          <div className="table-wrapper" style={{ marginBottom: 0 }} ref={dropRef}>
            {loading ? (
              <div className="loading-text">Cargando empleados...</div>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Nombre Completo</th>
                    <th>Tipo Doc / DNI</th>
                    <th>Departamento</th>
                    <th>Cargo</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {empleados.length === 0 ? (
                    <tr><td colSpan={6}>
                      <div className="empty-state">
                        <div className="empty-state-icon">🔍</div>
                        <p>No se encontraron empleados</p>
                      </div>
                    </td></tr>
                  ) : empleados.map(emp => (
                    <tr key={emp.idEmpleado}>
                      <td>{emp.nombres} {emp.apellidos}</td>
                      <td style={{ fontSize: "13px", color: "var(--text-2)" }}>
                        {emp.docIdentidad ?? "DNI"} · {emp.numeroDi}
                      </td>
                      <td>{emp.departamento ?? "—"}</td>
                      <td>{emp.cargo ?? "—"}</td>
                      <td>
                        <span className={`emp-status-dot ${emp.estado === "ACTIVO" ? "emp-status-active" : "emp-status-inactive"}`}>
                          {emp.estado === "ACTIVO" ? "Activo" : "Inactivo"}
                        </span>
                      </td>
                      <td>
                        <div style={{ display: "flex", gap: "6px", alignItems: "center" }}>
                          <button className="btn btn-secondary btn-sm"
                            onClick={() => setModalEditar({ ...emp })}>
                            ✏️ Editar
                          </button>
                          <div className="action-drop-wrap">
                            <button className="action-drop-btn"
                              onClick={() => openDropMenu(emp.idEmpleado)}>
                              ⋯
                            </button>
                            {openDrop === emp.idEmpleado && (
                              <div className="action-drop-menu">
                                {emp.estado === "ACTIVO" ? (
                                  <div className="action-drop-item drop-red"
                                    onClick={() => { setModalDesactivar({ open: true, id: emp.idEmpleado, motivo: "" }); setOpenDrop(null); }}>
                                    🚫 Desactivar empleado
                                  </div>
                                ) : (
                                  <div className="action-drop-item drop-green"
                                    onClick={() => { setModalReactivar({ open: true, id: emp.idEmpleado }); setOpenDrop(null); }}>
                                    ✅ Reactivar empleado
                                  </div>
                                )}
                                <div className="action-drop-item drop-yellow"
                                  onClick={() => { setModalSancion({ open: true, id: emp.idEmpleado }); setSancionForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  ⚠️ Registrar sanción
                                </div>
                                <div className="action-drop-item drop-green"
                                  onClick={() => { setModalAscenso({ open: true, id: emp.idEmpleado }); setAscensoForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  ↑ Registrar ascenso
                                </div>
                                <div className="action-drop-item drop-blue"
                                  onClick={() => { setModalCambio({ open: true, id: emp.idEmpleado }); setCambioForm(f => ({ ...f, idEmpleado: emp.idEmpleado })); setOpenDrop(null); }}>
                                  💲 Cambio salarial
                                </div>
                                <div className="action-drop-item drop-gray"
                                  onClick={() => setOpenDrop(null)}>
                                  🕐 Ver historial
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          {/* ── PAGINATION ── */}
          <div className="pagination-bar">
            <div className="pagination-left">
              Mostrar
              <select className="pagination-size-select" value={pageSize}
                onChange={e => { setPageSize(Number(e.target.value)); setPage(0); }}>
                {PAGE_SIZES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
              registros
            </div>
            <div className="pagination-btns">
              <button className="pagination-btn" onClick={() => setPage(0)} disabled={page === 0}>«</button>
              <button className="pagination-btn" onClick={() => setPage(p => p - 1)} disabled={page === 0}>‹</button>
              {Array.from({ length: Math.min(3, totalPages) }, (_, i) => (
                <button key={i} className={`pagination-btn ${page === i ? "active" : ""}`} onClick={() => setPage(i)}>
                  {i + 1}
                </button>
              ))}
              {totalPages > 4 && <span style={{ padding: "0 4px", color: "var(--text-3)" }}>...</span>}
              {totalPages > 3 && (
                <button className={`pagination-btn ${page === totalPages - 1 ? "active" : ""}`} onClick={() => setPage(totalPages - 1)}>
                  {totalPages}
                </button>
              )}
              <button className="pagination-btn" onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>›</button>
              <button className="pagination-btn" onClick={() => setPage(totalPages - 1)} disabled={page >= totalPages - 1}>»</button>
            </div>
            <div className="pagination-info">
              {page * pageSize + 1}–{Math.min((page + 1) * pageSize, totalElements)} de {totalElements} empleados
            </div>
          </div>
        </div>

      </main>

      {/* ══ MODAL: NUEVO EMPLEADO ══ */}
      {modalNuevo && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalNuevo(false)}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div><h3>➕ Registrar Nuevo Empleado</h3></div>
              <button className="modal-close" onClick={() => setModalNuevo(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info">
                Complete todos los campos obligatorios (*). El sistema verificará la duplicidad del DNI.
              </div>
              <form id="form-nuevo" onSubmit={handleRegistrar}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Tipo Documento *</label>
                    <select value={form.docIdentidad} onChange={e => setForm({ ...form, docIdentidad: e.target.value })}>
                      <option value="DNI">DNI</option>
                      <option value="CE">Carnet de Extranjería</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Número Documento *</label>
                    <input value={form.numeroDi} onChange={e => setForm({ ...form, numeroDi: e.target.value })} placeholder="Ej: 12345678" required />
                  </div>
                  <div className="form-group">
                    <label>Nombres *</label>
                    <input value={form.nombres} onChange={e => setForm({ ...form, nombres: e.target.value })} placeholder="Nombres completos" required />
                  </div>
                  <div className="form-group">
                    <label>Apellidos *</label>
                    <input value={form.apellidos} onChange={e => setForm({ ...form, apellidos: e.target.value })} placeholder="Apellidos completos" required />
                  </div>
                  <div className="form-group">
                    <label>Fecha de Nacimiento</label>
                    <input type="date" value={form.fechaNac} onChange={e => setForm({ ...form, fechaNac: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Sexo</label>
                    <select value={form.sexo} onChange={e => setForm({ ...form, sexo: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="M">Masculino</option>
                      <option value="F">Femenino</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" value={form.correo} onChange={e => setForm({ ...form, correo: e.target.value })} placeholder="correo@ejemplo.com" />
                  </div>
                  <div className="form-group">
                    <label>Teléfono</label>
                    <input value={form.telefono} onChange={e => setForm({ ...form, telefono: e.target.value })} placeholder="999 999 999" />
                  </div>
                  <div className="form-group">
                    <label>Cargo *</label>
                    <select value={form.cargo} onChange={e => setForm({ ...form, cargo: e.target.value })} required>
                      <option value="">Seleccione cargo</option>
                      {(form.idDpto
                        ? DEPT_CARGOS[departamentos.find(d => d.idDpto === parseInt(form.idDpto))?.nombre] ?? []
                        : []
                      ).map(c => <option key={c} value={c}>{c}</option>)}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select value={form.tipoContrato} onChange={e => setForm({ ...form, tipoContrato: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="PLAZO_FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="SERVICIOS">Por servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Fecha Inicio Contrato</label>
                    <input type="date" value={form.fechaInicio} onChange={e => setForm({ ...form, fechaInicio: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Sueldo Base (S/) *</label>
                    <input type="number" step="0.01" min="0" value={form.sueldo} onChange={e => setForm({ ...form, sueldo: e.target.value })} placeholder="0.00" required />
                  </div>
                  <div className="form-group">
                    <label>Departamento *</label>
                    <select value={form.idDpto} onChange={e => setForm({ ...form, idDpto: e.target.value, cargo: "" })} required>
                      <option value="">Seleccione departamento</option>
                      {departamentos.map(d => (
                        <option key={d.idDpto} value={d.idDpto}>{d.nombre}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Contraseña Inicial</label>
                    <input type="password" value={form.contrasenia} onChange={e => setForm({ ...form, contrasenia: e.target.value })} placeholder="Contraseña de acceso" />
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalNuevo(false)}>Cancelar</button>
              <button className="btn btn-success" type="submit" form="form-nuevo">✅ Registrar Empleado</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: EDITAR ══ */}
      {modalEditar && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalEditar(null)}>
          <div className="modal-box modal-box-wide">
            <div className="modal-header">
              <div><h3>✏️ Editar Empleado</h3><p>{modalEditar.nombres} {modalEditar.apellidos} — {modalEditar.numeroDi}</p></div>
              <button className="modal-close" onClick={() => setModalEditar(null)}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-info">Los campos DNI, nombres y apellidos no son editables.</div>
              <form id="form-editar" onSubmit={handleGuardarEdicion}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Cargo</label>
                    <input value={modalEditar.cargo ?? ""} onChange={e => setModalEditar({ ...modalEditar, cargo: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Tipo de Contrato</label>
                    <select value={modalEditar.tipoContrato ?? ""} onChange={e => setModalEditar({ ...modalEditar, tipoContrato: e.target.value })}>
                      <option value="">Seleccione</option>
                      <option value="PLAZO_FIJO">Plazo fijo</option>
                      <option value="INDEFINIDO">Indefinido</option>
                      <option value="SERVICIOS">Por servicio</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Sueldo Base (S/)</label>
                    <input type="number" step="0.01" min="0" value={modalEditar.sueldo ?? ""} onChange={e => setModalEditar({ ...modalEditar, sueldo: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>ID Departamento</label>
                    <input type="number" value={modalEditar.idDpto ?? ""} onChange={e => setModalEditar({ ...modalEditar, idDpto: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Correo Electrónico</label>
                    <input type="email" value={modalEditar.correo ?? ""} onChange={e => setModalEditar({ ...modalEditar, correo: e.target.value })} />
                  </div>
                  <div className="form-group">
                    <label>Teléfono</label>
                    <input value={modalEditar.telefono ?? ""} onChange={e => setModalEditar({ ...modalEditar, telefono: e.target.value })} />
                  </div>
                  <div className="form-group form-full">
                    <label>Dirección</label>
                    <input value={modalEditar.direccion ?? ""} onChange={e => setModalEditar({ ...modalEditar, direccion: e.target.value })} />
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

      {/* ══ MODAL: DESACTIVAR ══ */}
      {modalDesactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>🚫 Desactivar Empleado</h3><p>El acceso al sistema quedará bloqueado.</p></div>
              <button className="modal-close" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">⚠️ El historial se conservará. El empleado no podrá acceder al sistema.</div>
              <div className="form-group">
                <label>Motivo de desactivación *</label>
                <textarea rows={3} value={modalDesactivar.motivo}
                  onChange={e => setModalDesactivar({ ...modalDesactivar, motivo: e.target.value })}
                  placeholder="Describa el motivo..." autoFocus />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalDesactivar({ open: false, id: null, motivo: "" })}>Cancelar</button>
              <button className="btn btn-danger" onClick={confirmarDesactivar} disabled={!modalDesactivar.motivo.trim()}>Confirmar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: REACTIVAR ══ */}
      {modalReactivar.open && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>✅ Reactivar Empleado</h3></div>
              <button className="modal-close" onClick={() => setModalReactivar({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <p style={{ fontSize: "14px", color: "#475569" }}>¿Confirma la reactivación? El empleado recuperará acceso al sistema.</p>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalReactivar({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-success" onClick={confirmarReactivar}>Confirmar</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: SANCIÓN ══ */}
      {modalSancion.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalSancion({ open: false, id: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>⚠️ Registrar Sanción</h3><p>Empleado #{modalSancion.id}</p></div>
              <button className="modal-close" onClick={() => setModalSancion({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <div className="alert alert-warning">⚠️ La justificación es obligatoria.</div>
              <form id="form-sancion" onSubmit={handleSancion}>
                <div className="form-grid">
                  <div className="form-group">
                    <label>Tipo de Sanción *</label>
                    <select value={sancionForm.tipoSancion}
                      onChange={e => setSancionForm({ ...sancionForm, tipoSancion: e.target.value })} required>
                      <option value="">Seleccione el tipo</option>
                      <option value="VERBAL">Amonestación verbal</option>
                      <option value="ESCRITA">Amonestación escrita</option>
                      <option value="SUSPENSION">Suspensión (días)</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Días de Suspensión</label>
                    <input type="number" min="0" value={sancionForm.diasSuspension}
                      onChange={e => setSancionForm({ ...sancionForm, diasSuspension: e.target.value })} />
                  </div>
                  <div className="form-group form-full">
                    <label>Justificación *</label>
                    <textarea value={sancionForm.justificacion}
                      onChange={e => setSancionForm({ ...sancionForm, justificacion: e.target.value })}
                      placeholder="Describa detalladamente el motivo..." required />
                  </div>
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalSancion({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-danger" type="submit" form="form-sancion">🚫 Registrar Sanción</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: ASCENSO ══ */}
      {modalAscenso.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalAscenso({ open: false, id: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>↑ Registrar Ascenso</h3><p>Empleado #{modalAscenso.id}</p></div>
              <button className="modal-close" onClick={() => setModalAscenso({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <form id="form-ascenso" onSubmit={handleAscenso}>
                <div className="form-group">
                  <label>Nuevo Cargo</label>
                  <input value={ascensoForm.nuevoCargo}
                    onChange={e => setAscensoForm({ ...ascensoForm, nuevoCargo: e.target.value })}
                    placeholder="Ej: Jefe de Área, Especialista..." />
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalAscenso({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-primary" type="submit" form="form-ascenso">↑ Registrar Ascenso</button>
            </div>
          </div>
        </div>
      )}

      {/* ══ MODAL: CAMBIO SALARIAL ══ */}
      {modalCambio.open && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModalCambio({ open: false, id: null })}>
          <div className="modal-box">
            <div className="modal-header">
              <div><h3>💲 Cambio Salarial</h3><p>Empleado #{modalCambio.id}</p></div>
              <button className="modal-close" onClick={() => setModalCambio({ open: false, id: null })}>×</button>
            </div>
            <div className="modal-body">
              <form id="form-cambio" onSubmit={handleCambioSalarial}>
                <div className="form-group">
                  <label>Nuevo Sueldo (S/) *</label>
                  <input type="number" step="0.01" min="0" value={cambioForm.nuevoSueldo}
                    onChange={e => setCambioForm({ ...cambioForm, nuevoSueldo: e.target.value })}
                    placeholder="0.00" required />
                </div>
              </form>
            </div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setModalCambio({ open: false, id: null })}>Cancelar</button>
              <button className="btn btn-primary" type="submit" form="form-cambio">💾 Guardar Cambio</button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
