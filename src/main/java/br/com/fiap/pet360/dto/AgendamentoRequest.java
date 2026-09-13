package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record AgendamentoRequest(

        @NotNull(message = "Data/hora é obrigatória")
        LocalDateTime dataHora,

        @NotNull(message = "Tipo é obrigatório")
        TipoAgendamento tipo,

        StatusAgendamento status,

        @Size(max = 500)
        String observacoes,

        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        @NotNull(message = "Clínica é obrigatória")
        UUID clinicaId
) {
}
