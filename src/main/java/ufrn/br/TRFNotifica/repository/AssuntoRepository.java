package ufrn.br.TRFNotifica.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ufrn.br.TRFNotifica.base.BaseRepository;
import ufrn.br.TRFNotifica.model.Assunto;

import java.util.Optional;

@Repository
public interface AssuntoRepository extends BaseRepository<Assunto> {
    @Query(value = "select e from Assunto e where e.codigo=:codigo and e.nome=:nome")
    Optional<Assunto> findByCodigoAndNome(Integer codigo, String nome);
}
