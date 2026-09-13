package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.VacinaRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.Vacina;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.PetRepository;
import br.com.fiap.pet360.repository.VacinaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class VacinaService {

    private final VacinaRepository vacinaRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;

    public VacinaService(VacinaRepository vacinaRepository,
                         PetRepository petRepository,
                         ConsultaRepository consultaRepository) {
        this.vacinaRepository = vacinaRepository;
        this.petRepository = petRepository;
        this.consultaRepository = consultaRepository;
    }

    @Cacheable(value = "vacinas", key = "T(java.util.Objects).hash(#petId, #nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Vacina> listar(UUID petId, String nome, Pageable pageable) {
        Page<Vacina> page = vacinaRepository.search(petId, nome, pageable);
        page.forEach(this::touch);
        return page;
    }

    @Cacheable(value = "vacinas", key = "#id")
    @Transactional(readOnly = true)
    public Vacina buscarPorId(UUID id) {
        Vacina v = vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina", id));
        touch(v);
        return v;
    }

    @Transactional(readOnly = true)
    public List<Vacina> listarPorPet(UUID petId) {
        List<Vacina> list = vacinaRepository.findByPetIdOrderByDataAplicacaoDesc(petId);
        list.forEach(this::touch);
        return list;
    }

    @Transactional(readOnly = true)
    public List<Vacina> vencidasOuProximas(UUID petId, int diasJanela) {
        List<Vacina> list = vacinaRepository.findVencidasOuProximas(petId, LocalDate.now().plusDays(diasJanela));
        list.forEach(this::touch);
        return list;
    }

    private void touch(Vacina v) {
        if (v.getPet() != null) v.getPet().getNome();
        if (v.getConsulta() != null) v.getConsulta().getId();
    }

    @CacheEvict(value = "vacinas", allEntries = true)
    public Vacina criar(VacinaRequest req) {
        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
        Vacina v = new Vacina(req.nome(), req.dataAplicacao(), req.dataProxima(), pet);
        v.setLote(req.lote());
        if (req.consultaId() != null) {
            Consulta c = consultaRepository.findById(req.consultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta", req.consultaId()));
            v.setConsulta(c);
        }
        return vacinaRepository.save(v);
    }

    @CacheEvict(value = "vacinas", allEntries = true)
    public Vacina atualizar(UUID id, VacinaRequest req) {
        Vacina v = buscarPorId(id);
        if (!v.getPet().getId().equals(req.petId())) {
            Pet pet = petRepository.findById(req.petId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
            v.setPet(pet);
        }
        v.setNome(req.nome());
        v.setDataAplicacao(req.dataAplicacao());
        v.setDataProxima(req.dataProxima());
        v.setLote(req.lote());
        if (req.consultaId() == null) {
            v.setConsulta(null);
        } else if (v.getConsulta() == null || !v.getConsulta().getId().equals(req.consultaId())) {
            Consulta c = consultaRepository.findById(req.consultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta", req.consultaId()));
            v.setConsulta(c);
        }
        return vacinaRepository.save(v);
    }

    @CacheEvict(value = "vacinas", allEntries = true)
    public void remover(UUID id) {
        Vacina v = buscarPorId(id);
        vacinaRepository.delete(v);
    }
}
