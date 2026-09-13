package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Pet;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class PetResponse extends RepresentationModel<PetResponse> {

    private final UUID id;
    private final String nome;
    private final Especie especie;
    private final String raca;
    private final LocalDate dataNascimento;
    private final BigDecimal peso;
    private final String observacoes;
    private final UUID tutorId;
    private final String tutorNome;

    public PetResponse(UUID id, String nome, Especie especie, String raca, LocalDate dataNascimento,
                       BigDecimal peso, String observacoes, UUID tutorId, String tutorNome) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.dataNascimento = dataNascimento;
        this.peso = peso;
        this.observacoes = observacoes;
        this.tutorId = tutorId;
        this.tutorNome = tutorNome;
    }

    public static PetResponse from(Pet p) {
        return new PetResponse(
                p.getId(),
                p.getNome(),
                p.getEspecie(),
                p.getRaca(),
                p.getDataNascimento(),
                p.getPeso(),
                p.getObservacoes(),
                p.getTutor() != null ? p.getTutor().getId() : null,
                p.getTutor() != null ? p.getTutor().getNome() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Especie getEspecie() {
        return especie;
    }

    public String getRaca() {
        return raca;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public BigDecimal getPeso() {
        return peso;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public String getTutorNome() {
        return tutorNome;
    }
}
