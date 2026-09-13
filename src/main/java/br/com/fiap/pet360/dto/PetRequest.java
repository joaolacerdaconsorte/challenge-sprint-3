package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Especie;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PetRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome,

        @NotNull(message = "Espécie é obrigatória")
        Especie especie,

        @Size(max = 100)
        String raca,

        @PastOrPresent(message = "Data de nascimento não pode ser futura")
        LocalDate dataNascimento,

        @DecimalMin(value = "0.0", inclusive = false, message = "Peso deve ser positivo")
        BigDecimal peso,

        @Size(max = 500)
        String observacoes,

        @NotNull(message = "Tutor é obrigatório")
        UUID tutorId
) {
}
