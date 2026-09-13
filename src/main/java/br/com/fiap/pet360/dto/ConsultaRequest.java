package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.StatusConsulta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaRequest(

        @NotNull(message = "Data/hora é obrigatória")
        LocalDateTime dataHora,

        @NotNull(message = "Status é obrigatório")
        StatusConsulta status,

        @Size(max = 500)
        String motivo,

        @Size(max = 500)
        String diagnostico,

        @DecimalMin(value = "0.0", message = "Valor não pode ser negativo")
        BigDecimal valor,

        @NotNull(message = "Pet é obrigatório")
        UUID petId,

        @NotNull(message = "Clínica é obrigatória")
        UUID clinicaId
) {
}
