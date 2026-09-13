package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.TutorRequest;
import br.com.fiap.pet360.dto.TutorResponse;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/tutores", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tutores", description = "Gestão de tutores (responsáveis pelos pets)")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    @Operation(summary = "Lista tutores com paginação, ordenação e filtros")
    @ApiResponse(responseCode = "200", description = "Lista paginada")
    public ResponseEntity<Page<TutorResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        Page<TutorResponse> page = tutorService.listar(nome, email, pageable)
                .map(t -> addLinks(TutorResponse.from(t)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um tutor por ID")
    @ApiResponse(responseCode = "200", description = "Tutor encontrado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public ResponseEntity<TutorResponse> buscar(@PathVariable UUID id) {
        Tutor t = tutorService.buscarPorId(id);
        return ResponseEntity.ok(addLinks(TutorResponse.from(t)));
    }

    @PostMapping
    @Operation(summary = "Cria um novo tutor")
    @ApiResponse(responseCode = "201", description = "Tutor criado")
    @ApiResponse(responseCode = "400", description = "Payload inválido ou CPF/email duplicado")
    public ResponseEntity<TutorResponse> criar(@Valid @RequestBody TutorRequest req) {
        Tutor t = tutorService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(t.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(TutorResponse.from(t)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um tutor existente")
    @ApiResponse(responseCode = "200", description = "Tutor atualizado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public ResponseEntity<TutorResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody TutorRequest req) {
        Tutor t = tutorService.atualizar(id, req);
        return ResponseEntity.ok(addLinks(TutorResponse.from(t)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um tutor")
    @ApiResponse(responseCode = "204", description = "Tutor removido")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        tutorService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private TutorResponse addLinks(TutorResponse r) {
        r.add(linkTo(methodOn(TutorController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(TutorController.class).listar(null, null, null)).withRel("tutores"));
        r.add(linkTo(methodOn(PetController.class).listar(null, null, r.getId(), null)).withRel("pets"));
        return r;
    }
}
