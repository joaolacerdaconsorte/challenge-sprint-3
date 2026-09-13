package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.AgendamentoRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
import br.com.fiap.pet360.repository.AgendamentoRepository;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              PetRepository petRepository,
                              ClinicaRepository clinicaRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
    }

    @Cacheable(value = "agendamentos", key = "T(java.util.Objects).hash(#petId, #clinicaId, #status, #tipo, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Agendamento> listar(UUID petId, UUID clinicaId, StatusAgendamento status,
                                    TipoAgendamento tipo, Pageable pageable) {
        Page<Agendamento> page = agendamentoRepository.search(petId, clinicaId, status, tipo, pageable);
        page.forEach(this::touch);
        return page;
    }

    @Cacheable(value = "agendamentos", key = "#id")
    @Transactional(readOnly = true)
    public Agendamento buscarPorId(UUID id) {
        Agendamento a = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento", id));
        touch(a);
        return a;
    }

    @Transactional(readOnly = true)
    public List<Agendamento> pendentes(UUID clinicaId) {
        List<Agendamento> list = agendamentoRepository.findPendentes(LocalDateTime.now(), clinicaId);
        list.forEach(this::touch);
        return list;
    }

    private void touch(Agendamento a) {
        if (a.getPet() != null) a.getPet().getNome();
        if (a.getClinica() != null) a.getClinica().getNome();
    }

    @CacheEvict(value = "agendamentos", allEntries = true)
    public Agendamento criar(AgendamentoRequest req) {
        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
        Clinica clinica = clinicaRepository.findById(req.clinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinica", req.clinicaId()));
        StatusAgendamento status = req.status() == null ? StatusAgendamento.PENDENTE : req.status();
        Agendamento a = new Agendamento(req.dataHora(), req.tipo(), status, pet, clinica);
        a.setObservacoes(req.observacoes());
        return agendamentoRepository.save(a);
    }

    @CacheEvict(value = "agendamentos", allEntries = true)
    public Agendamento atualizar(UUID id, AgendamentoRequest req) {
        Agendamento a = buscarPorId(id);
        if (!a.getPet().getId().equals(req.petId())) {
            Pet pet = petRepository.findById(req.petId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
            a.setPet(pet);
        }
        if (!a.getClinica().getId().equals(req.clinicaId())) {
            Clinica clinica = clinicaRepository.findById(req.clinicaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinica", req.clinicaId()));
            a.setClinica(clinica);
        }
        a.setDataHora(req.dataHora());
        a.setTipo(req.tipo());
        a.setStatus(req.status() == null ? a.getStatus() : req.status());
        a.setObservacoes(req.observacoes());
        return agendamentoRepository.save(a);
    }

    @CacheEvict(value = "agendamentos", allEntries = true)
    public Agendamento confirmar(UUID id) {
        Agendamento a = buscarPorId(id);
        a.setStatus(StatusAgendamento.CONFIRMADO);
        return agendamentoRepository.save(a);
    }

    @CacheEvict(value = "agendamentos", allEntries = true)
    public Agendamento cancelar(UUID id) {
        Agendamento a = buscarPorId(id);
        a.setStatus(StatusAgendamento.CANCELADO);
        return agendamentoRepository.save(a);
    }

    @CacheEvict(value = "agendamentos", allEntries = true)
    public void remover(UUID id) {
        Agendamento a = buscarPorId(id);
        agendamentoRepository.delete(a);
    }
}
