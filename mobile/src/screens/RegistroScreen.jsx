import { useState } from 'react';
import {
  View, Text, TextInput, TouchableOpacity,
  StyleSheet, ActivityIndicator, Alert,
} from 'react-native';
import * as SecureStore from 'expo-secure-store';
import * as Crypto from 'expo-crypto';
import { registrarDispositivo, buscarEmpleadoPorDni } from '../services/api';

export default function RegistroScreen({ onRegistrado }) {
  const [dni,      setDni]      = useState('');
  const [cargando, setCargando] = useState(false);

  const handleRegistrar = async () => {
    if (!dni.trim()) {
      Alert.alert('Campo requerido', 'Ingresa tu número de DNI.');
      return;
    }
    if (!/^\d{8}$/.test(dni.trim())) {
      Alert.alert('DNI inválido', 'El DNI debe tener exactamente 8 dígitos.');
      return;
    }
    setCargando(true);
    try {
      // 1. Verificar que el empleado existe por DNI
      const empleado = await buscarEmpleadoPorDni(dni.trim());

      // 2. Generar token único para este dispositivo
      const deviceToken = await Crypto.digestStringAsync(
        Crypto.CryptoDigestAlgorithm.SHA256,
        `${dni}-${Date.now()}-${Math.random()}`
      );

      // 3. Registrar dispositivo (el backend devuelve idEmpleado)
      const resp = await registrarDispositivo(dni.trim(), deviceToken);

      // 4. Guardar localmente de forma segura
      await SecureStore.setItemAsync('deviceToken',    deviceToken);
      await SecureStore.setItemAsync('idEmpleado',     String(resp.idEmpleado ?? empleado.idEmpleado));
      await SecureStore.setItemAsync('nombreEmpleado', `${empleado.nombres} ${empleado.apellidos}`);

      onRegistrado();
    } catch (e) {
      Alert.alert('Error', e.message || 'No se pudo registrar el dispositivo.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <View style={s.container}>
      <Text style={s.logo}>🏥</Text>
      <Text style={s.titulo}>Hospital San Gabriel</Text>
      <Text style={s.subtitulo}>Registro de dispositivo</Text>
      <Text style={s.desc}>
        Ingresa tu DNI para vincular este celular a tu cuenta.{'\n'}
        Solo necesitas hacerlo una vez.
      </Text>

      <TextInput
        style={s.input}
        placeholder="Número de DNI (8 dígitos)"
        placeholderTextColor="#94a3b8"
        keyboardType="numeric"
        maxLength={8}
        value={dni}
        onChangeText={setDni}
      />

      <TouchableOpacity style={s.btn} onPress={handleRegistrar} disabled={cargando}>
        {cargando
          ? <ActivityIndicator color="#fff" />
          : <Text style={s.btnText}>Vincular dispositivo</Text>
        }
      </TouchableOpacity>
    </View>
  );
}

const s = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f0f9ff', alignItems: 'center', justifyContent: 'center', padding: 32 },
  logo:      { fontSize: 64, marginBottom: 8 },
  titulo:    { fontSize: 22, fontWeight: '700', color: '#1e293b', marginBottom: 4 },
  subtitulo: { fontSize: 15, color: '#2563eb', fontWeight: '600', marginBottom: 16 },
  desc:      { fontSize: 13, color: '#64748b', textAlign: 'center', marginBottom: 28, lineHeight: 20 },
  input:     {
    width: '100%', borderWidth: 1.5, borderColor: '#cbd5e1', borderRadius: 12,
    padding: 14, fontSize: 16, color: '#1e293b', backgroundColor: '#fff',
    marginBottom: 16,
  },
  btn:       {
    width: '100%', backgroundColor: '#2563eb', borderRadius: 12,
    padding: 16, alignItems: 'center',
    shadowColor: '#2563eb', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.3, shadowRadius: 8,
    elevation: 4,
  },
  btnText:   { color: '#fff', fontSize: 16, fontWeight: '700' },
});
