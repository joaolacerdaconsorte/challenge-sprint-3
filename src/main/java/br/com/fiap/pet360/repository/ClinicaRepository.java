package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Clinica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, UUID> {

    Optional<Clinica> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    @Query("""
            SELECT c FROM Clinica c
            WHERE (:nome IS NULL OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            """)
    Page<Clinica> search(@Param("nome") String nome, Pageable pageable);
}
