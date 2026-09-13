package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.VacinaRequest;
import br.com.fiap.pet360.dto.VacinaResponse;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.service.VacinaService;
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
@RequestMapping(value = "/api/vacinas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vacinas", description = "Vacinas aplicadas. Calcula automaticamente se está vencida.")
public class VacinaController {

    private final VacinaService vacinaService;

    public VacinaController(VacinaService vacinaService) {
        this.vacinaService = vacinaService;
    }

    @GetMapping
    @Operation(summary = "Lista vacinas com filtros (petId, nome)")
    public ResponseEntity<Page<VacinaResponse>> listar(
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "dataAplicacao") Pageable pageable) {
        Page<VacinaResponse> page = vacinaService.listar(petId, nome, pageable).map(v -> addLinks(VacinaResponse.from(v)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma vacina por ID")
    public ResponseEntity<VacinaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(VacinaResponse.from(vacinaService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Registra uma vacina aplicada")
    public ResponseEntity<VacinaResponse> criar(@Valid @RequestBody VacinaRequest req) {
        Vacina v = vacinaService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(v.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(VacinaResponse.from(v)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma vacina")
    public ResponseEntity<VacinaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody VacinaRequest req) {
        return ResponseEntity.ok(addLinks(VacinaResponse.from(vacinaService.atualizar(id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma vacina")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        vacinaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private VacinaResponse addLinks(VacinaResponse r) {
        r.add(linkTo(methodOn(VacinaController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(VacinaController.class).listar(null, null, null)).withRel("vacinas"));
        if (r.getPetId() != null) r.add(linkTo(methodOn(PetController.class).buscar(r.getPetId())).withRel("pet"));
        return r;
    }
}
