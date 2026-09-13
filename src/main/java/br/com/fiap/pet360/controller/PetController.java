package br.com.fiap.pet360.controller;

import br.com.fiap.pet360.dto.HistoricoPetItem;
import br.com.fiap.pet360.dto.MedicamentoResponse;
import br.com.fiap.pet360.dto.PetRequest;
import br.com.fiap.pet360.dto.PetResponse;
import br.com.fiap.pet360.dto.VacinaResponse;
import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.service.MedicamentoService;
import br.com.fiap.pet360.service.PetService;
import br.com.fiap.pet360.service.VacinaService;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/pets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pets", description = "Gestão de pets e operações de domínio (histórico, vacinas vencidas, medicamentos ativos)")
public class PetController {

    private final PetService petService;
    private final VacinaService vacinaService;
    private final MedicamentoService medicamentoService;

    public PetController(PetService petService, VacinaService vacinaService, MedicamentoService medicamentoService) {
        this.petService = petService;
        this.vacinaService = vacinaService;
        this.medicamentoService = medicamentoService;
    }

    @GetMapping
    @Operation(summary = "Lista pets com paginação, ordenação e filtros (nome, espécie, tutor)")
    public ResponseEntity<Page<PetResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Especie especie,
            @RequestParam(required = false) UUID tutorId,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<PetResponse> page = petService.listar(nome, especie, tutorId, pageable).map(p -> addLinks(PetResponse.from(p)));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um pet por ID")
    @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    public ResponseEntity<PetResponse> buscar(@PathVariable UUID id) {
        Pet p = petService.buscarPorId(id);
        return ResponseEntity.ok(addLinks(PetResponse.from(p)));
    }

    @PostMapping
    @Operation(summary = "Cria um novo pet vinculado a um tutor")
    @ApiResponse(responseCode = "201", description = "Pet criado")
    public ResponseEntity<PetResponse> criar(@Valid @RequestBody PetRequest req) {
        Pet p = petService.criar(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(p.getId()).toUri();
        return ResponseEntity.created(location).body(addLinks(PetResponse.from(p)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um pet")
    public ResponseEntity<PetResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody PetRequest req) {
        return ResponseEntity.ok(addLinks(PetResponse.from(petService.atualizar(id, req))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um pet")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        petService.remover(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Timeline agregada do pet (consultas + vacinas + medicamentos + agendamentos)")
    public ResponseEntity<List<HistoricoPetItem>> historico(@PathVariable UUID id) {
        return ResponseEntity.ok(petService.historico(id));
    }

    @GetMapping("/{id}/vacinas")
    @Operation(summary = "Vacinas do pet. Use vencidas=true para listar vencidas e próximas (janela em dias)")
    public ResponseEntity<List<VacinaResponse>> vacinasDoPet(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "false") boolean vencidas,
            @RequestParam(required = false, defaultValue = "30") int dias) {
        List<VacinaResponse> list = (vencidas
                ? vacinaService.vencidasOuProximas(id, dias)
                : vacinaService.listarPorPet(id))
                .stream().map(VacinaResponse::from).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}/medicamentos")
    @Operation(summary = "Medicamentos do pet. ativo=true filtra apenas tratamentos em curso")
    public ResponseEntity<List<MedicamentoResponse>> medicamentosDoPet(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "true") boolean ativo) {
        List<MedicamentoResponse> list = ativo
                ? medicamentoService.ativosDoPet(id).stream().map(MedicamentoResponse::from).toList()
                : medicamentoService.listar(id, null, null, Pageable.unpaged()).map(MedicamentoResponse::from).getContent();
        return ResponseEntity.ok(list);
    }

    private PetResponse addLinks(PetResponse r) {
        r.add(linkTo(methodOn(PetController.class).buscar(r.getId())).withSelfRel());
        r.add(linkTo(methodOn(PetController.class).listar(null, null, null, null)).withRel("pets"));
        r.add(linkTo(methodOn(PetController.class).historico(r.getId())).withRel("historico"));
        r.add(linkTo(methodOn(PetController.class).vacinasDoPet(r.getId(), false, 30)).withRel("vacinas"));
        r.add(linkTo(methodOn(PetController.class).medicamentosDoPet(r.getId(), true)).withRel("medicamentos"));
        if (r.getTutorId() != null) {
            r.add(linkTo(methodOn(TutorController.class).buscar(r.getTutorId())).withRel("tutor"));
        }
        return r;
    }
}
