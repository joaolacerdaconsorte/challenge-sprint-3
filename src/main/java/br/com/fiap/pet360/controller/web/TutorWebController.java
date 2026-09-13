package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.TutorRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.repository.TutorRepository;
import br.com.fiap.pet360.service.TutorService;
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

import java.util.UUID;

@Controller
@RequestMapping("/tutores")
public class TutorWebController {

    private final TutorService tutorService;
    private final TutorRepository tutorRepository;

    public TutorWebController(TutorService tutorService, TutorRepository tutorRepository) {
        this.tutorService = tutorService;
        this.tutorRepository = tutorRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "nome", required = false) String nome,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {
        Page<Tutor> tutores = tutorService.listar(nome, null, PageRequest.of(page, 10, Sort.by("nome").ascending()));
        model.addAttribute("tutores", tutores);
        model.addAttribute("nomeFiltro", nome);
        return "tutores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        return "tutores/formulario";
    }

    @PostMapping
    public String salvar(@RequestParam("nome") String nome,
                         @RequestParam("cpf") String cpf,
                         @RequestParam("email") String email,
                         @RequestParam(value = "telefone", required = false) String telefone,
                         @RequestParam(value = "endereco", required = false) String endereco,
                         RedirectAttributes redirectAttributes) {
        try {
            TutorRequest req = new TutorRequest(nome, cpf, email, telefone, endereco);
            tutorService.criar(req);
            redirectAttributes.addFlashAttribute("sucesso", "Tutor cadastrado com sucesso!");
            return "redirect:/tutores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar tutor: " + e.getMessage());
            return "redirect:/tutores/novo";
        }
    }

    @GetMapping("/{id}")
    public String detalhes(@PathVariable("id") UUID id, Model model) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", id));
        model.addAttribute("tutor", tutor);
        return "tutores/detalhes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            tutorService.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Tutor e registros vinculados removidos com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível remover o tutor: " + e.getMessage());
        }
        return "redirect:/tutores";
    }
}
