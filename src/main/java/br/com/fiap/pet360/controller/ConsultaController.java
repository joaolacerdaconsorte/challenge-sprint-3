package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.ConsultaRequest;
import br.com.fiap.pet360.dto.ConsultaResponse;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.StatusConsulta;
import br.com.fiap.pet360.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/consultas", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Consultas", description = "Consultas veterinárias com filtros por clínica, pet, status e intervalo de datas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping
    @Operation(summary = "Lista consultas com filtros (clinicaId, petId, status, dataInicio, dataFim) e paginação")
    public ResponseEntity<Page<ConsultaResponse>> listar(
            @RequestParam(required = false) UUID clinicaId,
            @RequestParam(required = false) UUID petId,
            @RequestParam(required = false) StatusConsulta status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable) {
        Page<ConsultaResponse> page = consultaService
                .listar(clinicaId, petId, status, dataInicio, dataFim, pageable)
                .map(c -> addLinks(ConsultaResponse.from(c)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma consulta por ID")
    public ResponseEntity<ConsultaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(addLinks(ConsultaResponse.from(consultaService.buscarPorId(id))));
    }

    @PostMapping
    @Operation(summary = "Cria uma consulta")
    public ResponseEntity<ConsultaResponse> criar(@Valid @RequestBody ConsultaRequest req) {
        Consulta c = consultaService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(c.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(ConsultaResponse.from(c)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma consulta")
    public ResponseEntity<ConsultaResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ConsultaRequest req) {
        return ResponseEntity.ok(addLinks(ConsultaResponse.from(consultaService.atualizar(id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma consulta")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        consultaService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private ConsultaResponse addLinks(ConsultaResponse r) {
        r.add(linkTo(methodOn(ConsultaController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(ConsultaController.class).listar(null, null, null, null, null, null)).withRel("consultas"));
        if (r.getPetId() != null) r.add(linkTo(methodOn(PetController.class).buscar(r.getPetId())).withRel("pet"));
        if (r.getClinicaId() != null) r.add(linkTo(methodOn(ClinicaController.class).buscar(r.getClinicaId())).withRel("clinica"));
        return r;
    }
}
