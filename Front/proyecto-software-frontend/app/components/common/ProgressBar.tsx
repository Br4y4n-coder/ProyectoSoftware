interface ProgressBarProps {
  value: number;
  variant?: "default" | "danger" | "warning";
  label?: string;
}

const fillMap = {
  default: "bg-blue-500",
  danger: "bg-red-500",
  warning: "bg-amber-500",
};

export function ProgressBar({
  value,
  variant = "default",
  label,
}: ProgressBarProps) {
  const clamped = Math.min(100, Math.max(0, value));
  return (
    <div className="space-y-1">
      {label && (
        <div className="flex justify-between text-xs text-zinc-500">
          <span>{label}</span>
          <span>{clamped}%</span>
        </div>
      )}
      <div className="h-2 rounded-full bg-zinc-100 overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-500 ${fillMap[variant]}`}
          style={{ width: `${clamped}%` }}
        />
      </div>
    </div>
  );
}
