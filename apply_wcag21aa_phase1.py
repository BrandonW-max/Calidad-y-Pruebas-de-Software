#!/usr/bin/env python3
"""
Aplica una primera fase de correcciones de accesibilidad WCAG 2.1 A/AA
sobre la rama local Fernando del proyecto RRHH.

Uso desde la raíz del repositorio:
    python apply_wcag21aa_phase1.py

El script:
- verifica la rama y exige un árbol Git limpio;
- genera copias de respaldo;
- corrige destinos del enlace Saltar al contenido;
- asocia etiquetas con campos de formularios cuando están inmediatamente antes del control;
- corrige selectores de Nómina y paginación de Empleados;
- mejora contraste de dos tokens que aparecen en las evidencias;
- hace enfocables contenedores de desplazamiento de tablas y modales;
- corrige el salto de jerarquía de las tarjetas de acceso del dashboard.

No certifica conformidad WCAG ni reemplaza la revisión manual y los nuevos escaneos.
"""
from __future__ import annotations

import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path

BRANCH_REQUIRED = "Fernando"
SRC = Path("frontend/rrhh-frontend/src")
ROOT = Path.cwd()


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    raise SystemExit(1)


def git(*args: str) -> str:
    try:
        p = subprocess.run(
            ["git", *args], cwd=ROOT, check=True, capture_output=True, text=True
        )
    except (subprocess.CalledProcessError, FileNotFoundError) as exc:
        detail = getattr(exc, "stderr", "") or str(exc)
        fail(f"No se pudo ejecutar git: {detail}")
    return p.stdout.strip()


def read(rel: str) -> str:
    path = ROOT / rel
    if not path.is_file():
        fail(f"No existe el archivo esperado: {rel}")
    return path.read_text(encoding="utf-8")


def replace_once(text: str, old: str, new: str, description: str) -> str:
    count = text.count(old)
    if count != 1:
        fail(f"{description}: se esperaba una coincidencia y se encontraron {count}. "
             "No se aplicó ningún cambio.")
    return text.replace(old, new, 1)


def find_jsx_tag_end(text: str, start: int) -> int:
    """Devuelve el índice posterior a >, ignorando > dentro de expresiones JSX."""
    quote = None
    brace_depth = 0
    i = start
    while i < len(text):
        ch = text[i]
        if quote:
            if ch == quote and (i == 0 or text[i - 1] != "\\"):
                quote = None
        else:
            if ch in ("'", '"', "`"):
                quote = ch
            elif ch == "{":
                brace_depth += 1
            elif ch == "}":
                brace_depth = max(0, brace_depth - 1)
            elif ch == ">" and brace_depth == 0:
                return i + 1
        i += 1
    fail("Se encontró una etiqueta JSX sin cierre >.")


def attr_value(tag: str, name: str) -> str | None:
    m = re.search(rf'\b{name}\s*=\s*(?:"([^"]*)"|\'([^\']*)\')', tag)
    if not m:
        return None
    return m.group(1) if m.group(1) is not None else m.group(2)


