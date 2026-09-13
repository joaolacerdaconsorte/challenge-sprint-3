package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record VacinaRequest(

        @NotBlank(message = "Nome da vacina é obrigatório")
        @Size(max = 100)
        String nome,

        @NotNull(message = "Data de aplicação é obrigatória")
        LocalDate dataAplicacao,

        LocalDate dataProxima,

        @Size(max = 50)
        String lote,

        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        UUID consultaId
) {
}
