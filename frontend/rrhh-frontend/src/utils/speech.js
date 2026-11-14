// ════════════════════════════════════════════════════════════════
// speech.js — Servicio Web Speech API (Text-to-Speech)
// Accesibilidad ODS 10 para personas con discapacidad visual y cognitiva
// ════════════════════════════════════════════════════════════════

let currentUtterance = null;
let currentOnEndCallback = null;

export function getCurrentUtterance() {
  return currentUtterance;
}

/**
 * Comprueba si el navegador soporta síntesis de voz
 */
export function isSpeechSupported() {
  return typeof window !== "undefined" && "speechSynthesis" in window && "SpeechSynthesisUtterance" in window;
}

/**
 * Obtiene la mejor voz en español disponible en el sistema
 */
export function getSpanishVoice() {
  if (!isSpeechSupported()) return null;
  const voices = window.speechSynthesis.getVoices();
  if (!voices || voices.length === 0) return null;

  // Priorizar español de Perú o Latinoamérica, luego cualquier español
  const esPe = voices.find(v => v.lang === "es-PE");
  if (esPe) return esPe;

  const esLatin = voices.find(v => v.lang.startsWith("es-419") || v.lang === "es-MX" || v.lang === "es-US" || v.lang === "es-CO");
  if (esLatin) return esLatin;

  const esAny = voices.find(v => v.lang.toLowerCase().startsWith("es"));
  if (esAny) return esAny;

  return voices[0] || null;
}

/**
 * Sintetiza un texto en voz alta
 * @param {string} text - Texto a narrar
 * @param {object} options - Opciones: rate (velocidad), pitch (tono), onStart, onEnd, onError
 */
