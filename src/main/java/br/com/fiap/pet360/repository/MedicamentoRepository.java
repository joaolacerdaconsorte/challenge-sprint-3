package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Medicamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, UUID> {

    List<Medicamento> findByPetIdAndAtivoTrueOrderByDataInicioDesc(UUID petId);

    List<Medicamento> findByPetIdOrderByDataInicioDesc(UUID petId);

    @Query("""
            SELECT m FROM Medicamento m
            WHERE (:petId IS NULL OR m.pet.id = :petId)
              AND (:ativo IS NULL OR m.ativo = :ativo)
              AND (:nome IS NULL OR LOWER(m.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
            """)
    Page<Medicamento> search(@Param("petId") UUID petId,
                             @Param("ativo") Boolean ativo,
                             @Param("nome") String nome,
                             Pageable pageable);
}
