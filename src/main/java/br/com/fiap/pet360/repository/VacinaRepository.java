package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Vacina;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface VacinaRepository extends JpaRepository<Vacina, UUID> {

    List<Vacina> findByPetIdOrderByDataAplicacaoDesc(UUID petId);

    @Query("""
            SELECT v FROM Vacina v
            WHERE v.pet.id = :petId
              AND v.dataProxima IS NOT NULL
              AND v.dataProxima <= :limite
            ORDER BY v.dataProxima ASC
            """)
    List<Vacina> findVencidasOuProximas(@Param("petId") UUID petId, @Param("limite") LocalDate limite);

    @Query("""
            SELECT v FROM Vacina v
            WHERE (:petId IS NULL OR v.pet.id = :petId)
              AND (:nome IS NULL OR LOWER(v.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            """)
    Page<Vacina> search(@Param("petId") UUID petId, @Param("nome") String nome, Pageable pageable);
}