def associate_adjacent_labels(content: str, prefix: str) -> tuple[str, int]:
    """
    Asocia <label>texto</label> con un <input>, <select> o <textarea>
    inmediatamente posterior. Omite labels que ya tienen htmlFor y labels
    envolventes (que ya son asociaciones implícitas).
    """
    edits: list[tuple[int, int, str]] = []
    counter = 1
    pos = 0

    while True:
        start = content.find("<label", pos)
        if start < 0:
            break
        if start + 6 < len(content) and (content[start + 6].isalnum() or content[start + 6] in "_-"):
            pos = start + 6
            continue

        label_end = find_jsx_tag_end(content, start)
        label_open = content[start:label_end]
        close_start = content.find("</label>", label_end)
        if close_start < 0:
            fail(f"Etiqueta label sin cierre en el archivo (prefijo {prefix}).")
        close_end = close_start + len("</label>")
        pos = close_end

        if re.search(r'\bhtmlFor\s*=', label_open):
            continue

        cursor = close_end
        while cursor < len(content) and content[cursor].isspace():
            cursor += 1

        m = re.match(r'<(input|select|textarea)\b', content[cursor:])
        if not m:
            continue

        control_start = cursor
        control_end = find_jsx_tag_end(content, control_start)
        control_open = content[control_start:control_end]
        existing_id = attr_value(control_open, "id")

        if existing_id:
            field_id = existing_id
            control_new = control_open
        elif re.search(r'\bid\s*=\s*\{', control_open):
            # No inferimos un id dinámico; requiere revisión manual.
            continue
        else:
            while f'id="{prefix}-{counter}"' in content:
                counter += 1
            field_id = f"{prefix}-{counter}"
            counter += 1
            insert_at = control_open.find(" ")
            if insert_at < 0:
                insert_at = control_open.rfind(">")
            control_new = (
                control_open[:insert_at]
                + f' id="{field_id}"'
                + control_open[insert_at:]
            )

        label_new = label_open[:-1] + f' htmlFor="{field_id}">'
        edits.append((start, label_end, label_new))
        edits.append((control_start, control_end, control_new))

    # Aplicar de derecha a izquierda para conservar los índices originales.
    for start, end, replacement in sorted(edits, key=lambda item: item[0], reverse=True):
        content = content[:start] + replacement + content[end:]
    return content, len(edits) // 2


def add_main_targets(content: str, rel: str) -> tuple[str, int]:
    """Añade id y tabIndex a todos los <main className="main-content..."> sin destino."""
    starts = [m.start() for m in re.finditer(r'<main\b', content)]
    edits: list[tuple[int, int, str]] = []
    for start in starts:
        end = find_jsx_tag_end(content, start)
        tag = content[start:end]
        if 'id="main-content"' in tag or "id='main-content'" in tag:
            continue
        if not re.search(r'\bclassName\s*=\s*["\']main-content(?:\s|["\'])', tag):
            continue
        replacement = tag[:-1] + ' id="main-content" tabIndex="-1">'
        edits.append((start, end, replacement))
    for start, end, replacement in sorted(edits, key=lambda item: item[0], reverse=True):
        content = content[:start] + replacement + content[end:]
    if not edits and not re.search(r'id=["\']main-content["\']', content):
        fail(f"{rel}: no se localizó el main principal para el enlace de salto.")
    return content, len(edits)


def add_tabindex_to_class_divs(content: str, class_name: str) -> tuple[str, int]:
    """Añade tabIndex=0 a divs cuyo className contiene una clase determinada."""
    pattern = re.compile(r'<div\b')
    edits = []
    for m in pattern.finditer(content):
        end = find_jsx_tag_end(content, m.start())
        tag = content[m.start():end]
        classes = attr_value(tag, "className")
        if not classes or class_name not in classes.split():
            continue
        if re.search(r'\btabIndex\s*=', tag):
            continue
        edits.append((m.start(), end, tag[:-1] + ' tabIndex={0}>'))
    for start, end, replacement in sorted(edits, key=lambda item: item[0], reverse=True):
        content = content[:start] + replacement + content[end:]
    return content, len(edits)


