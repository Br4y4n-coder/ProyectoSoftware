export function Avatar({
  initials,
  size = "md",
  className = "",
}: {
  initials: string;
  size?: "sm" | "md" | "lg";
  className?: string;
}) {
  const sizes = {
    sm: "w-7 h-7 text-[10px]",
    md: "w-8 h-8 text-xs",
    lg: "w-10 h-10 text-sm",
  };
  return (
    <div
      className={`rounded-full bg-primary flex items-center justify-center shrink-0 font-bold text-white ${sizes[size]} ${className}`}
    >
      {initials}
    </div>
  );
}
