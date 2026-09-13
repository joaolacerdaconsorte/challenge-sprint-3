package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.ClinicaRequest;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.service.ClinicaService;
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
@RequestMapping("/clinicas")
public class ClinicaWebController {

    private final ClinicaService clinicaService;

    public ClinicaWebController(ClinicaService clinicaService) {
        this.clinicaService = clinicaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "nome", required = false) String nome,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {
        Page<Clinica> clinicas = clinicaService.listar(nome, PageRequest.of(page, 10, Sort.by("nome").ascending()));
        model.addAttribute("clinicas", clinicas);
        model.addAttribute("nomeFiltro", nome);
        return "clinicas/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        return "clinicas/formulario";
    }

    @PostMapping
    public String salvar(@RequestParam("nome") String nome,
                         @RequestParam("cnpj") String cnpj,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "telefone", required = false) String telefone,
                         @RequestParam(value = "endereco", required = false) String endereco,
                         RedirectAttributes redirectAttributes) {
        try {
            ClinicaRequest req = new ClinicaRequest(nome, cnpj, email, telefone, endereco);
            clinicaService.criar(req);
            redirectAttributes.addFlashAttribute("sucesso", "Clínica cadastrada com sucesso!");
            return "redirect:/clinicas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar clínica: " + e.getMessage());
            return "redirect:/clinicas/nova";
        }
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            clinicaService.remover(id);
            redirectAttributes.addFlashAttribute("sucesso", "Clínica removida com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível remover a clínica: " + e.getMessage());
        }
        return "redirect:/clinicas";
    }
}
