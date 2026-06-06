package com.proyectoarquitectura.app.service.exportacion;

import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import com.proyectoarquitectura.app.repository.TicketRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.repository.LogAuditoriaRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportacionService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final LogAuditoriaRepository logAuditoriaRepository;

    public ExportacionService(TicketRepository ticketRepository,
                               UsuarioRepository usuarioRepository,
                               LogAuditoriaRepository logAuditoriaRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    // Exportar Tickets a CSV
    public byte[] exportarTicketsCSV() {
        List<Ticket> tickets = ticketRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        
        writer.println("ID,Codigo,Asunto,Descripcion,Tipo,Prioridad,Estado,Cliente,Fecha Creacion,Fecha Cierre");
        for (Ticket t : tickets) {
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    t.getId(),
                    escapeCsv(t.getCodigo()),
                    escapeCsv(t.getAsunto()),
                    escapeCsv(t.getDescripcion()),
                    escapeCsv(t.getTipo()),
                    escapeCsv(t.getPrioridad()),
                    escapeCsv(t.getEstado()),
                    t.getCliente() != null ? escapeCsv(t.getCliente().getCorreo()) : "",
                    t.getFechaCreacion() != null ? t.getFechaCreacion().toString() : "",
                    t.getFechaCierre() != null ? t.getFechaCierre().toString() : "");
        }
        writer.flush();
        return out.toByteArray();
    }

    // Exportar Usuarios a CSV
    public byte[] exportarUsuariosCSV() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        
        writer.println("ID,Nombres,Apellidos,Correo,Rol,Estado,Fecha Registro");
        for (Usuario u : usuarios) {
            writer.printf("%d,%s,%s,%s,%s,%s,%s%n",
                    u.getId(),
                    escapeCsv(u.getNombres()),
                    escapeCsv(u.getApellidos()),
                    escapeCsv(u.getCorreo()),
                    u.getRol() != null ? u.getRol().getNombre() : "",
                    u.getEstado(),
                    u.getCreadoEn() != null ? u.getCreadoEn().toString() : "");
        }
        writer.flush();
        return out.toByteArray();
    }

    // Exportar Auditoría a CSV
    public byte[] exportarAuditoriaCSV() {
        List<LogAuditoria> logs = logAuditoriaRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);
        
        writer.println("ID,Usuario,Accion,Detalles,IP,Fecha Hora");
        for (LogAuditoria log : logs) {
            writer.printf("%d,%s,%s,%s,%s,%s%n",
                    log.getId(),
                    escapeCsv(log.getUsuario()),
                    escapeCsv(log.getAccion()),
                    escapeCsv(log.getDetalles()),
                    escapeCsv(log.getIp()),
                    log.getFechaHora() != null ? log.getFechaHora().toString() : "");
        }
        writer.flush();
        return out.toByteArray();
    }

    // Exportar Tickets a JSON
    public String exportarTicketsJSON() {
        List<Ticket> tickets = ticketRepository.findAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tickets.size(); i++) {
            Ticket t = tickets.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(t.getId()).append(",")
                .append("\"codigo\":\"").append(escapeJson(t.getCodigo())).append("\",")
                .append("\"asunto\":\"").append(escapeJson(t.getAsunto())).append("\",")
                .append("\"descripcion\":\"").append(escapeJson(t.getDescripcion())).append("\",")
                .append("\"tipo\":\"").append(escapeJson(t.getTipo())).append("\",")
                .append("\"prioridad\":\"").append(escapeJson(t.getPrioridad())).append("\",")
                .append("\"estado\":\"").append(escapeJson(t.getEstado())).append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }

    // Exportar Usuarios a JSON
    public String exportarUsuariosJSON() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(u.getId()).append(",")
                .append("\"nombres\":\"").append(escapeJson(u.getNombres())).append("\",")
                .append("\"apellidos\":\"").append(escapeJson(u.getApellidos())).append("\",")
                .append("\"correo\":\"").append(escapeJson(u.getCorreo())).append("\",")
                .append("\"rol\":\"").append(u.getRol() != null ? u.getRol().getNombre() : "").append("\",")
                .append("\"estado\":\"").append(u.getEstado()).append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }

    // Exportar Auditoría a JSON
    public String exportarAuditoriaJSON() {
        List<LogAuditoria> logs = logAuditoriaRepository.findAll();
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < logs.size(); i++) {
            LogAuditoria log = logs.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(log.getId()).append(",")
                .append("\"usuario\":\"").append(escapeJson(log.getUsuario())).append("\",")
                .append("\"accion\":\"").append(escapeJson(log.getAccion())).append("\",")
                .append("\"detalles\":\"").append(escapeJson(log.getDetalles())).append("\",")
                .append("\"ip\":\"").append(escapeJson(log.getIp())).append("\",")
                .append("\"fechaHora\":\"").append(log.getFechaHora() != null ? log.getFechaHora().toString() : "").append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }

    private String escapeCsv(String str) {
        if (str == null) return "";
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}