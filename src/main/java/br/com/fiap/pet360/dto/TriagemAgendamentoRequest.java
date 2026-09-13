package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.TipoAgendamento;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record TriagemAgendamentoRequest(
        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        @NotNull(message = "Clínica é obrigatória")
        UUID clinicaId,

        @NotNull(message = "Data e hora são obrigatórias")
        @Future(message = "O agendamento deve ser para uma data futura")
        LocalDateTime dataHora,

        @NotNull(message = "Tipo de atendimento é obrigatório")
        TipoAgendamento tipo,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        String observacoes
) {}
