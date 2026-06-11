const BASE = 'http://localhost:8080';

export const getToken  = () => localStorage.getItem('token');
export const getUser   = () => JSON.parse(localStorage.getItem('user') || '{}');
export const saveSession = (token, rol, username) =>
  localStorage.setItem('user', JSON.stringify({ token, rol, username }));
export const clearSession = () => localStorage.removeItem('user');

const parseJwt = (token) => {
  try { return JSON.parse(atob(token.split('.')[1])); } catch { return {}; }
};

export const getCurrentUserId = () => {
  const user = getUser();
  return parseJwt(user.token).idUsuario;
};

const json = (r) => {
  if (!r.ok && r.status !== 400) throw new Error(`HTTP ${r.status}`);
  return r.json();
};

const h = () => ({
  'Content-Type': 'application/json',
  Authorization: `Bearer ${getUser().token}`,
});

const fetchAuth = (url, options = {}) =>
  fetch(url, { ...options, headers: { ...h(), ...options.headers } }).then((r) => {
    const newToken = r.headers.get('X-New-Token');
    if (newToken) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      user.token = newToken;
      localStorage.setItem('user', JSON.stringify(user));
    }
    if (r.status === 401) {
      clearSession();
      window.location.href = '/';
      throw new Error('Sesión expirada');
    }
    return r;
  });

// ── AUTH ──────────────────────────────────────────────────────────────────────
export const login = (nombreUsuario, password) =>
  fetch(`${BASE}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nombreUsuario, password }),
  }).then(json);

export const verify2FA = (username, code) =>
  fetch(`${BASE}/api/v1/auth/verify-2fa`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, code }),
  }).then(json);

export const solicitarRecuperacion = (email) =>
  fetch(`${BASE}/api/v1/auth/recovery`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email }),
  }).then(json);

// ── EMPLEADOS ─────────────────────────────────────────────────────────────────
export const listarEmpleados = (page = 0, size = 20) =>
  fetchAuth(`${BASE}/api/v1/empleados/listar?page=${page}&size=${size}`).then(json);

export const buscarEmpleados = (filtro, page = 0) =>
  fetchAuth(`${BASE}/api/v1/empleados/buscar?filtro=${encodeURIComponent(filtro)}&page=${page}`).then(json);

export const buscarEmpleadosAvanzado = ({ estado, idDpto, cargo } = {}, page = 0) => {
  const p = new URLSearchParams({ page });
  if (estado) p.append('estado', estado);
  if (idDpto) p.append('idDpto', idDpto);
  if (cargo)  p.append('cargo', cargo);
  return fetchAuth(`${BASE}/api/v1/empleados/buscar-avanzado?${p}`).then(json);
};

export const registrarEmpleado = (dto) =>
  fetchAuth(`${BASE}/api/v1/empleados/registrar`, {
    method: 'POST', body: JSON.stringify(dto),
  }).then(json);

export const actualizarEmpleado = (id, dto) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}`, {
    method: 'PUT', body: JSON.stringify(dto),
  }).then(json);

export const desactivarEmpleado = (id, motivo) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}/desactivar`, {
    method: 'PUT', body: JSON.stringify({ motivo }),
  }).then(json);

export const reactivarEmpleado = (id, motivo) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}/reactivar`, {
    method: 'PUT', body: JSON.stringify({ motivo }),
  }).then(json);

export const registrarSancion = (id, dto) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}/sancion`, {
    method: 'PUT', body: JSON.stringify(dto),
  }).then(json);

export const registrarAscenso = (id, dto) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}/ascenso`, {
    method: 'PUT', body: JSON.stringify(dto),
  }).then(json);

export const cambioSalarial = (id, dto) =>
  fetchAuth(`${BASE}/api/v1/empleados/${id}/cambio-salarial`, {
    method: 'PUT', body: JSON.stringify(dto),
  }).then(json);

// ── ASISTENCIA ────────────────────────────────────────────────────────────────
export const registrarManual = (dto) =>
  fetchAuth(`${BASE}/api/v1/asistencia/manual`, {
    method: 'POST', body: JSON.stringify(dto),
  }).then(json);

