package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.HistoricoPetItem;
import br.com.fiap.pet360.dto.PetRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.TutorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;
    private final TutorRepository tutorRepository;

    public PetService(PetRepository petRepository, TutorRepository tutorRepository) {
        this.petRepository = petRepository;
        this.tutorRepository = tutorRepository;
    }

    @Cacheable(value = "pets", key = "T(java.util.Objects).hash(#nome, #especie, #tutorId, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Pet> listar(String nome, Especie especie, UUID tutorId, Pageable pageable) {
        Page<Pet> page = petRepository.search(nome, especie, tutorId, pageable);
        page.forEach(p -> { if (p.getTutor() != null) p.getTutor().getNome(); });
        return page;
    }

    @Cacheable(value = "pets", key = "#id")
    @Transactional(readOnly = true)
    public Pet buscarPorId(UUID id) {
        Pet p = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
        if (p.getTutor() != null) p.getTutor().getNome();
        return p;
    }

    @CacheEvict(value = "pets", allEntries = true)
    public Pet criar(PetRequest req) {
        Tutor tutor = tutorRepository.findById(req.tutorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", req.tutorId()));
        Pet p = new Pet(req.nome(), req.especie(), req.raca(), req.dataNascimento(), req.peso(), tutor);
        p.setObservacoes(req.observacoes());
        return petRepository.save(p);
    }

    @CacheEvict(value = "pets", allEntries = true)
    public Pet atualizar(UUID id, PetRequest req) {
        Pet p = buscarPorId(id);
        if (!p.getTutor().getId().equals(req.tutorId())) {
            Tutor t = tutorRepository.findById(req.tutorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tutor", req.tutorId()));
            p.setTutor(t);
        }
        p.setNome(req.nome());
        p.setEspecie(req.especie());
        p.setRaca(req.raca());
        p.setDataNascimento(req.dataNascimento());
        p.setPeso(req.peso());
        p.setObservacoes(req.observacoes());
        return petRepository.save(p);
    }

    @CacheEvict(value = "pets", allEntries = true)
    public void remover(UUID id) {
        Pet p = buscarPorId(id);
        petRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public List<HistoricoPetItem> historico(UUID petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
        List<HistoricoPetItem> itens = new ArrayList<>();

        pet.getConsultas().forEach(c -> itens.add(new HistoricoPetItem(
                c.getId(),
                "CONSULTA",
                "Consulta — " + (c.getMotivo() == null ? c.getStatus().name() : c.getMotivo()),
                c.getDataHora()
        )));

        pet.getVacinas().forEach(v -> itens.add(new HistoricoPetItem(
                v.getId(),
                "VACINA",
                "Vacina aplicada: " + v.getNome(),
                v.getDataAplicacao().atStartOfDay()
        )));

        pet.getMedicamentos().forEach(m -> itens.add(new HistoricoPetItem(
                m.getId(),
                "MEDICAMENTO",
                "Medicamento: " + m.getNome() + (Boolean.TRUE.equals(m.getAtivo()) ? " (ativo)" : ""),
                m.getDataInicio().atStartOfDay()
        )));

        pet.getAgendamentos().forEach(a -> itens.add(new HistoricoPetItem(
                a.getId(),
                "AGENDAMENTO",
                "Agendamento " + a.getTipo() + " — " + a.getStatus(),
                a.getDataHora()
        )));

        itens.sort(Comparator.comparing(HistoricoPetItem::data, Comparator.nullsLast(LocalDateTime::compareTo)).reversed());
        return itens;
    }
}
