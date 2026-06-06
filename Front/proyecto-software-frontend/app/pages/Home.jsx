import { useEffect } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import HomeAdministrador from "./HomeAdministrador";
import HomeAgente from "./HomeAgente";
import HomeUsuario from "./HomeUsuario";

export default function Home() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const rol = (user?.rol || "").toLowerCase();

  useEffect(() => {
    if (rol === "agente") navigate("/agent/dashboard", { replace: true });
    else if (rol === "administrador") navigate("/admin/dashboard", { replace: true });
  }, [rol, navigate]);

  switch (rol) {
    case "administrador":
      return null;
    case "agente":
      return null;
    case "usuario":
      return <HomeUsuario />;
    default:
      return <UnknownRole rol={user?.rol} />;
  }
}

function UnknownRole({ rol }) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[40vh] space-y-3 text-center">
      <h1 className="text-3xl font-bold text-zinc-900">Rol desconocido</h1>
      <p className="text-zinc-500 max-w-md">
        {rol
          ? `Tu cuenta tiene el rol "${rol}", que aún no tiene una vista asociada.`
          : "No pudimos determinar tu rol. Cierra sesión e inicia de nuevo."}
      </p>
    </div>
  );
}
