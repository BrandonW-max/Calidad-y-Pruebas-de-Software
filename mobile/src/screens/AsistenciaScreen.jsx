import { useState, useEffect, useCallback } from 'react';
import {
  View, Text, TouchableOpacity, StyleSheet,
  ActivityIndicator, Alert,
} from 'react-native';
import * as LocalAuthentication from 'expo-local-authentication';
import * as SecureStore from 'expo-secure-store';
import { registrarAsistenciaBiometrica, getEstadoAsistenciaHoy } from '../services/api';

export default function AsistenciaScreen() {
  const [nombre,      setNombre]      = useState('');
  const [hora,        setHora]        = useState('');
  const [cargando,    setCargando]    = useState(false);
  const [cargandoEstado, setCargandoEstado] = useState(true);
  const [resultado,   setResultado]   = useState(null);
  const [tieneHuella, setTieneHuella] = useState(false);

  // Estado del día: null = sin datos aún
  const [estadoHoy, setEstadoHoy] = useState({
    tieneEntrada: false,
    tieneSalida:  false,
    horaEntrada:  null,
    horaSalida:   null,
    estado:       null,
  });

  const cargarEstado = useCallback(async () => {
    setCargandoEstado(true);
    try {
      const idEmpleado  = await SecureStore.getItemAsync('idEmpleado');
      const deviceToken = await SecureStore.getItemAsync('deviceToken');
      if (!idEmpleado || !deviceToken) return;
      const data = await getEstadoAsistenciaHoy(idEmpleado, deviceToken);
      setEstadoHoy(data);
    } catch {
      // sin registro hoy → estado por defecto
    } finally {
      setCargandoEstado(false);
    }
  }, []);

  useEffect(() => {
    const tick = () => setHora(new Date().toLocaleTimeString('es-PE', {
      hour: '2-digit', minute: '2-digit', second: '2-digit',
    }));
    tick();
    const t = setInterval(tick, 1000);
    SecureStore.getItemAsync('nombreEmpleado').then(n => setNombre(n || 'Empleado'));
    LocalAuthentication.hasHardwareAsync().then(setTieneHuella);
    cargarEstado();
    return () => clearInterval(t);
  }, [cargarEstado]);

  const marcar = async (tipo) => {
    // Validaciones de bloqueo
    if (tipo === 'ENTRADA' && estadoHoy.tieneEntrada) {
      Alert.alert('Ya registrado', 'Ya tienes tu entrada registrada hoy.');
      return;
    }
    if (tipo === 'SALIDA' && !estadoHoy.tieneEntrada) {
      Alert.alert('Sin entrada', 'Primero debes registrar tu entrada.');
      return;
    }
    if (tipo === 'SALIDA' && estadoHoy.tieneSalida) {
      Alert.alert('Ya registrado', 'Ya tienes tu salida registrada hoy.');
      return;
    }
    if (estadoHoy.tieneEntrada && estadoHoy.tieneSalida) {
      Alert.alert('Día completado', 'Tu asistencia de hoy ya está completa.');
      return;
    }
    if (!tieneHuella) {
      Alert.alert('Sin sensor', 'Este dispositivo no tiene sensor biométrico.');
      return;
    }

    setCargando(true);
    setResultado(null);
    try {
      const auth = await LocalAuthentication.authenticateAsync({
        promptMessage: `Verifica tu identidad para registrar ${tipo === 'ENTRADA' ? 'entrada' : 'salida'}`,
        cancelLabel: 'Cancelar',
        disableDeviceFallback: false,
      });
      if (!auth.success) { setCargando(false); return; }

      const deviceToken = await SecureStore.getItemAsync('deviceToken');
      const idEmpleado  = await SecureStore.getItemAsync('idEmpleado');

      await registrarAsistenciaBiometrica(Number(idEmpleado), deviceToken, tipo);

      const horaReg = new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' });
      setResultado({ tipo, hora: horaReg, exito: true });

      // Recargar estado para actualizar botones
      await cargarEstado();
    } catch (e) {
      setResultado({ tipo, hora: '', exito: false, error: e.message });
    } finally {
      setCargando(false);
    }
  };

  // Determina si cada botón está habilitado
  const entradaHabilitada = !estadoHoy.tieneEntrada && !estadoHoy.tieneSalida;
  const salidaHabilitada  = estadoHoy.tieneEntrada && !estadoHoy.tieneSalida;
  const diaCompleto       = estadoHoy.tieneEntrada && estadoHoy.tieneSalida;

  return (
    <View style={s.container}>
      {/* Encabezado */}
      <View style={s.header}>
        <Text style={s.headerLogo}>🏥</Text>
        <Text style={s.headerTitle}>Hospital San Gabriel</Text>
        <Text style={s.headerSub}>Sistema de Asistencia</Text>
      </View>

      {/* Info empleado */}
      <View style={s.card}>
        <Text style={s.cardNombre}>{nombre}</Text>
        <Text style={s.cardHora}>{hora}</Text>
        <Text style={s.cardFecha}>
          {new Date().toLocaleDateString('es-PE', { weekday: 'long', day: 'numeric', month: 'long' })}
        </Text>

        {/* Resumen del día */}
        {!cargandoEstado && (
          <View style={s.resumenRow}>
            <View style={s.resumenItem}>
              <Text style={s.resumenLabel}>Entrada</Text>
              <Text style={[s.resumenVal, estadoHoy.horaEntrada ? s.ok : s.pendiente]}>
                {estadoHoy.horaEntrada ?? '—:—'}
              </Text>
            </View>
            <View style={s.resumenItem}>
              <Text style={s.resumenLabel}>Salida</Text>
              <Text style={[s.resumenVal, estadoHoy.horaSalida ? s.ok : s.pendiente]}>
                {estadoHoy.horaSalida ?? '—:—'}
              </Text>
            </View>
          </View>
        )}
      </View>

      {/* Banner día completo */}
      {diaCompleto && (
        <View style={s.bannerCompleto}>
          <Text style={s.bannerIcon}>✅</Text>
          <Text style={s.bannerText}>Asistencia completada hoy</Text>
        </View>
      )}

      {/* Resultado de la última acción */}
      {resultado && (
        <View style={[s.resultado, resultado.exito ? s.resultadoOk : s.resultadoErr]}>
          <Text style={s.resultadoIcon}>{resultado.exito ? '✅' : '❌'}</Text>
          <Text style={s.resultadoText}>
            {resultado.exito
              ? `${resultado.tipo === 'ENTRADA' ? 'Entrada' : 'Salida'} registrada a las ${resultado.hora}`
              : resultado.error || 'Error al registrar'}
          </Text>
        </View>
      )}

      {/* Botones */}
      {cargando ? (
        <ActivityIndicator size="large" color="#2563eb" style={{ marginTop: 32 }} />
      ) : cargandoEstado ? (
        <ActivityIndicator size="small" color="#94a3b8" style={{ marginTop: 32 }} />
      ) : (
        <View style={s.botones}>
          <TouchableOpacity
            style={[s.btn, s.btnEntrada, !entradaHabilitada && s.btnDisabled]}
            onPress={() => marcar('ENTRADA')}
            disabled={!entradaHabilitada}
          >
            <Text style={s.btnIcon}>{estadoHoy.tieneEntrada ? '✅' : '🟢'}</Text>
            <Text style={[s.btnText, !entradaHabilitada && s.btnTextDisabled]}>
              {estadoHoy.tieneEntrada ? `Entrada\n${estadoHoy.horaEntrada}` : 'Registrar\nEntrada'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[s.btn, s.btnSalida, !salidaHabilitada && s.btnDisabled]}
            onPress={() => marcar('SALIDA')}
            disabled={!salidaHabilitada}
          >
            <Text style={s.btnIcon}>{estadoHoy.tieneSalida ? '✅' : (salidaHabilitada ? '🔴' : '🔒')}</Text>
            <Text style={[s.btnText, !salidaHabilitada && s.btnTextDisabled]}>
              {estadoHoy.tieneSalida
                ? `Salida\n${estadoHoy.horaSalida}`
                : !estadoHoy.tieneEntrada
                  ? 'Bloqueado\n(sin entrada)'
                  : 'Registrar\nSalida'}
            </Text>
          </TouchableOpacity>
        </View>
      )}

      <Text style={s.hint}>
        {tieneHuella ? '👆 Se pedirá verificación biométrica' : '⚠️ Dispositivo sin sensor biométrico'}
      </Text>
    </View>
  );
}

