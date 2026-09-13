package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.PetRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import br.com.fiap.pet360.service.PetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/pets")
public class PetWebController {

    private final PetService petService;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetWebController(PetService petService,
                            PetRepository petRepository,
                            TutorRepository tutorRepository) {
        this.petService = petService;
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "nome", required = false) String nome,
                         @RequestParam(value = "especie", required = false) Especie especie,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {
        Page<Pet> pets = petService.listar(nome, especie, null, PageRequest.of(page, 10, Sort.by("nome").ascending()));
        model.addAttribute("pets", pets);
        model.addAttribute("nomeFiltro", nome);
        model.addAttribute("especieFiltro", especie);
        model.addAttribute("especies", Especie.values());
        return "pets/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tutores", tutorRepository.findAll(Sort.by("nome").ascending()));
        model.addAttribute("especies", Especie.values());
        return "pets/formulario";
    }

    @PostMapping
    public String salvar(@RequestParam("nome") String nome,
                         @RequestParam("especie") Especie especie,
                         @RequestParam(value = "raca", required = false) String raca,
                         @RequestParam(value = "peso", required = false) BigDecimal peso,
                         @RequestParam(value = "dataNascimento", required = false) LocalDate dataNascimento,
                         @RequestParam("tutorId") UUID tutorId,
                         @RequestParam(value = "observacoes", required = false) String observacoes,
                         RedirectAttributes redirectAttributes) {
        try {
            PetRequest req = new PetRequest(nome, especie, raca, dataNascimento, peso, observacoes, tutorId);
            petService.criar(req);
            redirectAttributes.addFlashAttribute("sucesso", "Pet cadastrado com sucesso!");
            return "redirect:/pets";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar pet: " + e.getMessage());
            return "redirect:/pets/novo";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") UUID id, Model model) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
        model.addAttribute("pet", pet);
        model.addAttribute("historico", petService.historico(id));
        return "pets/detalhes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            petService.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Pet removido com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível remover o pet: " + e.getMessage());
        }
        return "redirect:/pets";
    }
}
