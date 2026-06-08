import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import "../styles/Dashboard.css";
import {
  listarEmpleados,
  buscarEmpleados,
  buscarEmpleadosAvanzado,
  registrarEmpleado,
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
  const [empleados, setEmpleados] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [mensaje, setMensaje] = useState("");

  const [filtroTexto, setFiltroTexto] = useState("");
  const [filtroEstado, setFiltroEstado] = useState("");
  const [filtroCargo, setFiltroCargo] = useState("");

  const [form, setForm] = useState(FORM_VACIO);
  const [sancionForm, setSancionForm] = useState({ idEmpleado: "", tipoSancion: "", diasSuspension: 0, justificacion: "" });
  const [ascensoForm, setAscensoForm] = useState({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });

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
    } catch {
      setMensaje("Error al registrar empleado");
    }
  };

  const handleDesactivar = async (id) => {
    const motivo = prompt("Motivo de desactivación:");
    if (!motivo) return;
    try {
      await desactivarEmpleado(id, motivo);
      setMensaje("Empleado desactivado");
      cargarEmpleados();
    } catch {
      setMensaje("Error al desactivar empleado");
    }
  };

  const handleReactivar = async (id) => {
    try {
      await reactivarEmpleado(id, "Reactivación manual");
      setMensaje("Empleado reactivado");
      cargarEmpleados();
    } catch {
      setMensaje("Error al reactivar empleado");
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
      setMensaje("Error al registrar sanción");
    }
  };

  const handleAscenso = async (e) => {
    e.preventDefault();
    setMensaje("");
    try {
      await registrarAscenso(ascensoForm.idEmpleado, { nuevoCargo: ascensoForm.nuevoCargo });
      await cambioSalarial(ascensoForm.idEmpleado, { nuevoSueldo: parseFloat(ascensoForm.nuevoSueldo) });
      setMensaje("Ascenso y cambio salarial registrados");
      setAscensoForm({ idEmpleado: "", nuevoCargo: "", nuevoSueldo: "" });
      cargarEmpleados();
    } catch {
      setMensaje("Error al registrar ascenso");
    }
  };

  return (
    <div className="dashboard">
      <aside className="sidebar">
        <div className="sidebar-header"><h2>Panel RRHH</h2><p>Recursos Humanos</p></div>
        <nav className="sidebar-nav">
          <Link to="/rrhh">🏠 Inicio</Link>
          <Link to="/rrhh/empleados" className="active">👥 Gestión de Empleados</Link>
          <Link to="/rrhh/asistencia">📋 Control de Asistencia</Link>
          <Link to="/rrhh/nomina">💰 Cálculo de Nómina</Link>
          <Link to="/rrhh/solicitudes">📅 Solicitudes</Link>
          <Link to="/rrhh/reportes">📊 Reportes</Link>
          <div className="nav-logout"><Link to="/">🚪 Cerrar Sesión</Link></div>
        </nav>
      </aside>

      <main className="main-content">
        <header className="main-header">
          <h1>Gestión de Empleados</h1>
          <span>ECU-02</span>
        </header>

        {mensaje && <div className="alert alert-success">{mensaje}</div>}
        {error   && <div className="alert alert-warning">{error}</div>}

        {/* ── BÚSQUEDA ── */}
        <div className="section-card">
          <h2>Buscar Empleado</h2>
          <div className="toolbar">
            <input
              type="text" placeholder="DNI o nombre"
              value={filtroTexto} onChange={(e) => setFiltroTexto(e.target.value)}
            />
            <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
              <option value="">Estado</option>
              <option value="ACTIVO">Activo</option>
              <option value="INACTIVO">Inactivo</option>
            </select>
            <input
              type="text" placeholder="Cargo"
              value={filtroCargo} onChange={(e) => setFiltroCargo(e.target.value)}
            />
            <button className="btn btn-primary" onClick={handleBuscar}>Buscar</button>
            <button className="btn btn-secondary" onClick={cargarEmpleados}>Ver todos</button>
          </div>

          <div className="table-wrapper">
            {loading ? (
              <p style={{ padding: "16px", color: "#888" }}>Cargando...</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>DNI</th><th>Apellidos y Nombres</th><th>Departamento</th>
                    <th>Cargo</th><th>Estado</th><th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {empleados.length === 0 ? (
                    <tr><td colSpan={6} style={{ textAlign: "center", color: "#888" }}>No se encontraron resultados</td></tr>
                  ) : empleados.map((e) => (
                    <tr key={e.idEmpleado}>
                      <td>{e.numeroDi}</td>
                      <td>{e.apellidos}, {e.nombres}</td>
                      <td>{e.departamento}</td>
                      <td>{e.cargo}</td>
                      <td>
                        <span className={`badge ${e.estado === "ACTIVO" ? "badge-success" : "badge-danger"}`}>
                          {e.estado}
                        </span>
                      </td>
                      <td>
                        {e.estado === "ACTIVO" ? (
                          <button className="btn btn-danger btn-sm" onClick={() => handleDesactivar(e.idEmpleado)}>
                            Desactivar
                          </button>
                        ) : (
                          <button className="btn btn-success btn-sm" onClick={() => handleReactivar(e.idEmpleado)}>
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

        {/* ── REGISTRAR EMPLEADO ── */}
        <div className="section-card">
          <h2>Registrar Nuevo Empleado</h2>
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
                <input value={form.nombres} onChange={(e) => setForm({ ...form, nombres: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Apellidos *</label>
                <input value={form.apellidos} onChange={(e) => setForm({ ...form, apellidos: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Fecha Nacimiento</label>
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
                <label>Correo</label>
                <input type="email" value={form.correo} onChange={(e) => setForm({ ...form, correo: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Teléfono</label>
                <input value={form.telefono} onChange={(e) => setForm({ ...form, telefono: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Cargo *</label>
                <input value={form.cargo} onChange={(e) => setForm({ ...form, cargo: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Tipo Contrato</label>
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
                <label>Sueldo *</label>
                <input type="number" step="0.01" value={form.sueldo} onChange={(e) => setForm({ ...form, sueldo: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>ID Departamento *</label>
                <input type="number" value={form.idDpto} onChange={(e) => setForm({ ...form, idDpto: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Contraseña inicial</label>
                <input type="password" value={form.contrasenia} onChange={(e) => setForm({ ...form, contrasenia: e.target.value })} />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-success">Registrar Empleado</button>
              <button type="button" className="btn btn-secondary" onClick={() => setForm(FORM_VACIO)}>Limpiar</button>
            </div>
          </form>
        </div>

        {/* ── SANCIÓN ── */}
        <div className="section-card">
          <h2>Registrar Sanción</h2>
          <form onSubmit={handleSancion}>
            <div className="form-grid">
              <div className="form-group">
                <label>ID Empleado</label>
                <input type="number" value={sancionForm.idEmpleado}
                  onChange={(e) => setSancionForm({ ...sancionForm, idEmpleado: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Tipo de Sanción</label>
                <select value={sancionForm.tipoSancion}
                  onChange={(e) => setSancionForm({ ...sancionForm, tipoSancion: e.target.value })}>
                  <option value="">Seleccione</option>
                  <option value="VERBAL">Amonestación verbal</option>
                  <option value="ESCRITA">Amonestación escrita</option>
                  <option value="SUSPENSION">Suspensión</option>
                </select>
              </div>
              <div className="form-group">
                <label>Días de Suspensión</label>
                <input type="number" min="0" value={sancionForm.diasSuspension}
                  onChange={(e) => setSancionForm({ ...sancionForm, diasSuspension: e.target.value })} />
              </div>
              <div className="form-group form-full">
                <label>Justificación *</label>
                <textarea value={sancionForm.justificacion}
                  onChange={(e) => setSancionForm({ ...sancionForm, justificacion: e.target.value })} required />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-danger">Registrar Sanción</button>
            </div>
          </form>
        </div>

        {/* ── ASCENSO / CAMBIO SALARIAL ── */}
        <div className="section-card">
          <h2>Registrar Ascenso / Cambio Salarial</h2>
          <form onSubmit={handleAscenso}>
            <div className="form-grid">
              <div className="form-group">
                <label>ID Empleado</label>
                <input type="number" value={ascensoForm.idEmpleado}
                  onChange={(e) => setAscensoForm({ ...ascensoForm, idEmpleado: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Nuevo Cargo</label>
                <input value={ascensoForm.nuevoCargo}
                  onChange={(e) => setAscensoForm({ ...ascensoForm, nuevoCargo: e.target.value })} />
              </div>
              <div className="form-group">
                <label>Nuevo Sueldo</label>
                <input type="number" step="0.01" value={ascensoForm.nuevoSueldo}
                  onChange={(e) => setAscensoForm({ ...ascensoForm, nuevoSueldo: e.target.value })} />
              </div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">Guardar Cambio</button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
