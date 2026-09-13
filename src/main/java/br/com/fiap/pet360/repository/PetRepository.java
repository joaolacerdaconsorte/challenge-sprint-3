package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Especie;
import br.com.fiap.pet360.model.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PetRepository extends JpaRepository<Pet, UUID> {

    Page<Pet> findByTutorId(UUID tutorId, Pageable pageable);

    @Query("""
            SELECT p FROM Pet p
            WHERE (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
              AND (:especie IS NULL OR p.especie = :especie)
              AND (:tutorId IS NULL OR p.tutor.id = :tutorId)
            """)
    Page<Pet> search(@Param("nome") String nome,
                     @Param("especie") Especie especie,
                     @Param("tutorId") UUID tutorId,
                     Pageable pageable);
}
