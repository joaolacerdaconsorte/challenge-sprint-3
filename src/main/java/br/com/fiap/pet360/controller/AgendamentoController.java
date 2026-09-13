package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.AgendamentoRequest;
import br.com.fiap.pet360.dto.AgendamentoResponse;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.service.AgendamentoService;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/agendamentos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Agendamentos", description = "Agendamentos futuros (consulta, vacina, exame, cirurgia, etc)")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    @Operation(summary = "Lista agendamentos com filtros (petId, clinicaId, status, tipo)")
    public ResponseEntity<Page<AgendamentoResponse>> listar(
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) UUID clinicaId,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) TipoAgendamento tipo,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable) {
        Page<AgendamentoResponse> page = agendamentoService.listar(petId, clinicaId, status, tipo, pageable)
                .map(a -> addLinks(AgendamentoResponse.from(a)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Lista agendamentos PENDENTE futuros, opcionalmente por clínica")
    public ResponseEntity<List<AgendamentoResponse>> pendentes(@RequestParam(required = false) UUID clinicaId) {
        List<AgendamentoResponse> list = agendamentoService.pendentes(clinicaId)
                .stream().map(a -> addLinks(AgendamentoResponse.from(a))).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um agendamento por ID")
    public ResponseEntity<AgendamentoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(AgendamentoResponse.from(agendamentoService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Cria um agendamento")
    public ResponseEntity<AgendamentoResponse> criar(@Valid @RequestBody AgendamentoRequest req) {
        Agendamento a = agendamentoService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(a.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(AgendamentoResponse.from(a)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um agendamento")
    public ResponseEntity<AgendamentoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody AgendamentoRequest req) {
        return ResponseEntity.ok(addLinks(AgendamentoResponse.from(agendamentoService.atualizar(id, req))));
    }

    @PatchMapping("/{id}/confirmar")
    @Operation(summary = "Confirma um agendamento PENDENTE")
    public ResponseEntity<AgendamentoResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(AgendamentoResponse.from(agendamentoService.confirmar(id))));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela um agendamento")
    public ResponseEntity<AgendamentoResponse> cancelar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(AgendamentoResponse.from(agendamentoService.cancelar(id))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um agendamento")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        agendamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private AgendamentoResponse addLinks(AgendamentoResponse r) {
        r.add(linkTo(methodOn(AgendamentoController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(AgendamentoController.class).listar(null, null, null, null, null)).withRel("agendamentos"));
        if (r.getPetId() != null) r.add(linkTo(methodOn(PetController.class).buscar(r.getPetId())).withRel("pet"));
        if (r.getClinicaId() != null) r.add(linkTo(methodOn(ClinicaController.class).buscar(r.getClinicaId())).withRel("clinica"));
        return r;
    }
}
