package br.com.fiap.pet360.dto;

import br.com.fiap.pet360.model.Clinica;
import org.springframework.hateoas.RepresentationModel;

import java.util.UUID;

public class ClinicaResponse extends RepresentationModel<ClinicaResponse> {

    private final UUID id;
    private final String nome;
    private final String cnpj;
    private final String email;
    private final String telefone;
    private final String endereco;

    public ClinicaResponse(UUID id, String nome, String cnpj, String email, String telefone, String endereco) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public static ClinicaResponse from(Clinica c) {
        return new ClinicaResponse(c.getId(), c.getNome(), c.getCnpj(), c.getEmail(), c.getTelefone(), c.getEndereco());
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
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
}
