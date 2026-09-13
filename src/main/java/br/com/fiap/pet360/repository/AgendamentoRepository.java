package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Agendamento;
import br.com.fiap.pet360.model.StatusAgendamento;
import br.com.fiap.pet360.model.TipoAgendamento;
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
public interface AgendamentoRepository extends JpaRepository<Agendamento, UUID> {

    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.status = br.com.fiap.pet360.model.StatusAgendamento.PENDENTE
              AND a.dataHora >= :agora
              AND (:clinicaId IS NULL OR a.clinica.id = :clinicaId)
            ORDER BY a.dataHora ASC
            """)
    List<Agendamento> findPendentes(@Param("agora") LocalDateTime agora, @Param("clinicaId") UUID clinicaId);

    @Query("""
            SELECT a FROM Agendamento a
            WHERE (:petId IS NULL OR a.pet.id = :petId)
              AND (:clinicaId IS NULL OR a.clinica.id = :clinicaId)
              AND (:status IS NULL OR a.status = :status)
              AND (:tipo IS NULL OR a.tipo = :tipo)
            """)
    Page<Agendamento> search(@Param("petId") UUID petId,
                             @Param("clinicaId") UUID clinicaId,
                             @Param("status") StatusAgendamento status,
                             @Param("tipo") TipoAgendamento tipo,
                             Pageable pageable);

    boolean existsByClinicaIdAndDataHoraAndStatusNot(UUID clinicaId, LocalDateTime dataHora, StatusAgendamento status);
}
