package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.ClinicaRequest;
import br.com.fiap.pet360.dto.ClinicaResponse;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.service.ClinicaService;
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
@RequestMapping(value = "/api/clinicas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Clinicas", description = "Clínicas e hospitais veterinários parceiros")
public class ClinicaController {

    private final ClinicaService clinicaService;

    public ClinicaController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    @GetMapping
    @Operation(summary = "Lista clínicas paginadas")
    public ResponseEntity<Page<ClinicaResponse>> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<ClinicaResponse> page = clinicaService.listar(nome, pageable).map(c -> addLinks(ClinicaResponse.from(c)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma clínica por ID")
    @ApiResponse(responseCode = "404", description = "Clínica não encontrada")
    public ResponseEntity<ClinicaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(ClinicaResponse.from(clinicaService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Cria uma nova clínica")
    @ApiResponse(responseCode = "201", description = "Clínica criada")
    public ResponseEntity<ClinicaResponse> criar(@Valid @RequestBody ClinicaRequest req) {
        Clinica c = clinicaService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(c.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(ClinicaResponse.from(c)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma clínica")
    public ResponseEntity<ClinicaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ClinicaRequest req) {
        return ResponseEntity.ok(addLinks(ClinicaResponse.from(clinicaService.atualizar(id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma clínica")
    @ApiResponse(responseCode = "204", description = "Clínica removida")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        clinicaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private ClinicaResponse addLinks(ClinicaResponse r) {
        r.add(linkTo(methodOn(ClinicaController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(ClinicaController.class).listar(null, null)).withRel("clinicas"));
        return r;
    }
}
