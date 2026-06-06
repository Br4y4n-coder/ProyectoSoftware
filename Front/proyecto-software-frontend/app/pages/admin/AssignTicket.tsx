import { useEffect } from "react";
import { useNavigate, useParams } from "react-router";
import AssignTicketModal from "../../components/admin/AssignTicketModal";

/**
 * Ruta /admin/assign-ticket/:ticketId — muestra el modal de asignación
 * sobre un fondo atenuado y vuelve al dashboard al cerrar.
 */
export default function AssignTicket() {
  const { ticketId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    if (!ticketId) navigate("/admin/dashboard");
  }, [ticketId, navigate]);

  if (!ticketId) return null;

  return (
    <AssignTicketModal
      ticketId={ticketId}
      onClose={() => navigate("/admin/dashboard")}
      onAssigned={() => navigate("/admin/dashboard")}
    />
  );
}
