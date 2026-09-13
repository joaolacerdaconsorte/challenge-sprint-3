package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.AlertaRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.TipoAlerta;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.repository.AlertaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         PetRepository petRepository,
                         TutorRepository tutorRepository) {
        this.alertaRepository = alertaRepository;
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @Cacheable(value = "alertas", key = "T(java.util.Objects).hash(#tutorId, #petId, #lido, #tipo, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Alerta> listar(UUID tutorId, UUID petId, Boolean lido, TipoAlerta tipo, Pageable pageable) {
        Page<Alerta> page = alertaRepository.search(tutorId, petId, lido, tipo, pageable);
        page.forEach(this::touch);
        return page;
    }

    @Cacheable(value = "alertas", key = "#id")
    @Transactional(readOnly = true)
    public Alerta buscarPorId(UUID id) {
        Alerta a = alertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", id));
        touch(a);
        return a;
    }

    private void touch(Alerta a) {
        if (a.getPet() != null) a.getPet().getNome();
        if (a.getTutor() != null) a.getTutor().getNome();
    }

    @CacheEvict(value = "alertas", allEntries = true)
    public Alerta criar(AlertaRequest req) {
        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
        Tutor tutor = tutorRepository.findById(req.tutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", req.tutorId()));
        LocalDateTime data = req.dataAlerta() == null ? LocalDateTime.now() : req.dataAlerta();
        Alerta a = new Alerta(req.tipo(), req.descricao(), data, pet, tutor);
        return alertaRepository.save(a);
    }

    @CacheEvict(value = "alertas", allEntries = true)
    public Alerta atualizar(UUID id, AlertaRequest req) {
        Alerta a = buscarPorId(id);
        a.setTipo(req.tipo());
        a.setDescricao(req.descricao());
        if (req.dataAlerta() != null) a.setDataAlerta(req.dataAlerta());
        return alertaRepository.save(a);
    }

    @CacheEvict(value = "alertas", allEntries = true)
    public Alerta marcarComoLido(UUID id) {
        Alerta a = buscarPorId(id);
        a.setLido(Boolean.TRUE);
        return alertaRepository.save(a);
    }

    @CacheEvict(value = "alertas", allEntries = true)
    public void remover(UUID id) {
        Alerta a = buscarPorId(id);
        alertaRepository.delete(a);
    }
}
