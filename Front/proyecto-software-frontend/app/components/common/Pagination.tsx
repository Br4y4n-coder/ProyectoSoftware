import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationProps {
  from: number;
  to: number;
  total: number;
}

export function Pagination({ from, to, total }: PaginationProps) {
  return (
    <div className="flex items-center justify-between px-4 py-3 border-t border-zinc-100 text-sm text-zinc-500">
      <span>
        Mostrando {from}-{to} de {total} tickets
      </span>
      <div className="flex items-center gap-1">
        <button
          type="button"
          className="p-2 rounded-lg hover:bg-zinc-100 text-zinc-600 transition disabled:opacity-40"
          disabled={from <= 1}
          aria-label="Página anterior"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <button
          type="button"
          className="p-2 rounded-lg hover:bg-zinc-100 text-zinc-600 transition"
          aria-label="Página siguiente"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
