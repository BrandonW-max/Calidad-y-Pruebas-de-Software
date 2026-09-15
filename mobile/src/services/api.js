// LOCAL: usa tu IP (ipconfig → IPv4), ej: 'http://192.168.1.100:8080'
// AZURE: usa la URL pública del backend, ej: 'https://mi-backend.azurewebsites.net'
const BASE_URL = process.env.EXPO_PUBLIC_API_URL ?? 'http://192.168.1.100:8080';

const leerRespuesta = async (res) => {
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
};

const mensajeError = (data, fallback) => {
  if (!data) return fallback;
  if (typeof data === 'string') return data;
  return data.mensaje || data.message || fallback;
};

export const getEstadoAsistenciaHoy = async (idEmpleado, deviceToken) => {
  const res = await fetch(
    `${BASE_URL}/api/v1/asistencia/biometrico/estado/${idEmpleado}`,
    { headers: { 'X-Device-Token': deviceToken } }
  );
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
};

export const buscarEmpleadoPorDni = async (numeroDi) => {
  const res = await fetch(`${BASE_URL}/api/v1/dispositivos/dni/${numeroDi}`);
  if (res.status === 404) throw new Error('DNI no encontrado. Verifica tu número de documento.');
  if (res.status === 400) throw new Error('Empleado inactivo o desvinculado.');
  if (!res.ok) throw new Error(`Error ${res.status}`);
  return res.json();
};

export const enviarOtp = async (numeroDi) => {
  const res = await fetch(`${BASE_URL}/api/v1/dispositivos/enviar-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ numeroDi }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, `HTTP ${res.status}`));
  return data; // { sessionId }
};

export const registrarDispositivo = async (numeroDi, deviceToken, sessionId, codigo) => {
  const res = await fetch(`${BASE_URL}/api/v1/dispositivos/registrar`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ numeroDi, deviceToken, sessionId, codigo }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, `HTTP ${res.status}`));
  return data;
};

export const registrarAsistenciaBiometrica = async (idEmpleado, deviceToken, tipo) => {
  const res = await fetch(`${BASE_URL}/api/v1/asistencia/biometrico`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idEmpleado, deviceToken, tipo }),
  });
  const data = await leerRespuesta(res);
  if (!res.ok) throw new Error(mensajeError(data, `HTTP ${res.status}`));
  return data;
};
