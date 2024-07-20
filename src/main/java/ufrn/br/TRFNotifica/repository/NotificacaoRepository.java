package ufrn.br.TRFNotifica.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ufrn.br.TRFNotifica.model.Notificacao;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, String> {
    @Query(value = "select e from Notificacao e where e.usuario.id=:usuarioId and e.processo.id=:processoId")
    Optional<Notificacao> findByUsuarioIdAndProcessoId(String usuarioId, String processoId);

    @Query("select e from Notificacao e where e.processo.id=:processoId")
    List<Notificacao> findByProcessoId(String processoId);

    @Query("select e from Notificacao e where e.usuario.id=:usuarioId")
    List<Notificacao> findByUsuarioId(String usuarioId);
}
