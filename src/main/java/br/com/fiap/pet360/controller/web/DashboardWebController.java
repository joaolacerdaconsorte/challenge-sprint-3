package br.com.fiap.pet360.controller.web;

import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class DashboardWebController {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;
    private final ClinicaRepository clinicaRepository;
    private final ConsultaRepository consultaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final AlertaRepository alertaRepository;

    public DashboardWebController(PetRepository petRepository,
                                  TutorRepository tutorRepository,
                                  ClinicaRepository clinicaRepository,
                                  ConsultaRepository consultaRepository,
                                  AgendamentoRepository agendamentoRepository,
                                  AlertaRepository alertaRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
        this.clinicaRepository = clinicaRepository;
        this.consultaRepository = consultaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.alertaRepository = alertaRepository;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalPets", petRepository.count());
        model.addAttribute("totalTutores", tutorRepository.count());
        model.addAttribute("totalClinicas", clinicaRepository.count());
        model.addAttribute("totalConsultas", consultaRepository.count());

        List<Agendamento> pendentes = agendamentoRepository.findPendentes(LocalDateTime.now().minusHours(24), null);
        model.addAttribute("agendamentosPendentes", pendentes);

        List<Alerta> ultimosAlertas = alertaRepository.findAll(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "dataAlerta"))
        ).getContent();
        model.addAttribute("ultimosAlertas", ultimosAlertas);

        return "dashboard";
    }
}
