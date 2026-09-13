package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MedicamentoRequest(

        @NotBlank(message = "Nome do medicamento é obrigatório")
        @Size(max = 150)
        String nome,

        @Size(max = 100)
        String dosagem,

        @Size(max = 100)
        String frequencia,

        @NotNull(message = "Data de início é obrigatória")
        LocalDate dataInicio,

        LocalDate dataFim,

        Boolean ativo,

        @DecimalMin(value = "0.0", message = "Custo não pode ser negativo")
        BigDecimal custo,

        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        UUID consultaId
) {
}
