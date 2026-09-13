package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Consulta;
import br.com.fiap.pet360.model.StatusConsulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    List<Consulta> findByPetIdOrderByDataHoraDesc(UUID petId);

    @Query("""
            SELECT c FROM Consulta c
            WHERE (:clinicaId IS NULL OR c.clinica.id = :clinicaId)
              AND (:petId IS NULL OR c.pet.id = :petId)
              AND (:status IS NULL OR c.status = :status)
              AND (:dataInicio IS NULL OR c.dataHora >= :dataInicio)
              AND (:dataFim IS NULL OR c.dataHora <= :dataFim)
            """)
    Page<Consulta> search(@Param("clinicaId") UUID clinicaId,
                          @Param("petId") UUID petId,
                          @Param("status") StatusConsulta status,
                          @Param("dataInicio") LocalDateTime dataInicio,
                          @Param("dataFim") LocalDateTime dataFim,
                          Pageable pageable);
}
