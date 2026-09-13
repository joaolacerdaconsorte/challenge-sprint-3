package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.TriagemAgendamentoRequest;
import br.com.fiap.pet360.dto.TriagemAgendamentoResult;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TriagemAgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;
    private final VacinaRepository vacinaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AlertaRepository alertaRepository;

    public TriagemAgendamentoService(AgendamentoRepository agendamentoRepository,
                                   PetRepository petRepository,
                                   ClinicaRepository clinicaRepository,
                                   VacinaRepository vacinaRepository,
                                   MedicamentoRepository medicamentoRepository,
                                   AlertaRepository alertaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
        this.vacinaRepository = vacinaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.alertaRepository = alertaRepository;
    }

    @CacheEvict(value = {"agendamentos", "alertas"}, allEntries = true)
    public TriagemAgendamentoResult processarAgendamentoComTriagem(TriagemAgendamentoRequest req) {
        boolean conflito = agendamentoRepository.existsByClinicaIdAndDataHoraAndStatusNot(
                req.clinicaId(),
                req.dataHora(),
                StatusAgendamento.CANCELADO
        );

        if (conflito) {
            throw new BusinessException("A clínica selecionada já possui agendamento confirmado no mesmo horário. Por favor, escolha outro horário.");
        }

        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));

        Clinica clinica = clinicaRepository.findById(req.clinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinica", req.clinicaId()));

        List<Vacina> vacinasPendentes = vacinaRepository.findVencidasOuProximas(pet.getId(), LocalDate.now().plusDays(30));
        List<Medicamento> medicamentosAtivos = medicamentoRepository.findByPetIdAndAtivoTrueOrderByDataInicioDesc(pet.getId());

        Agendamento agendamento = new Agendamento(
                req.dataHora(),
                req.tipo(),
                StatusAgendamento.CONFIRMADO,
                pet,
                clinica
        );
        agendamento.setObservacoes(req.observacoes());
        agendamento = agendamentoRepository.save(agendamento);

        boolean alertaCriado = false;
        String descricaoAlerta = null;

        if (!vacinasPendentes.isEmpty()) {
            alertaCriado = true;
            descricaoAlerta = "Triagem Preventiva: " + pet.getNome() + " possui " + vacinasPendentes.size() +
                    " vacina(s) pendente(s) de atualização. Verifique durante a consulta na clínica " + clinica.getNome() + ".";

            Alerta alerta = new Alerta(TipoAlerta.VACINA, descricaoAlerta, LocalDateTime.now(), pet, pet.getTutor());
            alertaRepository.save(alerta);
        } else if (!medicamentosAtivos.isEmpty()) {
            alertaCriado = true;
            descricaoAlerta = "Triagem Preventiva: " + pet.getNome() + " está em tratamento contínuo com " +
                    medicamentosAtivos.size() + " medicamento(s). Leve as prescrições para a consulta na " + clinica.getNome() + ".";

            Alerta alerta = new Alerta(TipoAlerta.MEDICAMENTO, descricaoAlerta, LocalDateTime.now(), pet, pet.getTutor());
            alertaRepository.save(alerta);
        }

        String mensagemStatus = "Agendamento confirmado com sucesso para " + pet.getNome() + " na unidade " + clinica.getNome() + ".";

        return new TriagemAgendamentoResult(
                agendamento,
                alertaCriado,
                descricaoAlerta,
                vacinasPendentes.size(),
                medicamentosAtivos.size(),
                mensagemStatus
        );
    }
}
