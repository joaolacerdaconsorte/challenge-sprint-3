package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.ClinicaRequest;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Clinica;
import br.com.fiap.pet360.repository.ClinicaRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClinicaService {

    private final ClinicaRepository clinicaRepository;

    public ClinicaService(ClinicaRepository clinicaRepository) {
        this.clinicaRepository = clinicaRepository;
    }

    @Cacheable(value = "clinicas", key = "T(java.util.Objects).hash(#nome, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Clinica> listar(String nome, Pageable pageable) {
        return clinicaRepository.search(nome, pageable);
    }

    @Cacheable(value = "clinicas", key = "#id")
    @Transactional(readOnly = true)
    public Clinica buscarPorId(UUID id) {
        return clinicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Clinica", id));
    }

    @CacheEvict(value = "clinicas", allEntries = true)
    public Clinica criar(ClinicaRequest req) {
        if (clinicaRepository.existsByCnpj(req.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + req.cnpj());
        }
        Clinica c = new Clinica(req.nome(), req.cnpj(), req.email(), req.telefone(), req.endereco());
        return clinicaRepository.save(c);
    }

    @CacheEvict(value = "clinicas", allEntries = true)
    public Clinica atualizar(UUID id, ClinicaRequest req) {
        Clinica c = buscarPorId(id);
        if (!c.getCnpj().equals(req.cnpj()) && clinicaRepository.existsByCnpj(req.cnpj())) {
            throw new BusinessException("CNPJ já cadastrado: " + req.cnpj());
        }
        c.setNome(req.nome());
        c.setCnpj(req.cnpj());
        c.setEmail(req.email());
        c.setTelefone(req.telefone());
        c.setEndereco(req.endereco());
        return clinicaRepository.save(c);
    }

    @CacheEvict(value = "clinicas", allEntries = true)
    public void remover(UUID id) {
        Clinica c = buscarPorId(id);
        clinicaRepository.delete(c);
    }
}
