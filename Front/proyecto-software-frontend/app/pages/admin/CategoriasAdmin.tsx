import { useEffect, useState } from "react";
import apiFetch from "../../api/apiFetch";

import { Plus, Edit, Trash2, Search } from "lucide-react";

export default function CategoriasAdmin() {
  const [categorias, setCategorias] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  const fetchCategorias = async () => {
    const token = localStorage.getItem('auth_token');
    
    if (!token) {
      setError("No hay sesion activa");
      setIsLoading(false);
      return;
    }
    
    try {
      const response = await apiFetch(`/api/categorias`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      if (response.ok) {
        const data = await response.json();
        setCategorias(data?.data || []);
      } else {
        setError("Error al cargar categorias");
      }
    } catch (error) {
      setError("Error de conexion");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchCategorias();
  }, []);

  const filteredCategorias = categorias.filter(cat =>
    cat.nombre?.toLowerCase().includes(search.toLowerCase())
  );

  if (isLoading) {
    return <div className="h-96 bg-zinc-100 rounded-xl animate-pulse" />;
  }

  if (error) {
    return (
      <div className="rounded-xl bg-red-50 border border-red-200 p-6 text-center">
        <p className="text-red-700">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-primary text-white rounded-lg"
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-zinc-900">Categorias</h1>
        <p className="text-sm text-zinc-500 mt-1">
          Administra las categorias de tickets
        </p>
      </div>

      <div className="flex flex-col sm:flex-row justify-between gap-4">
        <div className="flex-1 flex items-center gap-2 h-10 px-3 rounded-lg bg-white border border-zinc-200">
          <Search className="w-4 h-4 text-zinc-400" />
          <input
            type="search"
            placeholder="Buscar categoria..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="flex-1 text-sm outline-none bg-transparent"
          />
        </div>
        <button
          onClick={() => alert("Nueva categoria - proximamente")}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary-light transition"
        >
          <Plus className="w-4 h-4" />
          Nueva Categoria
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredCategorias.map((cat) => (
          <div key={cat.id} className="rounded-xl bg-white border border-zinc-200 p-4 hover:shadow-md transition">
            <div className="flex items-start justify-between mb-2">
              <div>
                <h3 className="font-bold text-zinc-900">{cat.nombre}</h3>
                {cat.areaNombre && (
                  <p className="text-xs text-zinc-500 mt-1">Area: {cat.areaNombre}</p>
                )}
              </div>
              <span className={`px-2 py-1 rounded-full text-[10px] font-bold ${
                cat.activo ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              }`}>
                {cat.activo ? "ACTIVO" : "INACTIVO"}
              </span>
            </div>
            
            {cat.colorHex && (
              <div className="flex items-center gap-2 mt-2">
                <div className="w-4 h-4 rounded-full border border-zinc-200" style={{ backgroundColor: cat.colorHex }} />
                <span className="text-xs text-zinc-500">{cat.colorHex}</span>
              </div>
            )}
            
            <div className="flex justify-end gap-2 mt-3 pt-2 border-t border-zinc-100">
              <button
                onClick={() => alert(`Editar ${cat.nombre} - proximamente`)}
                className="p-1 text-blue-500 hover:bg-blue-50 rounded transition"
              >
                <Edit className="w-4 h-4" />
              </button>
              <button
                onClick={() => alert(`Eliminar ${cat.nombre} - proximamente`)}
                className="p-1 text-red-500 hover:bg-red-50 rounded transition"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {filteredCategorias.length === 0 && (
        <div className="rounded-xl bg-yellow-50 border border-yellow-200 p-6 text-center">
          <p className="text-yellow-700">No hay categorias registradas</p>
        </div>
      )}
    </div>
  );
}