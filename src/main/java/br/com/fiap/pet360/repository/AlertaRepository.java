package br.com.fiap.pet360.repository;

import br.com.fiap.pet360.model.Alerta;
import br.com.fiap.pet360.model.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, UUID> {

    @Query("""
            SELECT a FROM Alerta a
            WHERE (:tutorId IS NULL OR a.tutor.id = :tutorId)
              AND (:petId IS NULL OR a.pet.id = :petId)
              AND (:lido IS NULL OR a.lido = :lido)
              AND (:tipo IS NULL OR a.tipo = :tipo)
            ORDER BY a.dataAlerta DESC
            """)
    Page<Alerta> search(@Param("tutorId") UUID tutorId,
                        @Param("petId") UUID petId,
                        @Param("lido") Boolean lido,
                        @Param("tipo") TipoAlerta tipo,
                        Pageable pageable);
}
