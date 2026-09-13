package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Tutor;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

public class TutorResponse extends RepresentationModel<TutorResponse> {

    private final UUID id;
    private final String nome;
    private final String cpf;
    private final String email;
    private final String telefone;
    private final String endereco;
    private final int quantidadePets;

    public TutorResponse(UUID id, String nome, String cpf, String email, String telefone, String endereco, int quantidadePets) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.quantidadePets = quantidadePets;
    }

    public static TutorResponse from(Tutor t) {
        return new TutorResponse(
                t.getId(),
                t.getNome(),
                t.getCpf(),
                t.getEmail(),
                t.getTelefone(),
                t.getEndereco(),
                t.getPets() == null ? 0 : t.getPets().size()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public int getQuantidadePets() {
        return quantidadePets;
    }
}
