package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Medicamento;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MedicamentoResponse extends RepresentationModel<MedicamentoResponse> {

    private final UUID id;
    private final String nome;
    private final String dosagem;
    private final String frequencia;
    private final LocalDate dataInicio;
    private final LocalDate dataFim;
    private final Boolean ativo;
    private final BigDecimal custo;
    private final UUID petId;
    private final String petNome;
    private final UUID consultaId;

    public MedicamentoResponse(UUID id, String nome, String dosagem, String frequencia, LocalDate dataInicio,
                               LocalDate dataFim, Boolean ativo, BigDecimal custo, UUID petId, String petNome, UUID consultaId) {
        this.id = id;
        this.nome = nome;
        this.dosagem = dosagem;
        this.frequencia = frequencia;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.ativo = ativo;
        this.custo = custo;
        this.petId = petId;
        this.petNome = petNome;
        this.consultaId = consultaId;
    }

    public static MedicamentoResponse from(Medicamento m) {
        return new MedicamentoResponse(
                m.getId(),
                m.getNome(),
                m.getDosagem(),
                m.getFrequencia(),
                m.getDataInicio(),
                m.getDataFim(),
                m.getAtivo(),
                m.getCusto(),
                m.getPet() != null ? m.getPet().getId() : null,
                m.getPet() != null ? m.getPet().getNome() : null,
                m.getConsulta() != null ? m.getConsulta().getId() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDosagem() {
        return dosagem;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public BigDecimal getCusto() {
        return custo;
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
}
