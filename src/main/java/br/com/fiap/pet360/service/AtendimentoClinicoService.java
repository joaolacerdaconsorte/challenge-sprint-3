package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.AtendimentoClinicoForm;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.StatusConsulta;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class AtendimentoClinicoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final VacinaRepository vacinaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaRepository alertaRepository;

    public AtendimentoClinicoService(AgendamentoRepository agendamentoRepository,
                                   ConsultaRepository consultaRepository,
                                   PetRepository petRepository,
                                   VacinaRepository vacinaRepository,
                                   MedicamentoRepository medicamentoRepository,
                                   AlertaRepository alertaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.consultaRepository = consultaRepository;
        this.petRepository = petRepository;
        this.vacinaRepository = vacinaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaRepository = alertaRepository;
    }

    @CacheEvict(value = {"agendamentos", "consultas", "pets", "vacinas", "medicamentos", "alertas"}, allEntries = true)
    public Consulta realizarAtendimento(AtendimentoClinicoForm form) {
        Agendamento agendamento = agendamentoRepository.findById(form.getAgendamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", form.getAgendamentoId()));

        if (agendamento.getStatus() == StatusAgendamento.REALIZADO) {
            throw new BusinessException("Este agendamento já foi realizado anteriormente.");
        }

        Pet pet = agendamento.getPet();

        pet.setPeso(form.getPesoAtual());
        petRepository.save(pet);

        Consulta consulta = new Consulta(
                LocalDateTime.now(),
                StatusConsulta.REALIZADA,
                form.getMotivo(),
                form.getValor(),
                pet,
                agendamento.getClinica()
        );
        consulta.setDiagnostico(form.getDiagnostico());
        consulta = consultaRepository.save(consulta);

        if (form.isAplicarVacina() && form.getNomeVacina() != null && !form.getNomeVacina().isBlank()) {
            int diasProxima = form.getDiasProximaDose() != null ? form.getDiasProximaDose() : 365;
            LocalDate dataProxima = LocalDate.now().plusDays(diasProxima);

            Vacina vacina = new Vacina(form.getNomeVacina(), LocalDate.now(), dataProxima, pet);
            vacina.setLote(form.getLoteVacina());
            vacina.setConsulta(consulta);
            vacinaRepository.save(vacina);

            String descAlertaVacina = "Vacina " + vacina.getNome() + " aplicada em " + pet.getNome() +
                    ". Próximo reforço previsto para " + dataProxima + ".";
            alertaRepository.save(new Alerta(TipoAlerta.VACINA, descAlertaVacina, LocalDateTime.now(), pet, pet.getTutor()));
        }

        if (form.isPrescreverMedicamento() && form.getNomeMedicamento() != null && !form.getNomeMedicamento().isBlank()) {
            int duracao = form.getDuracaoDias() != null ? form.getDuracaoDias() : 7;
            LocalDate dataFim = LocalDate.now().plusDays(duracao);

            Medicamento med = new Medicamento(
                    form.getNomeMedicamento(),
                    form.getDosagem(),
                    form.getFrequencia(),
                    LocalDate.now(),
                    dataFim,
                    form.getCustoMedicamento(),
                    pet
            );
            med.setConsulta(consulta);
            med.setAtivo(true);
            medicamentoRepository.save(med);

            String descAlertaMed = "Prescrição médica para " + pet.getNome() + ": " + med.getNome() +
                    " (" + med.getDosagem() + ", " + med.getFrequencia() + ") por " + duracao + " dias.";
            alertaRepository.save(new Alerta(TipoAlerta.MEDICAMENTO, descAlertaMed, LocalDateTime.now(), pet, pet.getTutor()));
        }

        agendamento.setStatus(StatusAgendamento.REALIZADO);
        agendamentoRepository.save(agendamento);

        return consulta;
    }
}
