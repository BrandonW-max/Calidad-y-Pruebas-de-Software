/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect, useCallback } from "react";

const STORAGE_KEY = "rrhh_accessibility_preferences";

const defaultPreferences = {
  highContrast: "none", // "none" | "dark" | "light"
  colorBlindMode: "none", // "none" | "protanopia" | "deuteranopia" | "tritanopia" | "achromatopsia"
  fontSize: "normal",   // "normal" | "large" | "xlarge"
  dyslexicFont: false,
  relaxedSpacing: false,
  reducedMotion: false,
  highlightInteractive: false,
  readingGuide: false,
};

const AccessibilityContext = createContext(null);

export function AccessibilityProvider({ children }) {
  const [preferences, setPreferences] = useState(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY);
      return saved ? { ...defaultPreferences, ...JSON.parse(saved) } : defaultPreferences;
    } catch {
      return defaultPreferences;
    }
  });

  const [isToolbarOpen, setIsToolbarOpen] = useState(false);
  const [mouseY, setMouseY] = useState(0);

  // Guardar en localStorage cuando cambie
  useEffect(() => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
    } catch (e) {
      console.warn("No se pudo guardar la configuración de accesibilidad:", e);
    }
  }, [preferences]);

  // Aplicar clases CSS al elemento body según las preferencias activas
  useEffect(() => {
    const body = document.body;

    // Contraste
    body.classList.remove("a11y-contrast-dark", "a11y-contrast-light");
    if (preferences.highContrast === "dark") body.classList.add("a11y-contrast-dark");
    if (preferences.highContrast === "light") body.classList.add("a11y-contrast-light");

    // Daltonismo y trastornos del color
    body.classList.remove(
      "a11y-colorblind-protanopia",
      "a11y-colorblind-deuteranopia",
      "a11y-colorblind-tritanopia",
      "a11y-colorblind-achromatopsia"
    );
    if (preferences.colorBlindMode === "protanopia") body.classList.add("a11y-colorblind-protanopia");
    if (preferences.colorBlindMode === "deuteranopia") body.classList.add("a11y-colorblind-deuteranopia");
    if (preferences.colorBlindMode === "tritanopia") body.classList.add("a11y-colorblind-tritanopia");
    if (preferences.colorBlindMode === "achromatopsia") body.classList.add("a11y-colorblind-achromatopsia");

    // Tamaño de fuente
    body.classList.remove("a11y-text-large", "a11y-text-xlarge");
    if (preferences.fontSize === "large") body.classList.add("a11y-text-large");
    if (preferences.fontSize === "xlarge") body.classList.add("a11y-text-xlarge");

    // Fuente adaptada para dislexia
    if (preferences.dyslexicFont) body.classList.add("a11y-font-dyslexic");
    else body.classList.remove("a11y-font-dyslexic");

    // Espaciado relajado
    if (preferences.relaxedSpacing) body.classList.add("a11y-spacing-relaxed");
    else body.classList.remove("a11y-spacing-relaxed");

    // Reducción de animaciones
    if (preferences.reducedMotion) body.classList.add("a11y-reduced-motion");
    else body.classList.remove("a11y-reduced-motion");

    // Resaltado de enlaces y botones
    if (preferences.highlightInteractive) body.classList.add("a11y-highlight-interactive");
    else body.classList.remove("a11y-highlight-interactive");
  }, [preferences]);

  // Manejo de la guía de lectura (regla que sigue el cursor)
  useEffect(() => {
    if (!preferences.readingGuide) return;

    const handleMouseMove = (e) => {
      setMouseY(e.clientY);
    };

    window.addEventListener("mousemove", handleMouseMove, { passive: true });
    return () => window.removeEventListener("mousemove", handleMouseMove);
  }, [preferences.readingGuide]);

  // Atajo de teclado global: Alt + A para abrir/cerrar accesibilidad
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.altKey && (e.key === "a" || e.key === "A")) {
        e.preventDefault();
        setIsToolbarOpen(prev => !prev);
      }
      if (e.key === "Escape" && isToolbarOpen) {
        setIsToolbarOpen(false);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isToolbarOpen]);

  // Helpers para modificar preferencias
  const setHighContrast = useCallback((mode) => {
    setPreferences(prev => ({ ...prev, highContrast: mode }));
  }, []);

  const setColorBlindMode = useCallback((mode) => {
    setPreferences(prev => ({ ...prev, colorBlindMode: mode }));
  }, []);

  const setFontSize = useCallback((size) => {
    setPreferences(prev => ({ ...prev, fontSize: size }));
  }, []);

  const toggleDyslexicFont = useCallback(() => {
    setPreferences(prev => ({ ...prev, dyslexicFont: !prev.dyslexicFont }));
  }, []);

  const toggleRelaxedSpacing = useCallback(() => {
    setPreferences(prev => ({ ...prev, relaxedSpacing: !prev.relaxedSpacing }));
  }, []);

  const toggleReducedMotion = useCallback(() => {
    setPreferences(prev => ({ ...prev, reducedMotion: !prev.reducedMotion }));
  }, []);

  const toggleHighlightInteractive = useCallback(() => {
    setPreferences(prev => ({ ...prev, highlightInteractive: !prev.highlightInteractive }));
  }, []);

  const toggleReadingGuide = useCallback(() => {
    setPreferences(prev => ({ ...prev, readingGuide: !prev.readingGuide }));
  }, []);

  const resetPreferences = useCallback(() => {
    setPreferences(defaultPreferences);
  }, []);

  const activeFeaturesCount =
    (preferences.highContrast !== "none" ? 1 : 0) +
    (preferences.colorBlindMode !== "none" ? 1 : 0) +
    (preferences.fontSize !== "normal" ? 1 : 0) +
    (preferences.dyslexicFont ? 1 : 0) +
    (preferences.relaxedSpacing ? 1 : 0) +
    (preferences.reducedMotion ? 1 : 0) +
    (preferences.highlightInteractive ? 1 : 0) +
    (preferences.readingGuide ? 1 : 0);

  return (
    <AccessibilityContext.Provider
      value={{
        preferences,
        isToolbarOpen,
        setIsToolbarOpen,
        setHighContrast,
        setColorBlindMode,
        setFontSize,
        toggleDyslexicFont,
        toggleRelaxedSpacing,
        toggleReducedMotion,
        toggleHighlightInteractive,
        toggleReadingGuide,
        resetPreferences,
        activeFeaturesCount,
      }}
    >
      {/* Definición de matrices SVG para corrección y simulación de daltonismo */}
      <svg
        style={{ position: "absolute", width: 0, height: 0, overflow: "hidden" }}
        aria-hidden="true"
        focusable="false"
      >
        <defs>
          {/* Protanopía: Ceguera/deficiencia al rojo */}
          <filter id="a11y-filter-protanopia">
            <feColorMatrix
              type="matrix"
              values="0.56667 0.43333 0 0 0  0.55833 0.44167 0 0 0  0 0.24167 0.75833 0 0  0 0 0 1 0"
            />
          </filter>
          {/* Deuteranopía: Ceguera/deficiencia al verde */}
          <filter id="a11y-filter-deuteranopia">
            <feColorMatrix
              type="matrix"
              values="0.625 0.375 0 0 0  0.7 0.3 0 0 0  0 0.3 0.7 0 0  0 0 0 1 0"
            />
          </filter>
          {/* Tritanopía: Ceguera/deficiencia al azul */}
          <filter id="a11y-filter-tritanopia">
            <feColorMatrix
              type="matrix"
              values="0.95 0.05 0 0 0  0 0.43333 0.56667 0 0  0 0.475 0.525 0 0  0 0 0 1 0"
            />
          </filter>
        </defs>
      </svg>

      {children}
      {preferences.readingGuide && (
        <div
          className="a11y-reading-ruler"
          style={{ top: `${mouseY}px` }}
          aria-hidden="true"
        />
      )}
    </AccessibilityContext.Provider>
  );
}

export function useAccessibility() {
  const context = useContext(AccessibilityContext);
  if (!context) {
    throw new Error("useAccessibility debe ser utilizado dentro de un AccessibilityProvider");
  }
  return context;
}
