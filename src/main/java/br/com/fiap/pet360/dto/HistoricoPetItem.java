package br.com.fiap.pet360.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoPetItem(
        UUID id,
        String tipo,
        String descricao,
        LocalDateTime data
) {
}
