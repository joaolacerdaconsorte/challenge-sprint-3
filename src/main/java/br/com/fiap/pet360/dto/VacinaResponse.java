package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Vacina;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.UUID;

public class VacinaResponse extends RepresentationModel<VacinaResponse> {

    private final UUID id;
    private final String nome;
    private final LocalDate dataAplicacao;
    private final LocalDate dataProxima;
    private final String lote;
    private final UUID petId;
    private final String petNome;
    private final UUID consultaId;
    private final boolean vencida;

    public VacinaResponse(UUID id, String nome, LocalDate dataAplicacao, LocalDate dataProxima, String lote,
                          UUID petId, String petNome, UUID consultaId, boolean vencida) {
        this.id = id;
        this.nome = nome;
        this.dataAplicacao = dataAplicacao;
        this.dataProxima = dataProxima;
        this.lote = lote;
        this.petId = petId;
        this.petNome = petNome;
        this.consultaId = consultaId;
        this.vencida = vencida;
    }

    public static VacinaResponse from(Vacina v) {
        boolean venc = v.getDataProxima() != null && v.getDataProxima().isBefore(LocalDate.now());
        return new VacinaResponse(
                v.getId(),
                v.getNome(),
                v.getDataAplicacao(),
                v.getDataProxima(),
                v.getLote(),
                v.getPet() != null ? v.getPet().getId() : null,
                v.getPet() != null ? v.getPet().getNome() : null,
                v.getConsulta() != null ? v.getConsulta().getId() : null,
                venc
        );
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public LocalDate getDataAplicacao() {
        return dataAplicacao;
    }

    public LocalDate getDataProxima() {
        return dataProxima;
    }

    public String getLote() {
        return lote;
    }

    public UUID getPetId() {
        return petId;
    }

    public String getPetNome() {
        return petNome;
    }

    public UUID getConsultaId() {
        return consultaId;
    }

    public boolean isVencida() {
        return vencida;
    }
}
