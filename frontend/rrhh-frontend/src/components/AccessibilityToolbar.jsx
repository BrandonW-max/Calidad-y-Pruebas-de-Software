import { useEffect, useRef } from "react";
import {
  Accessibility,
  X,
  Type,
  Sun,
  Moon,
  Sparkles,
  RotateCcw,
  Eye
} from "lucide-react";
import { useAccessibility } from "../context/AccessibilityContext";

export default function AccessibilityToolbar() {
  const triggerRef = useRef(null);
  const panelRef = useRef(null);
  const closeButtonRef = useRef(null);
  const wasOpenRef = useRef(false);
  const {
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
  } = useAccessibility();

  useEffect(() => {
    if (!isToolbarOpen) {
      if (wasOpenRef.current) {
        wasOpenRef.current = false;
        if (triggerRef.current?.isConnected) triggerRef.current.focus();
        else document.querySelector("#main-content")?.focus();
      }
      return undefined;
    }

    wasOpenRef.current = true;
    closeButtonRef.current?.focus();

    const handleTab = (event) => {
      if (event.key !== "Tab") return;

      const focusable = panelRef.current?.querySelectorAll(
        'a[href], button:not(:disabled), input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex="-1"])'
      );
      const visible = Array.from(focusable ?? []).filter(element =>
        element.getClientRects().length > 0 && !element.closest('[inert], [aria-hidden="true"]')
      );

      if (!visible.length) {
        event.preventDefault();
        panelRef.current?.focus();
        return;
      }

      const first = visible[0];
      const last = visible[visible.length - 1];
      const active = document.activeElement;
      if (!panelRef.current?.contains(active)) {
        event.preventDefault();
        (event.shiftKey ? last : first).focus();
      } else if (event.shiftKey && active === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && active === last) {
        event.preventDefault();
        first.focus();
      }
    };

    document.addEventListener("keydown", handleTab);
    return () => document.removeEventListener("keydown", handleTab);
  }, [isToolbarOpen]);

  return (
    <>
      {/* Botón flotante accesible */}
      <button
        ref={triggerRef}
        type="button"
        className="a11y-floating-trigger"
        onClick={() => setIsToolbarOpen(prev => !prev)}
        aria-label="Abrir panel de accesibilidad (Atajo Alt + A)"
        aria-expanded={isToolbarOpen}
        title="Herramientas de Accesibilidad ODS 10 (Alt + A)"
      >
        <Accessibility size={28} aria-hidden="true" />
        {activeFeaturesCount > 0 && (
          <span className="a11y-floating-badge" aria-label={`${activeFeaturesCount} ajustes activos`}>
            {activeFeaturesCount}
          </span>
        )}
      </button>

      {/* Drawer / Panel lateral */}
      {isToolbarOpen && (
        <div
          className="a11y-panel-backdrop"
          onClick={() => setIsToolbarOpen(false)}
          role="dialog"
          aria-modal="true"
          aria-labelledby="a11y-title"
        >
          <div
            ref={panelRef}
            className="a11y-panel-drawer"
            onClick={(e) => e.stopPropagation()}
            tabIndex={-1}
          >
            {/* Cabecera */}
            <div className="a11y-panel-header">
              <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <Accessibility size={22} color="#60a5fa" />
                <div>
                  <h2 id="a11y-title" style={{ margin: 0, fontSize: 16, fontWeight: 700 }}>
                    Accesibilidad (ODS 10)
                  </h2>
                  <span style={{ fontSize: 11, color: "#94a3b8" }}>
                    Inclusión y reducción de desigualdades
                  </span>
                </div>
              </div>
              <button
                ref={closeButtonRef}
                type="button"
                onClick={() => setIsToolbarOpen(false)}
                style={{
                  background: "transparent",
                  border: "none",
                  color: "#ffffff",
                  cursor: "pointer",
                  padding: 4,
                  borderRadius: 6,
                }}
                aria-label="Cerrar panel de accesibilidad"
              >
                <X size={20} />
              </button>
            </div>

            {/* Contenido */}
            <div className="a11y-panel-body">
              {/* Notificación de atajo */}
              <div style={{
                background: "#f1f5f9",
                borderRadius: 8,
                padding: "8px 12px",
                fontSize: 12,
                color: "#475569",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between"
              }}>
                <span>💡 Atajo rápido:</span>
                <kbd style={{ background: "#e2e8f0", padding: "2px 6px", borderRadius: 4, fontWeight: 700, fontSize: 11 }}>
                  Alt + A
                </kbd>
              </div>

              {/* SECCIÓN 1: CONTRASTE VISUAL */}
              <div>
                <div className="a11y-section-title">Contraste de Pantalla</div>
                <div className="a11y-btn-group">
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.highContrast === "none" ? "active" : ""}`}
                    onClick={() => setHighContrast("none")}
                  >
                    <Sun size={18} />
                    <span>Estándar</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.highContrast === "dark" ? "active" : ""}`}
                    onClick={() => setHighContrast("dark")}
                  >
                    <Moon size={18} />
                    <span>Alto Oscuro</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.highContrast === "light" ? "active" : ""}`}
                    onClick={() => setHighContrast("light")}
                  >
                    <Sparkles size={18} />
                    <span>Alto Claro</span>
                  </button>
                </div>
              </div>

              {/* SECCIÓN 2: TAMAÑO DE TEXTO */}
              <div>
                <div className="a11y-section-title">Tamaño de Texto</div>
                <div className="a11y-btn-group">
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.fontSize === "normal" ? "active" : ""}`}
                    onClick={() => setFontSize("normal")}
                  >
                    <Type size={16} />
                    <span>Normal 100%</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.fontSize === "large" ? "active" : ""}`}
                    onClick={() => setFontSize("large")}
                  >
                    <Type size={20} />
                    <span>Grande 110%</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.fontSize === "xlarge" ? "active" : ""}`}
                    onClick={() => setFontSize("xlarge")}
                  >
                    <Type size={24} />
                    <span>Extra 120%</span>
                  </button>
                </div>
              </div>

              {/* SECCIÓN 3: MODOS PARA DALTONISMO Y SALUD VISUAL */}
              <div>
                <div className="a11y-section-title" style={{ display: "flex", alignItems: "center", gap: 6 }}>
                  <Eye size={14} color="#0284c7" />
                  <span>Modos para Daltonismo (ODS 10)</span>
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(110px, 1fr))", gap: 6 }}>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.colorBlindMode === "none" ? "active" : ""}`}
                    onClick={() => setColorBlindMode("none")}
                    style={{ padding: "8px 6px" }}
                  >
                    <span style={{ fontWeight: 700 }}>Estándar</span>
                    <span style={{ fontSize: 10, opacity: 0.75 }}>Sin filtro</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.colorBlindMode === "deuteranopia" ? "active" : ""}`}
                    onClick={() => setColorBlindMode("deuteranopia")}
                    title="Ajuste para debilidad al color verde (el más habitual)"
                    style={{ padding: "8px 6px" }}
                  >
                    <span style={{ fontWeight: 700 }}>Deuteranopía</span>
                    <span style={{ fontSize: 10, opacity: 0.75 }}>Verde</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.colorBlindMode === "protanopia" ? "active" : ""}`}
                    onClick={() => setColorBlindMode("protanopia")}
                    title="Ajuste para debilidad al color rojo"
                    style={{ padding: "8px 6px" }}
                  >
                    <span style={{ fontWeight: 700 }}>Protanopía</span>
                    <span style={{ fontSize: 10, opacity: 0.75 }}>Rojo</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.colorBlindMode === "tritanopia" ? "active" : ""}`}
                    onClick={() => setColorBlindMode("tritanopia")}
                    title="Ajuste para debilidad al color azul"
                    style={{ padding: "8px 6px" }}
                  >
                    <span style={{ fontWeight: 700 }}>Tritanopía</span>
                    <span style={{ fontSize: 10, opacity: 0.75 }}>Azul</span>
                  </button>
                  <button
                    type="button"
                    className={`a11y-toggle-btn ${preferences.colorBlindMode === "achromatopsia" ? "active" : ""}`}
                    onClick={() => setColorBlindMode("achromatopsia")}
                    title="Escala de grises completa para acromatopsia"
                    style={{ padding: "8px 6px" }}
                  >
                    <span style={{ fontWeight: 700 }}>Monocromo</span>
                    <span style={{ fontSize: 10, opacity: 0.75 }}>Grises</span>
                  </button>
                </div>
              </div>

              {/* SECCIÓN 3: LEGIBILIDAD Y COGNICIÓN */}
              <div>
                <div className="a11y-section-title">Legibilidad y Adaptabilidad Cognitiva</div>
                <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                  <label className="a11y-switch-row" style={{ cursor: "pointer" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1e293b" }}>
                        Fuente para Dislexia
                      </div>
                      <div style={{ fontSize: 11, color: "#64748b" }}>
                        Tipografía de alta diferenciación de caracteres
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.dyslexicFont}
                      onChange={toggleDyslexicFont}
                      style={{ width: 18, height: 18, accentColor: "#2563eb", cursor: "pointer" }}
                    />
                  </label>

                  <label className="a11y-switch-row" style={{ cursor: "pointer" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1e293b" }}>
                        Espaciado Cómodo
                      </div>
                      <div style={{ fontSize: 11, color: "#64748b" }}>
                        Aumenta el interlineado y separación de letras
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.relaxedSpacing}
                      onChange={toggleRelaxedSpacing}
                      style={{ width: 18, height: 18, accentColor: "#2563eb", cursor: "pointer" }}
                    />
                  </label>

                  <label className="a11y-switch-row" style={{ cursor: "pointer" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1e293b" }}>
                        Guía de Lectura
                      </div>
                      <div style={{ fontSize: 11, color: "#64748b" }}>
                        Regla focal horizontal que sigue tu cursor
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.readingGuide}
                      onChange={toggleReadingGuide}
                      style={{ width: 18, height: 18, accentColor: "#2563eb", cursor: "pointer" }}
                    />
                  </label>
                </div>
              </div>

              {/* SECCIÓN 4: NAVEGACIÓN Y FOCO */}
              <div>
                <div className="a11y-section-title">Interacción y Movimiento</div>
                <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                  <label className="a11y-switch-row" style={{ cursor: "pointer" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1e293b" }}>
                        Resaltar Enlaces y Botones
                      </div>
                      <div style={{ fontSize: 11, color: "#64748b" }}>
                        Subrayado y bordes visibles en elementos clickeables
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.highlightInteractive}
                      onChange={toggleHighlightInteractive}
                      style={{ width: 18, height: 18, accentColor: "#2563eb", cursor: "pointer" }}
                    />
                  </label>

                  <label className="a11y-switch-row" style={{ cursor: "pointer" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1e293b" }}>
                        Reducir Animaciones
                      </div>
                      <div style={{ fontSize: 11, color: "#64748b" }}>
                        Evita parpadeos y transiciones bruscas
                      </div>
                    </div>
                    <input
                      type="checkbox"
                      checked={preferences.reducedMotion}
                      onChange={toggleReducedMotion}
                      style={{ width: 18, height: 18, accentColor: "#2563eb", cursor: "pointer" }}
                    />
                  </label>
                </div>
              </div>

              {/* BOTÓN RESTABLECER */}
              <div style={{ marginTop: "auto", paddingTop: 16 }}>
                <button
                  type="button"
                  onClick={resetPreferences}
                  className="btn"
                  style={{
                    width: "100%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    gap: 8,
                    background: "#f1f5f9",
                    color: "#475569",
                    border: "1px solid #cbd5e1",
                    padding: "10px",
                    borderRadius: 8,
                    fontWeight: 600,
                    fontSize: 13,
                    cursor: "pointer",
                  }}
                >
                  <RotateCcw size={16} />
                  <span>Restablecer valores predeterminados</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
