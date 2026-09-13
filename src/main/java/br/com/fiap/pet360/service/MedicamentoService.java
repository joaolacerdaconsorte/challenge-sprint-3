package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.MedicamentoRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Medicamento;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.MedicamentoRepository;
import br.com.fiap.pet360.repository.PetRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final PetRepository petRepository;
    private final ConsultaRepository consultaRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                              PetRepository petRepository,
                              ConsultaRepository consultaRepository) {
        this.medicamentoRepository = medicamentoRepository;
        this.petRepository = petRepository;
        this.consultaRepository = consultaRepository;
    }

    @Cacheable(value = "medicamentos", key = "T(java.util.Objects).hash(#petId, #ativo, #nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Medicamento> listar(UUID petId, Boolean ativo, String nome, Pageable pageable) {
        Page<Medicamento> page = medicamentoRepository.search(petId, ativo, nome, pageable);
        page.forEach(this::touch);
        return page;
    }

    @Cacheable(value = "medicamentos", key = "#id")
    @Transactional(readOnly = true)
    public Medicamento buscarPorId(UUID id) {
        Medicamento m = medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
        touch(m);
        return m;
    }

    @Transactional(readOnly = true)
    public List<Medicamento> ativosDoPet(UUID petId) {
        List<Medicamento> list = medicamentoRepository.findByPetIdAndAtivoTrueOrderByDataInicioDesc(petId);
        list.forEach(this::touch);
        return list;
    }

    private void touch(Medicamento m) {
        if (m.getPet() != null) m.getPet().getNome();
        if (m.getConsulta() != null) m.getConsulta().getId();
    }

    @CacheEvict(value = "medicamentos", allEntries = true)
    public Medicamento criar(MedicamentoRequest req) {
        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
        Medicamento m = new Medicamento(req.nome(), req.dosagem(), req.frequencia(),
                req.dataInicio(), req.dataFim(), req.custo(), pet);
        m.setAtivo(req.ativo() == null ? Boolean.TRUE : req.ativo());
        if (req.consultaId() != null) {
            Consulta c = consultaRepository.findById(req.consultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta", req.consultaId()));
            m.setConsulta(c);
        }
        return medicamentoRepository.save(m);
    }

    @CacheEvict(value = "medicamentos", allEntries = true)
    public Medicamento atualizar(UUID id, MedicamentoRequest req) {
        Medicamento m = buscarPorId(id);
        if (!m.getPet().getId().equals(req.petId())) {
            Pet pet = petRepository.findById(req.petId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
            m.setPet(pet);
        }
        m.setNome(req.nome());
        m.setDosagem(req.dosagem());
        m.setFrequencia(req.frequencia());
        m.setDataInicio(req.dataInicio());
        m.setDataFim(req.dataFim());
        m.setAtivo(req.ativo() == null ? m.getAtivo() : req.ativo());
        m.setCusto(req.custo());
        if (req.consultaId() == null) {
            m.setConsulta(null);
        } else if (m.getConsulta() == null || !m.getConsulta().getId().equals(req.consultaId())) {
            Consulta c = consultaRepository.findById(req.consultaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta", req.consultaId()));
            m.setConsulta(c);
        }
        return medicamentoRepository.save(m);
    }

    @CacheEvict(value = "medicamentos", allEntries = true)
    public Medicamento finalizarTratamento(UUID id) {
        Medicamento m = buscarPorId(id);
        m.setAtivo(false);
        return medicamentoRepository.save(m);
    }

    @CacheEvict(value = "medicamentos", allEntries = true)
    public void remover(UUID id) {
        Medicamento m = buscarPorId(id);
        medicamentoRepository.delete(m);
    }
}
