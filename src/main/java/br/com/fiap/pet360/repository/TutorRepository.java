package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, UUID> {

    Optional<Tutor> findByCpf(String cpf);

    Optional<Tutor> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    @Query("""
            SELECT t FROM Tutor t
            WHERE (:nome IS NULL OR LOWER(t.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:email IS NULL OR LOWER(t.email) LIKE LOWER(CONCAT('%', :email, '%')))
            """)
    Page<Tutor> search(@Param("nome") String nome, @Param("email") String email, Pageable pageable);
}