def build_changes() -> tuple[dict[str, str], dict[str, int]]:
    changes: dict[str, str] = {}
    report: dict[str, int] = {}

    # Rutas que actualmente renderizan <main className="main-content..."> sin destino.
    page_names = [
        "GestionEmpleados.jsx",
        "GestionHorarios.jsx",
        "ControlAsistencia.jsx",
        "SolicitudPermisos.jsx",
        "GenerarReportes.jsx",
        "GerenciaJustificaciones.jsx",
    ]
    for name in page_names:
        rel = str(SRC / "pages" / name)
        c = read(rel)
        c, count = add_main_targets(c, rel)
        changes[rel] = c
        report[f"{name}: destinos de salto"] = count

    # Login: añadir el destino global y asociaciones explícitas en campos con iconos.
    rel = str(SRC / "pages/Login.jsx")
    c = read(rel)
    c = replace_once(c, '<div className="login-page">',
                     '<main className="login-page" id="main-content" tabIndex="-1">',
                     "Login: destino main")
    c = replace_once(c, "<label>Usuario o email</label>",
                     '<label htmlFor="login-username">Usuario o email</label>',
                     "Login: etiqueta de usuario")
    c = replace_once(c, '<input\n                  type="text"\n                  placeholder="Ingrese su usuario o email"',
                     '<input\n                  id="login-username"\n                  type="text"\n                  placeholder="Ingrese su usuario o email"',
                     "Login: id de usuario")
    c = replace_once(c, "<label>Contraseña</label>",
                     '<label htmlFor="login-password">Contraseña</label>',
                     "Login: etiqueta de contraseña")
    c = replace_once(c, 'type={showPwd ? "text" : "password"}',
                     'id="login-password"\n                  type={showPwd ? "text" : "password"}',
                     "Login: id de contraseña")
    c = replace_once(c, 'onClick={() => setShowPwd(v => !v)} tabIndex={-1}>',
                     'onClick={() => setShowPwd(v => !v)} aria-label={showPwd ? "Ocultar contraseña" : "Mostrar contraseña"} aria-pressed={showPwd}>',
                     "Login: botón mostrar contraseña")
    # Login modal recovery dialog.
    c = replace_once(c, '<div className="login-modal-box">',
                     '<div className="login-modal-box" role="dialog" aria-modal="true" aria-labelledby="forgot-title">',
                     "Login: semántica diálogo recuperación")
    c = replace_once(c, "<h3>🔑 Recuperar Contraseña</h3>",
                     '<h3 id="forgot-title">🔑 Recuperar Contraseña</h3>',
                     "Login: título diálogo recuperación")
    c = replace_once(c, "<label>Usuario o correo</label>",
                     '<label htmlFor="forgot-username">Usuario o correo</label>',
                     "Login: etiqueta recuperación")
    c = replace_once(c, '<input\n                      type="text"\n                      placeholder="Ingrese su usuario o correo"',
                     '<input\n                      id="forgot-username"\n                      type="text"\n                      placeholder="Ingrese su usuario o correo"',
                     "Login: id recuperación")
    c = replace_once(c, '    </div>\n  );\n}\n',
                     '    </main>\n  );\n}\n',
                     "Login: cierre main")
    changes[rel] = c

    # Reset password: destino principal, labels, ids y botones de visibilidad.
    rel = str(SRC / "pages/ResetPassword.jsx")
    c = read(rel)
    c = replace_once(c, '<div className="login-page">',
                     '<main className="login-page" id="main-content" tabIndex="-1">',
                     "ResetPassword: destino main")
    c = replace_once(c, "<label>Nueva contraseña</label>",
                     '<label htmlFor="reset-new-password">Nueva contraseña</label>',
                     "ResetPassword: etiqueta nueva contraseña")
    c = replace_once(c, 'type={showNueva ? "text" : "password"}',
                     'id="reset-new-password"\n                    type={showNueva ? "text" : "password"}',
                     "ResetPassword: id nueva contraseña")
    c = replace_once(c, "<label>Confirmar contraseña</label>",
                     '<label htmlFor="reset-confirm-password">Confirmar contraseña</label>',
                     "ResetPassword: etiqueta confirmar contraseña")
    c = replace_once(c, 'type={showConf ? "text" : "password"}',
                     'id="reset-confirm-password"\n                    type={showConf ? "text" : "password"}',
                     "ResetPassword: id confirmar contraseña")
    c = replace_once(c, 'onClick={() => setShowNueva(v => !v)} tabIndex={-1}>',
                     'onClick={() => setShowNueva(v => !v)} aria-label={showNueva ? "Ocultar contraseña" : "Mostrar contraseña"} aria-pressed={showNueva}>',
                     "ResetPassword: botón nueva contraseña")
    c = replace_once(c, 'onClick={() => setShowConf(v => !v)} tabIndex={-1}>',
                     'onClick={() => setShowConf(v => !v)} aria-label={showConf ? "Ocultar contraseña" : "Mostrar contraseña"} aria-pressed={showConf}>',
                     "ResetPassword: botón confirmar contraseña")
    c = replace_once(c, '    </div>\n  );\n}\n',
                     '    </main>\n  );\n}\n',
                     "ResetPassword: cierre main")
    changes[rel] = c

    # Pantalla 403: el skip link también debe poder llegar a un contenido principal.
    rel = str(SRC / "pages/Acceso403.jsx")
    c = read(rel)
    c = replace_once(c, '<div className="acceso-403-page">',
                     '<main className="acceso-403-page" id="main-content" tabIndex="-1">',
                     "403: destino main")
    c = replace_once(c, '    </div>\n  );\n}\n',
                     '    </main>\n  );\n}\n',
                     "403: cierre main")
    changes[rel] = c

    # Asociar etiquetas que aparecen inmediatamente antes de un control.
    form_pages = [
        "GestionEmpleados.jsx",
        "GestionHorarios.jsx",
        "ControlAsistencia.jsx",
        "SolicitudPermisos.jsx",
        "GenerarReportes.jsx",
        "CalculoNomina.jsx",
    ]
    for name in form_pages:
        rel = str(SRC / "pages" / name)
        c = changes.get(rel, read(rel))
        prefix = re.sub(r'[^a-z0-9]+', '-', name.lower()).strip("-")
        c, count = associate_adjacent_labels(c, f"a11y-{prefix}")
        changes[rel] = c
        report[f"{name}: etiquetas asociadas automáticamente"] = count

    # Campo de búsqueda de empleados: la etiqueta precede a un contenedor con icono.
    rel = str(SRC / "pages/GestionEmpleados.jsx")
    c = changes[rel]
    c = replace_once(c, '<label className="filter-label">Buscar por nombre</label>',
                     '<label className="filter-label" htmlFor="employee-search-name">Buscar por nombre</label>',
                     "Empleados: etiqueta de búsqueda")
    c = replace_once(c, '<input className="filter-input" placeholder="Ingrese nombre"',
                     '<input id="employee-search-name" className="filter-input" placeholder="Ingrese nombre"',
                     "Empleados: id de búsqueda")
    # Selector de número de filas: etiqueta visualmente oculta para lectores de pantalla.
    c = replace_once(c, '              <select className="pagination-size-select" value={pageSize}',
                     '              <label className="sr-only" htmlFor="employee-page-size">Cantidad de registros por página</label>\n              <select id="employee-page-size" className="pagination-size-select" value={pageSize}',
                     "Empleados: etiqueta selector de paginación")
    changes[rel] = c

    # Nómina: Departamento y Empleado tenían texto visual en div, no etiquetas.
    rel = str(SRC / "pages/CalculoNomina.jsx")
    c = changes[rel]
    c = replace_once(
        c,
        '<div style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 4 }}>Departamento</div>\n                  <select value={filtroDepartamento}',
        '<label htmlFor="payroll-filter-department" style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 4 }}>Departamento</label>\n                  <select id="payroll-filter-department" value={filtroDepartamento}',
        "Nómina: etiqueta filtro departamento"
    )
    c = replace_once(
        c,
        '<div style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 4 }}>Empleado</div>\n                  <select value={filtroEmpleado}',
        '<label htmlFor="payroll-filter-employee" style={{ fontSize: 11, color: "var(--text-3)", fontWeight: 600, textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 4 }}>Empleado</label>\n                  <select id="payroll-filter-employee" value={filtroEmpleado}',
        "Nómina: etiqueta filtro empleado"
    )
    changes[rel] = c

    # Contraste: tokens documentados en WAVE/axe con ratios bajo el umbral 4.5:1.
    rel = str(SRC / "styles/Dashboard.css")
    c = read(rel)
    c = replace_once(c, '--text-3: var(--color-ink-400, #94a3b8);',
                     '--text-3: var(--color-ink-500, #64748b);',
                     "Dashboard.css: token texto secundario")
    changes[rel] = c

    rel = str(SRC / "index.css")
    c = read(rel)
    c = replace_once(c, '--color-signal-600: #0d9c8a;',
                     '--color-signal-600: #087568;',
                     "index.css: token signal contrast")
    changes[rel] = c

    # En los dashboards, QuickAccessCard renderizaba h3 después del h1 sin h2 intermedio.
    rel = str(SRC / "components/QuickAccessCard.jsx")
    c = read(rel)
    c = replace_once(c, "<h3\n          className={`break-words",
                     "<h2\n          className={`break-words",
                     "QuickAccessCard: nivel de encabezado")
    c = replace_once(c, "</h3>",
                     "</h2>",
                     "QuickAccessCard: cierre de encabezado")
    changes[rel] = c

    # Los table-wrapper tienen overflow horizontal; varios también son verticales.
    # Hacerlos enfocables permite desplazarlos con teclado.
    for name in ["GestionEmpleados.jsx", "ControlAsistencia.jsx", "SolicitudPermisos.jsx", "GenerarReportes.jsx"]:
        rel = str(SRC / "pages" / name)
        c = changes.get(rel, read(rel))
        pattern = re.compile(r'<div\b')
        edits = []
        for m in pattern.finditer(c):
            end = find_jsx_tag_end(c, m.start())
            tag = c[m.start():end]
            classes = attr_value(tag, "className")
            if not classes or "table-wrapper" not in classes.split():
                continue
            if re.search(r'\btabIndex\s*=', tag):
                continue
            edits.append((m.start(), end, tag[:-1] + ' tabIndex={0}>'))
        for start, end, replacement in sorted(edits, key=lambda item: item[0], reverse=True):
            c = c[:start] + replacement + c[end:]
        report[f"{name}: tablas desplazables enfocables"] = len(edits)
        changes[rel] = c

    # Los modales pueden tener max-height y overflow-y:auto en Dashboard.css.
    for name in ["GestionEmpleados.jsx", "GestionHorarios.jsx", "ControlAsistencia.jsx", "SolicitudPermisos.jsx", "GenerarReportes.jsx"]:
        rel = str(SRC / "pages" / name)
        c = changes.get(rel, read(rel))
        c, count = add_tabindex_to_class_divs(c, "modal-box")
        report[f"{name}: modales enfocables"] = count
        changes[rel] = c

    # Solo conservar archivos que realmente cambian.
    original_cache = {rel: read(rel) for rel in changes}
    changes = {rel: text for rel, text in changes.items() if text != original_cache[rel]}
    return changes, report


