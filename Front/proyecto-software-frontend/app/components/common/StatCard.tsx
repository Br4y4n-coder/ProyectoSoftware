import type { ReactNode } from "react";

interface StatCardProps {
  label: string;
  value: string | number;
  subtext?: string;
  trend?: { value: string; positive?: boolean };
  accent?: "blue" | "green" | "amber" | "red";
  icon?: ReactNode;
}

const accentMap = {
  blue: "border-l-blue-500",
  green: "border-l-emerald-500",
  amber: "border-l-amber-500",
  red: "border-l-red-500",
};

export function StatCard({
  label,
  value,
  subtext,
  trend,
  accent = "blue",
  icon,
}: StatCardProps) {
  return (
    <div
      className={`rounded-xl bg-white border border-zinc-200 border-l-4 ${accentMap[accent]} p-5 shadow-sm hover:shadow-md transition-shadow duration-200`}
    >
      <div className="flex items-start justify-between gap-2">
        <p className="text-[11px] font-semibold tracking-wider text-zinc-500 uppercase">
          {label}
        </p>
        {icon && <span className="text-zinc-400">{icon}</span>}
      </div>
      <p className="mt-2 text-2xl font-bold text-zinc-900">{value}</p>
      {subtext && <p className="mt-1 text-sm text-zinc-500">{subtext}</p>}
      {trend && (
        <p
          className={`mt-1 text-sm font-medium ${
            trend.positive ? "text-emerald-600" : "text-zinc-500"
          }`}
        >
          {trend.value}
        </p>
      )}
    </div>
  );
}
