package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoResponse extends RepresentationModel<AgendamentoResponse> {

    private final UUID id;
    private final LocalDateTime dataHora;
    private final TipoAgendamento tipo;
    private final StatusAgendamento status;
    private final String observacoes;
    private final UUID petId;
    private final String petNome;
    private final UUID clinicaId;
    private final String clinicaNome;

    public AgendamentoResponse(UUID id, LocalDateTime dataHora, TipoAgendamento tipo, StatusAgendamento status,
                               String observacoes, UUID petId, String petNome, UUID clinicaId, String clinicaNome) {
        this.id = id;
        this.dataHora = dataHora;
        this.tipo = tipo;
        this.status = status;
        this.observacoes = observacoes;
        this.petId = petId;
        this.petNome = petNome;
        this.clinicaId = clinicaId;
        this.clinicaNome = clinicaNome;
    }

    public static AgendamentoResponse from(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(),
                a.getDataHora(),
                a.getTipo(),
                a.getStatus(),
                a.getObservacoes(),
                a.getPet() != null ? a.getPet().getId() : null,
                a.getPet() != null ? a.getPet().getNome() : null,
                a.getClinica() != null ? a.getClinica().getId() : null,
                a.getClinica() != null ? a.getClinica().getNome() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public TipoAgendamento getTipo() {
        return tipo;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getPetNome() {
        return petNome;
    }

    public UUID getClinicaId() {
        return clinicaId;
    }

    public String getClinicaNome() {
        return clinicaNome;
    }
}