export function speakText(text, options = {}) {
  if (!isSpeechSupported()) {
    console.warn("La síntesis de voz no es soportada en este navegador.");
    options.onError?.(new Error("Síntesis de voz no soportada"));
    return false;
  }

  // Detener cualquier locución previa
  stopSpeech();

  if (!text || typeof text !== "string" || text.trim().length === 0) {
    return false;
  }

  const cleanText = text.replace(/[*_#`]/g, " ").trim();
  const utterance = new SpeechSynthesisUtterance(cleanText);

  const voice = getSpanishVoice();
  if (voice) {
    utterance.voice = voice;
    utterance.lang = voice.lang || "es-ES";
  } else {
    utterance.lang = "es-ES";
  }

  utterance.rate = options.rate ?? 0.95; // Velocidad ligeramente pausada para claridad cognitiva
  utterance.pitch = options.pitch ?? 1.0;

  currentOnEndCallback = options.onEnd;

  utterance.onstart = () => {
    options.onStart?.();
  };

  utterance.onend = () => {
    currentUtterance = null;
    currentOnEndCallback = null;
    options.onEnd?.();
  };

  utterance.onerror = (e) => {
    currentUtterance = null;
    currentOnEndCallback = null;
    // Si fue cancelado voluntariamente, no considerarlo error crítico
    if (e.error !== "canceled" && e.error !== "interrupted") {
      options.onError?.(e);
    } else {
      options.onEnd?.();
    }
  };

  currentUtterance = utterance;
  window.speechSynthesis.speak(utterance);
  return true;
}

/**
 * Detiene la reproducción de voz actual
 */
export function stopSpeech() {
  if (isSpeechSupported()) {
    window.speechSynthesis.cancel();
  }
  if (currentOnEndCallback) {
    try {
      currentOnEndCallback();
    } catch {
      // ignore
    }
  }
  currentUtterance = null;
  currentOnEndCallback = null;
}

/**
 * Pausa la locución
 */
export function pauseSpeech() {
  if (isSpeechSupported() && window.speechSynthesis.speaking) {
    window.speechSynthesis.pause();
  }
}

/**
 * Reanuda la locución
 */
export function resumeSpeech() {
  if (isSpeechSupported() && window.speechSynthesis.paused) {
    window.speechSynthesis.resume();
  }
}

/**
 * Indica si actualmente está hablando
 */
export function isSpeaking() {
  return isSpeechSupported() && (window.speechSynthesis.speaking || window.speechSynthesis.pending);
}

/**
 * Formateador de moneda en soles a palabras habladas para mejor comprensión auditiva
 * Ejemplo: 2500.50 -> "2500 soles con 50 céntimos"
 */
export function formatCurrencyForSpeech(amount) {
  const num = Number(amount) || 0;
  const parts = num.toFixed(2).split(".");
  const soles = parseInt(parts[0], 10);
  const centimos = parseInt(parts[1], 10);

  let result = `${soles} ${soles === 1 ? "sol" : "soles"}`;
  if (centimos > 0) {
    result += ` con ${centimos} céntimos`;
  }
  return result;
}

/**
 * Genera el guion sonoro accesible de una boleta de nómina individual
 */
export function buildPayrollSpeechNarrative(nomina, periodoLabel = "") {
  if (!nomina) return "";

  const nombre = nomina.nombreEmpleado || nomina.empleadoNombre || nomina.nombres || "colaborador";
  const periodo = periodoLabel || (nomina.mes && nomina.anio ? `${nomina.mes} de ${nomina.anio}` : (nomina.periodo || "actual"));

  const sueldoBase = Number(nomina.sueldoBase ?? nomina.sueldo ?? 0);
  const totalBonif = (Number(nomina.bonifFamiliar) || 0) +
                     (Number(nomina.bonifTurnoNocturno) || 0) +
                     (Number(nomina.bonifGuardia) || 0) +
                     (Number(nomina.bonifRiesgo) || 0) +
                     (Number(nomina.bonifCargo) || 0);

  const totalDesc = (Number(nomina.descuentoTardanzas) || 0) +
                    (Number(nomina.descuentoLey) || 0);

  const neto = Number(nomina.sueldoNeto ?? Math.max(0, sueldoBase + totalBonif - totalDesc));

  const frases = [
    `Hola ${nombre}. Este es el resumen de tu boleta de pago para el período ${periodo}.`,
    `Tu sueldo devengado base es de ${formatCurrencyForSpeech(sueldoBase)}.`,
  ];

  if (totalBonif > 0) {
    frases.push(`Tienes un total de bonificaciones de ${formatCurrencyForSpeech(totalBonif)}.`);
  } else {
    frases.push("No se registraron bonificaciones adicionales este período.");
  }

  if (totalDesc > 0) {
    frases.push(`Se aplicaron descuentos por ley y tardanzas por un total de ${formatCurrencyForSpeech(totalDesc)}.`);
  } else {
    frases.push("No tienes descuentos en este período.");
  }

  frases.push(`Por lo tanto, el sueldo neto a cobrar depositado en tu cuenta es de ${formatCurrencyForSpeech(neto)}.`);
  frases.push("Fin del resumen. Puedes descargar el comprobante en PDF cuando lo desees.");

  return frases.join(" ");
}

/**
 * Genera el guion sonoro del resumen general para RRHH
 */
export function buildGeneralPayrollSummaryNarrative(listaNominas, periodoLabel = "") {
  if (!Array.isArray(listaNominas) || listaNominas.length === 0) {
    return "No hay datos de nómina calculados para el período seleccionado.";
  }

  const totalColaboradores = listaNominas.length;
  const totalBruto = listaNominas.reduce((acc, n) => acc + Number(n.sueldoBruto ?? n.sueldoBase ?? 0), 0);
  const totalNeto = listaNominas.reduce((acc, n) => acc + Number(n.sueldoNeto ?? 0), 0);

  return `Resumen general de nómina para el período ${periodoLabel}. Se han procesado ${totalColaboradores} empleados. El importe bruto acumulado es de ${formatCurrencyForSpeech(totalBruto)}, y el monto neto total a pagar a los trabajadores es de ${formatCurrencyForSpeech(totalNeto)}.`;
}
