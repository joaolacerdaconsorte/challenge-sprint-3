package br.com.fiap.pet360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(

        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "Password é obrigatório")
        @Size(min = 6, max = 100)
        String password
) {
}
