package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.StatusConsulta;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ConsultaResponse extends RepresentationModel<ConsultaResponse> {

    private final UUID id;
    private final LocalDateTime dataHora;
    private final StatusConsulta status;
    private final String motivo;
    private final String diagnostico;
    private final BigDecimal valor;
    private final UUID petId;
    private final String petNome;
    private final UUID clinicaId;
    private final String clinicaNome;

    public ConsultaResponse(UUID id, LocalDateTime dataHora, StatusConsulta status, String motivo, String diagnostico,
                            BigDecimal valor, UUID petId, String petNome, UUID clinicaId, String clinicaNome) {
        this.id = id;
        this.dataHora = dataHora;
        this.status = status;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.valor = valor;
        this.petId = petId;
        this.petNome = petNome;
        this.clinicaId = clinicaId;
        this.clinicaNome = clinicaNome;
    }

    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(
                c.getId(),
                c.getDataHora(),
                c.getStatus(),
                c.getMotivo(),
                c.getDiagnostico(),
                c.getValor(),
                c.getPet() != null ? c.getPet().getId() : null,
                c.getPet() != null ? c.getPet().getNome() : null,
                c.getClinica() != null ? c.getClinica().getId() : null,
                c.getClinica() != null ? c.getClinica().getNome() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public StatusConsulta getStatus() {
        return status;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public BigDecimal getValor() {
        return valor;
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
