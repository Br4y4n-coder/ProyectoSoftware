package com.proyectoarquitectura.app.models.dto.tickets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidacionTicketActivoResponse {

    private boolean tieneTicketActivo;
    private TicketResponse ticketActivo;
}
