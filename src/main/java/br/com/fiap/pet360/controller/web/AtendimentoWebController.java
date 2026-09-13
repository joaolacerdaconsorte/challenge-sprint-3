package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.dto.AtendimentoClinicoForm;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.service.AtendimentoClinicoService;
import br.com.fiap.pet360.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/atendimento")
public class AtendimentoWebController {

    private final AgendamentoRepository agendamentoRepository;
    private final AtendimentoClinicoService atendimentoClinicoService;
    private final PetService petService;

    public AtendimentoWebController(AgendamentoRepository agendamentoRepository,
                                   AtendimentoClinicoService atendimentoClinicoService,
                                   PetService petService) {
        this.agendamentoRepository = agendamentoRepository;
        this.atendimentoClinicoService = atendimentoClinicoService;
        this.petService = petService;
    }

    @GetMapping("/fila")
    public String fila(Model model) {
        List<Agendamento> agendamentos = agendamentoRepository.findPendentes(
                LocalDateTime.now().minusDays(30), null
        );
        model.addAttribute("filaAtendimento", agendamentos);
        return "atendimento/fila";
    }

    @GetMapping("/{agendamentoId}/iniciar")
    public String iniciar(@PathVariable("agendamentoId") UUID agendamentoId, Model model) {
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", agendamentoId));

        AtendimentoClinicoForm form = new AtendimentoClinicoForm();
        form.setAgendamentoId(agendamento.getId());
        form.setPesoAtual(agendamento.getPet().getPeso());
        form.setMotivo(agendamento.getObservacoes() != null ? agendamento.getObservacoes() : "Atendimento Clínico");

        model.addAttribute("agendamento", agendamento);
        model.addAttribute("pet", agendamento.getPet());
        model.addAttribute("historico", petService.historico(agendamento.getPet().getId()));
        model.addAttribute("form", form);

        return "atendimento/executar";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("form") AtendimentoClinicoForm form,
                         RedirectAttributes redirectAttributes) {
        try {
            Consulta consulta = atendimentoClinicoService.realizarAtendimento(form);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Atendimento clínico concluído com sucesso! Prontuário de " + consulta.getPet().getNome() + " atualizado.");
            return "redirect:/pets/" + consulta.getPet().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar atendimento: " + e.getMessage());
            return "redirect:/atendimento/" + form.getAgendamentoId() + "/iniciar";
        }
    }
}
