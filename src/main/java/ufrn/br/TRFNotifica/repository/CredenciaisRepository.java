package ufrn.br.TRFNotifica.repository;

import org.springframework.stereotype.Repository;
import ufrn.br.TRFNotifica.base.BaseRepository;
import ufrn.br.TRFNotifica.model.Credenciais;

import java.util.Optional;

@Repository
public interface CredenciaisRepository extends BaseRepository<Credenciais> {
    Optional<Credenciais> findCredenciaisByUsername(String username);
}

