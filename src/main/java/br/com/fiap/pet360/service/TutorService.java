package br.com.fiap.pet360.service;

import br.com.fiap.pet360.dto.TutorRequest;
import br.com.fiap.pet360.exception.BusinessException;
import br.com.fiap.pet360.exception.ResourceNotFoundException;
import br.com.fiap.pet360.model.Tutor;
import br.com.fiap.pet360.repository.TutorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    @Cacheable(value = "tutores", key = "T(java.util.Objects).hash(#nome, #email, #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString())")
    @Transactional(readOnly = true)
    public Page<Tutor> listar(String nome, String email, Pageable pageable) {
        Page<Tutor> page = tutorRepository.search(nome, email, pageable);
        page.forEach(t -> t.getPets().size());
        return page;
    }

    @Cacheable(value = "tutores", key = "#id")
    @Transactional(readOnly = true)
    public Tutor buscarPorId(UUID id) {
        Tutor t = tutorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor", id));
        t.getPets().size();
        return t;
    }

    @CacheEvict(value = "tutores", allEntries = true)
    public Tutor criar(TutorRequest req) {
        if (tutorRepository.existsByCpf(req.cpf())) {
            throw new BusinessException("CPF já cadastrado: " + req.cpf());
        }
        if (tutorRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email já cadastrado: " + req.email());
        }
        Tutor t = new Tutor(req.nome(), req.cpf(), req.email(), req.telefone(), req.endereco());
        return tutorRepository.save(t);
    }

    @CacheEvict(value = "tutores", allEntries = true)
    public Tutor atualizar(UUID id, TutorRequest req) {
        Tutor t = buscarPorId(id);
        if (!t.getCpf().equals(req.cpf()) && tutorRepository.existsByCpf(req.cpf())) {
            throw new BusinessException("CPF já cadastrado: " + req.cpf());
        }
        if (!t.getEmail().equals(req.email()) && tutorRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email já cadastrado: " + req.email());
        }
        t.setNome(req.nome());
        t.setCpf(req.cpf());
        t.setEmail(req.email());
        t.setTelefone(req.telefone());
        t.setEndereco(req.endereco());
        return tutorRepository.save(t);
    }

    @CacheEvict(value = "tutores", allEntries = true)
    public void remover(UUID id) {
        Tutor t = buscarPorId(id);
        tutorRepository.delete(t);
    }
}