export const listarJustificacionesPendientes = () =>
  fetchAuth(`${BASE}/api/v1/asistencia/justificaciones/pendientes`).then(json);

export const listarJustificacionesEmpleado = (idEmpleado) =>
  fetchAuth(`${BASE}/api/v1/asistencia/justificaciones/empleado/${idEmpleado}`).then(json);

export const revisarJustificacion = (id, decision, comentario = '') =>
  fetchAuth(`${BASE}/api/v1/asistencia/justificar/${id}/revisar`, {
    method: 'PUT', body: JSON.stringify({ decision, comentario }),
  }).then(json);

export const justificarTardanza = (dto) =>
  fetchAuth(`${BASE}/api/v1/asistencia/justificar`, {
    method: 'POST', body: JSON.stringify(dto),
  }).then(json);

// ── HORARIOS ──────────────────────────────────────────────────────────────────
export const listarHorarios = () =>
  fetchAuth(`${BASE}/api/v1/horarios`).then(json);

export const crearHorario = (dto) =>
  fetchAuth(`${BASE}/api/v1/horarios`, {
    method: 'POST', body: JSON.stringify(dto),
  }).then(json);

export const actualizarHorario = (id, dto) =>
  fetchAuth(`${BASE}/api/v1/horarios/${id}`, {
    method: 'PUT', body: JSON.stringify(dto),
  }).then(json);

export const eliminarHorario = (id) =>
  fetchAuth(`${BASE}/api/v1/horarios/${id}`, {
    method: 'DELETE',
  }).then(json);

// ── NÓMINA ────────────────────────────────────────────────────────────────────
export const calcularNomina = async (dto) => {
  const r = await fetchAuth(`${BASE}/api/nomina/calcular`, {
    method: 'POST', body: JSON.stringify(dto),
  });
  const data = await r.json();
  if (data && data.message) throw new Error(data.message);
  return data;
};

export const getNominasPorPeriodo = (periodo) =>
  fetchAuth(`${BASE}/api/nomina/periodo/${periodo}`).then(json);

export const getNominasPorEmpleado = (idEmpleado) =>
  fetchAuth(`${BASE}/api/nomina/empleado/${idEmpleado}`).then(json);

export const enviarComprobante = (id) =>
  fetchAuth(`${BASE}/api/nomina/${id}/enviar`, {
    method: 'POST',
  }).then(json);

export const descargarComprobante = (id) =>
  fetchAuth(`${BASE}/api/nomina/${id}/comprobante`);

// ── SOLICITUDES ───────────────────────────────────────────────────────────────
export const registrarSolicitud = (dto) =>
  fetchAuth(`${BASE}/api/solicitud/registrar`, {
    method: 'POST', body: JSON.stringify(dto),
  }).then(json);

export const revisarSolicitudRRHH = (idSolicitud, aprobado, observacionRrhh = '') =>
  fetchAuth(`${BASE}/api/solicitud/revisar`, {
    method: 'PUT',
    body: JSON.stringify({ idSolicitud, aprobado, observacionRrhh }),
  }).then(json);

export const decidirSolicitudGerencia = (idSolicitud, aprobado, respuesta = '') =>
  fetchAuth(`${BASE}/api/solicitud/decidir`, {
    method: 'PUT',
    body: JSON.stringify({ idSolicitud, aprobado, respuesta }),
  }).then(json);

export const getSolicitudesPorEstado = (estado) =>
  fetchAuth(`${BASE}/api/solicitud/estado/${estado}`).then(json);

export const getSolicitudesPorEmpleado = (idEmpleado) =>
  fetchAuth(`${BASE}/api/solicitud/empleado/${idEmpleado}`).then(json);

// ── REPORTES ──────────────────────────────────────────────────────────────────
export const generarReporte = (tipo, filtros, formato) => {
  const params = formato ? `?formato=${formato}` : '';
  return fetchAuth(`${BASE}/api/reporte/${tipo}${params}`, {
    method: 'POST', body: JSON.stringify(filtros),
  });
};
