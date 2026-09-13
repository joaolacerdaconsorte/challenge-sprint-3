package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.AlertaRequest;
import br.com.fiap.pet360.dto.AlertaResponse;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.service.AlertaService;
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
@RequestMapping(value = "/api/alertas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Alertas", description = "Alertas direcionados ao tutor (vacina, medicamento, agendamento, bem-estar)")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(summary = "Lista alertas com filtros (tutorId, petId, lido, tipo)")
    public ResponseEntity<Page<AlertaResponse>> listar(
            @RequestParam(required = false) UUID tutorId,
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) Boolean lido,
            @RequestParam(required = false) TipoAlerta tipo,
            @PageableDefault(size = 20, sort = "dataAlerta") Pageable pageable) {
        Page<AlertaResponse> page = alertaService.listar(tutorId, petId, lido, tipo, pageable)
                .map(a -> addLinks(AlertaResponse.from(a)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um alerta por ID")
    public ResponseEntity<AlertaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(AlertaResponse.from(alertaService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Cria um alerta para o tutor")
    public ResponseEntity<AlertaResponse> criar(@Valid @RequestBody AlertaRequest req) {
        Alerta a = alertaService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(a.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(AlertaResponse.from(a)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um alerta")
    public ResponseEntity<AlertaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AlertaRequest req) {
        return ResponseEntity.ok(addLinks(AlertaResponse.from(alertaService.atualizar(id, req))));
    }

    @PatchMapping("/{id}/marcar-lido")
    @Operation(summary = "Marca um alerta como lido")
    public ResponseEntity<AlertaResponse> marcarLido(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(AlertaResponse.from(alertaService.marcarComoLido(id))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um alerta")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        alertaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private AlertaResponse addLinks(AlertaResponse r) {
        r.add(linkTo(methodOn(AlertaController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(AlertaController.class).listar(null, null, null, null, null)).withRel("alertas"));
        if (r.getPetId() != null) r.add(linkTo(methodOn(PetController.class).buscar(r.getPetId())).withRel("pet"));
        if (r.getTutorId() != null) r.add(linkTo(methodOn(TutorController.class).buscar(r.getTutorId())).withRel("tutor"));
        return r;
    }
}
