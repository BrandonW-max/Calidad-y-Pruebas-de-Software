import { useLayoutEffect, useRef } from "react";

/** Native modal dialog with a labelled title, Escape handling and focus restoration. */
export default function AccessibleDialog({
  labelledBy,
  describedBy,
  onClose,
  children,
  className = "",
  style,
  closeOnBackdrop = true,
  fallbackFocus = "#main-content",
}) {
  const dialogRef = useRef(null);

  useLayoutEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog || dialog.open) return undefined;

    const returnTarget = document.activeElement;
    dialog.showModal();
    const initialTarget = dialog.querySelector("[autofocus]") ?? dialog.querySelector(
      "button:not(:disabled), a[href], input:not(:disabled), select:not(:disabled), textarea:not(:disabled), [tabindex]:not([tabindex='-1'])"
    );
    (initialTarget ?? dialog).focus();

    return () => {
      if (dialog.open) dialog.close();
      if (returnTarget && !returnTarget.isConnected) {
        document.querySelector(fallbackFocus)?.focus();
      }
    };
  }, [fallbackFocus]);

  const handleCancel = (event) => {
    event.preventDefault();
    onClose?.();
  };

  const handleClick = (event) => {
    if (closeOnBackdrop && event.target === event.currentTarget) onClose?.();
  };

  return (
    <dialog
      ref={dialogRef}
      className={`accessible-dialog ${className}`.trim()}
      aria-modal="true"
      aria-labelledby={labelledBy}
      aria-describedby={describedBy}
      onCancel={handleCancel}
      onClick={handleClick}
      style={style}
      tabIndex={-1}
    >
      {children}
    </dialog>
  );
}
