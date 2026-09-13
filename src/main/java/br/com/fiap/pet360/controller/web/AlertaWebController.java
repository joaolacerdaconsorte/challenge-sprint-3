package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.service.AlertaService;
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
@RequestMapping("/alertas")
public class AlertaWebController {

    private final AlertaService alertaService;

    public AlertaWebController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "lido", required = false) Boolean lido,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {
        Page<Alerta> alertas = alertaService.listar(
                null, null, lido, null, PageRequest.of(page, 10, Sort.by("dataAlerta").descending())
        );
        model.addAttribute("alertas", alertas);
        model.addAttribute("lidoFiltro", lido);
        return "alertas/lista";
    }

    @PostMapping("/{id}/marcar-lido")
    public String marcarLido(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            alertaService.marcarComoLido(id);
            redirectAttributes.addFlashAttribute("sucesso", "Alerta marcado como lido.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível atualizar alerta: " + e.getMessage());
        }
        return "redirect:/alertas";
    }
}
