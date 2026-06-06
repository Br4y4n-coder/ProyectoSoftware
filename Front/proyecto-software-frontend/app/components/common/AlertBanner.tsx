import type { ReactNode } from "react";
import { AlertTriangle, X } from "lucide-react";

export function AlertBanner({
  children,
  variant = "danger",
  onDismiss,
}: {
  children: ReactNode;
  variant?: "danger" | "warning" | "info";
  onDismiss?: () => void;
}) {
  const styles = {
    danger: "bg-red-50 border-red-200 text-red-800",
    warning: "bg-amber-50 border-amber-200 text-amber-800",
    info: "bg-blue-50 border-blue-200 text-blue-800",
  };
  return (
    <div
      className={`flex items-center gap-3 px-4 py-3 rounded-lg border ${styles[variant]} transition-opacity`}
    >
      <AlertTriangle className="w-5 h-5 shrink-0" />
      <p className="flex-1 text-sm font-medium">{children}</p>
      {onDismiss && (
        <button
          type="button"
          onClick={onDismiss}
          className="shrink-0 p-1 rounded hover:bg-black/5 transition"
          aria-label="Cerrar alerta"
        >
          <X className="w-4 h-4" />
        </button>
      )}
    </div>
  );
}
