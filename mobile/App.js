import { useEffect, useState } from 'react';
import { StatusBar } from 'expo-status-bar';
import * as SecureStore from 'expo-secure-store';
import RegistroScreen  from './src/screens/RegistroScreen';
import AsistenciaScreen from './src/screens/AsistenciaScreen';

export default function App() {
  const [registrado, setRegistrado] = useState(null); // null=cargando, false=nuevo, true=vinculado

  const verificarRegistro = () => {
    SecureStore.getItemAsync('deviceToken').then(token => {
      setRegistrado(!!token);
    });
  };

  useEffect(() => {
    verificarRegistro();
  }, []);

  if (registrado === null) return null; // splash nativo mientras carga

  return (
    <>
      <StatusBar style="dark" />
      {registrado
        ? <AsistenciaScreen onDesvincular={() => setRegistrado(false)} />
        : <RegistroScreen onRegistrado={() => setRegistrado(true)} />
      }
    </>
  );
}
