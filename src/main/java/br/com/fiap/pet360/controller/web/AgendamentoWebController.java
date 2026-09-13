package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.TriagemAgendamentoRequest;
import br.com.fiap.pet360.dto.TriagemAgendamentoResult;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.service.AgendamentoService;
import br.com.fiap.pet360.service.TriagemAgendamentoService;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoWebController {

    private final AgendamentoService agendamentoService;
    private final TriagemAgendamentoService triagemAgendamentoService;
    private final AgendamentoRepository agendamentoRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;

    public AgendamentoWebController(AgendamentoService agendamentoService,
                                   TriagemAgendamentoService triagemAgendamentoService,
                                   AgendamentoRepository agendamentoRepository,
                                   PetRepository petRepository,
                                   ClinicaRepository clinicaRepository) {
        this.agendamentoService = agendamentoService;
        this.triagemAgendamentoService = triagemAgendamentoService;
        this.agendamentoRepository = agendamentoRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "status", required = false) StatusAgendamento status,
                         @RequestParam(value = "tipo", required = false) TipoAgendamento tipo,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         Model model) {
        Page<Agendamento> agendamentos = agendamentoService.listar(
                null, null, status, tipo, PageRequest.of(page, 10, Sort.by("dataHora").descending())
        );
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("statusFiltro", status);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("todosStatus", StatusAgendamento.values());
        model.addAttribute("todosTipos", TipoAgendamento.values());
        return "agendamentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("pets", petRepository.findAll(Sort.by("nome").ascending()));
        model.addAttribute("clinicas", clinicaRepository.findAll(Sort.by("nome").ascending()));
        model.addAttribute("tipos", TipoAgendamento.values());
        return "agendamentos/novo";
    }

    @PostMapping("/triagem")
    public String processarTriagem(@RequestParam("petId") UUID petId,
                                   @RequestParam("clinicaId") UUID clinicaId,
                                   @RequestParam("dataHora") String dataHoraStr,
                                   @RequestParam("tipo") TipoAgendamento tipo,
                                   @RequestParam(value = "observacoes", required = false) String observacoes,
                                   RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime dataHora = LocalDateTime.parse(dataHoraStr);
            TriagemAgendamentoRequest req = new TriagemAgendamentoRequest(petId, clinicaId, dataHora, tipo, observacoes);
            TriagemAgendamentoResult resultado = triagemAgendamentoService.processarAgendamentoComTriagem(req);

            redirectAttributes.addFlashAttribute("resultado", resultado);
            redirectAttributes.addFlashAttribute("sucesso", resultado.mensagemStatus());
            return "redirect:/agendamentos/" + resultado.agendamento().getId() + "/comprovante";
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/agendamentos/novo";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar agendamento: " + e.getMessage());
            return "redirect:/agendamentos/novo";
        }
    }

    @GetMapping("/{id}/comprovante")
    public String comprovante(@PathVariable("id") UUID id, Model model) {
        Agendamento a = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        model.addAttribute("agendamento", a);
        return "agendamentos/comprovante";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Agendamento cancelado com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Não foi possível cancelar: " + e.getMessage());
        }
        return "redirect:/agendamentos";
    }
}
