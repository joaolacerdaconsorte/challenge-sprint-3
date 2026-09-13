package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.ConsultaRequest;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.Pet;
import br.com.fiap.pet360.model.StatusConsulta;
import br.com.fiap.pet360.repository.ClinicaRepository;
import br.com.fiap.pet360.repository.ConsultaRepository;
import br.com.fiap.pet360.repository.PetRepository;
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
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PetRepository petRepository;
    private final ClinicaRepository clinicaRepository;

    public ConsultaService(ConsultaRepository consultaRepository,
                           PetRepository petRepository,
                           ClinicaRepository clinicaRepository) {
        this.consultaRepository = consultaRepository;
        this.petRepository = petRepository;
        this.clinicaRepository = clinicaRepository;
    }

    @Cacheable(value = "consultas", key = "T(java.util.Objects).hash(#clinicaId, #petId, #status, #dataInicio, #dataFim, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Consulta> listar(UUID clinicaId, UUID petId, StatusConsulta status,
                                 LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable) {
        Page<Consulta> page = consultaRepository.search(clinicaId, petId, status, dataInicio, dataFim, pageable);
        page.forEach(this::touch);
        return page;
    }

    @Cacheable(value = "consultas", key = "#id")
    @Transactional(readOnly = true)
    public Consulta buscarPorId(UUID id) {
        Consulta c = consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));
        touch(c);
        return c;
    }

    private void touch(Consulta c) {
        if (c.getPet() != null) c.getPet().getNome();
        if (c.getClinica() != null) c.getClinica().getNome();
    }

    @CacheEvict(value = "consultas", allEntries = true)
    public Consulta criar(ConsultaRequest req) {
        Pet pet = petRepository.findById(req.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
        Clinica clinica = clinicaRepository.findById(req.clinicaId())
                .orElseThrow(() -> new ResourceNotFoundException("Clinica", req.clinicaId()));
        Consulta c = new Consulta(req.dataHora(), req.status(), req.motivo(), req.valor(), pet, clinica);
        c.setDiagnostico(req.diagnostico());
        return consultaRepository.save(c);
    }

    @CacheEvict(value = "consultas", allEntries = true)
    public Consulta atualizar(UUID id, ConsultaRequest req) {
        Consulta c = buscarPorId(id);
        if (!c.getPet().getId().equals(req.petId())) {
            Pet pet = petRepository.findById(req.petId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet", req.petId()));
            c.setPet(pet);
        }
        if (!c.getClinica().getId().equals(req.clinicaId())) {
            Clinica clinica = clinicaRepository.findById(req.clinicaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Clinica", req.clinicaId()));
            c.setClinica(clinica);
        }
        c.setDataHora(req.dataHora());
        c.setStatus(req.status());
        c.setMotivo(req.motivo());
        c.setDiagnostico(req.diagnostico());
        c.setValor(req.valor());
        return consultaRepository.save(c);
    }

    @CacheEvict(value = "consultas", allEntries = true)
    public void remover(UUID id) {
        Consulta c = buscarPorId(id);
        consultaRepository.delete(c);
    }
}
