package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record ClinicaRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "CNPJ é obrigatório")
        @CNPJ(message = "CNPJ inválido")
        String cnpj,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 20)
        String telefone,

        @Size(max = 200)
        String endereco
) {
}
