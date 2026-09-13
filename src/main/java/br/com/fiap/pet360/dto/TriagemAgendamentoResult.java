package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Agendamento;

public record TriagemAgendamentoResult(
        Agendamento agendamento,
        boolean alertaCriado,
        String alertaDescricao,
        int vacinasPendentes,
        int medicamentosAtivos,
        String mensagemStatus
) {}
