package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.TipoAlerta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlertaRequest(

        @NotNull(message = "Tipo é obrigatório")
        TipoAlerta tipo,

        @Size(max = 500)
        String descricao,

        LocalDateTime dataAlerta,

        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        @NotNull(message = "Tutor é obrigatório")
        UUID tutorId
) {
}