const s = StyleSheet.create({
  container:     { flex: 1, backgroundColor: '#f8fafc', padding: 24 },
  header:        { alignItems: 'center', marginTop: 40, marginBottom: 24 },
  headerLogo:    { fontSize: 48 },
  headerTitle:   { fontSize: 20, fontWeight: '700', color: '#1e293b' },
  headerSub:     { fontSize: 13, color: '#64748b', marginTop: 2 },
  card:          {
    backgroundColor: '#fff', borderRadius: 16, padding: 24,
    alignItems: 'center', marginBottom: 20,
    shadowColor: '#000', shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08, shadowRadius: 8, elevation: 3,
  },
  cardNombre:    { fontSize: 18, fontWeight: '700', color: '#1e293b', marginBottom: 8 },
  cardHora:      { fontSize: 38, fontWeight: '800', color: '#2563eb', fontVariant: ['tabular-nums'] },
  cardFecha:     { fontSize: 13, color: '#64748b', marginTop: 4, textTransform: 'capitalize' },
  resumenRow:    { flexDirection: 'row', gap: 24, marginTop: 16 },
  resumenItem:   { alignItems: 'center' },
  resumenLabel:  { fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 2 },
  resumenVal:    { fontSize: 16, fontWeight: '700' },
  ok:            { color: '#16a34a' },
  pendiente:     { color: '#cbd5e1' },
  bannerCompleto:{ flexDirection: 'row', alignItems: 'center', justifyContent: 'center',
                   backgroundColor: '#dcfce7', borderRadius: 12, padding: 12, marginBottom: 16, gap: 8 },
  bannerIcon:    { fontSize: 20 },
  bannerText:    { fontSize: 14, fontWeight: '700', color: '#15803d' },
  resultado:     {
    borderRadius: 12, padding: 16, flexDirection: 'row',
    alignItems: 'center', gap: 10, marginBottom: 16,
  },
  resultadoOk:   { backgroundColor: '#dcfce7' },
  resultadoErr:  { backgroundColor: '#fee2e2' },
  resultadoIcon: { fontSize: 22 },
  resultadoText: { fontSize: 14, fontWeight: '600', color: '#1e293b', flex: 1 },
  botones:       { flexDirection: 'row', gap: 16, marginTop: 8 },
  btn:           {
    flex: 1, borderRadius: 16, padding: 24, alignItems: 'center',
    shadowColor: '#000', shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15, shadowRadius: 8, elevation: 4,
  },
  btnEntrada:    { backgroundColor: '#16a34a' },
  btnSalida:     { backgroundColor: '#dc2626' },
  btnDisabled:   { backgroundColor: '#e2e8f0', shadowOpacity: 0, elevation: 0 },
  btnIcon:       { fontSize: 32, marginBottom: 8 },
  btnText:       { color: '#fff', fontSize: 15, fontWeight: '700', textAlign: 'center', lineHeight: 22 },
  btnTextDisabled: { color: '#94a3b8' },
  hint:          { textAlign: 'center', color: '#94a3b8', fontSize: 12, marginTop: 24 },
});
