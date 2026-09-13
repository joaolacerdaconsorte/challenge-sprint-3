package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.MedicamentoRequest;
import br.com.fiap.pet360.dto.MedicamentoResponse;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.service.MedicamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping(value = "/api/medicamentos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Medicamentos", description = "Tratamentos medicamentosos com controle de adesão (ativo/inativo)")
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    public MedicamentoController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @GetMapping
    @Operation(summary = "Lista medicamentos com filtros (petId, ativo, nome)")
    public ResponseEntity<Page<MedicamentoResponse>> listar(
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "dataInicio") Pageable pageable) {
        Page<MedicamentoResponse> page = medicamentoService.listar(petId, ativo, nome, pageable)
                .map(m -> addLinks(MedicamentoResponse.from(m)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um medicamento por ID")
    public ResponseEntity<MedicamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(MedicamentoResponse.from(medicamentoService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Cria um medicamento (tratamento)")
    public ResponseEntity<MedicamentoResponse> criar(@Valid @RequestBody MedicamentoRequest req) {
        Medicamento m = medicamentoService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(m.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(MedicamentoResponse.from(m)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um medicamento")
    public ResponseEntity<MedicamentoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody MedicamentoRequest req) {
        return ResponseEntity.ok(addLinks(MedicamentoResponse.from(medicamentoService.atualizar(id, req))));
    }

    @PatchMapping("/{id}/finalizar")
    @Operation(summary = "Finaliza um tratamento (marca ativo=false)")
    public ResponseEntity<MedicamentoResponse> finalizar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(MedicamentoResponse.from(medicamentoService.finalizarTratamento(id))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um medicamento")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        medicamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private MedicamentoResponse addLinks(MedicamentoResponse r) {
        r.add(linkTo(methodOn(MedicamentoController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(MedicamentoController.class).listar(null, null, null, null)).withRel("medicamentos"));
        if (r.getPetId() != null) r.add(linkTo(methodOn(PetController.class).buscar(r.getPetId())).withRel("pet"));
        return r;
    }
}
