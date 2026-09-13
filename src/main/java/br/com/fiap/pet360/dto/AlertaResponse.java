package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.TipoAlerta;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.UUID;

public class AlertaResponse extends RepresentationModel<AlertaResponse> {

    private final UUID id;
    private final TipoAlerta tipo;
    private final String descricao;
    private final LocalDateTime dataAlerta;
    private final Boolean lido;
    private final UUID petId;
    private final String petNome;
    private final UUID tutorId;
    private final String tutorNome;

    public AlertaResponse(UUID id, TipoAlerta tipo, String descricao, LocalDateTime dataAlerta, Boolean lido,
                          UUID petId, String petNome, UUID tutorId, String tutorNome) {
        this.id = id;
        this.tipo = tipo;
        this.descricao = descricao;
        this.dataAlerta = dataAlerta;
        this.lido = lido;
        this.petId = petId;
        this.petNome = petNome;
        this.tutorId = tutorId;
        this.tutorNome = tutorNome;
    }

    public static AlertaResponse from(Alerta a) {
        return new AlertaResponse(
                a.getId(),
                a.getTipo(),
                a.getDescricao(),
                a.getDataAlerta(),
                a.getLido(),
                a.getPet() != null ? a.getPet().getId() : null,
                a.getPet() != null ? a.getPet().getNome() : null,
                a.getTutor() != null ? a.getTutor().getId() : null,
                a.getTutor() != null ? a.getTutor().getNome() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public TipoAlerta getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getDataAlerta() {
        return dataAlerta;
    }

    public Boolean getLido() {
        return lido;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getPetNome() {
        return petNome;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public String getTutorNome() {
        return tutorNome;
    }
}