def main() -> None:
    branch = git("branch", "--show-current")
    if branch != BRANCH_REQUIRED:
        fail(f"Estás en la rama '{branch}'. Cambia a '{BRANCH_REQUIRED}' antes de aplicar.")
    if git("status", "--porcelain"):
        fail("Hay cambios locales sin confirmar. Haz commit o stash primero; el script no los sobrescribirá.")

    changes, report = build_changes()
    if not changes:
        print("No se detectaron cambios pendientes para esta fase.")
        return

    stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_root = ROOT / f".wcag21aa-backup-{stamp}"
    for rel, new_text in changes.items():
        path = ROOT / rel
        backup = backup_root / rel
        backup.parent.mkdir(parents=True, exist_ok=True)
        backup.write_text(path.read_text(encoding="utf-8"), encoding="utf-8")
        path.write_text(new_text, encoding="utf-8")

    print(f"\nCambios aplicados en la rama {BRANCH_REQUIRED}:")
    for rel in changes:
        print(f"  - {rel}")
    print(f"\nCopias originales guardadas en: {backup_root}")
    print("\nResumen de asociaciones/ajustes detectados:")
    for label, count in report.items():
        if count:
            print(f"  {label}: {count}")
    print("\nAhora valida:")
    print("  cd frontend/rrhh-frontend")
    print("  npm run lint")
    print("  npm run build")
    print("\nDespués revisa: git diff")


if __name__ == "__main__":
    main()
