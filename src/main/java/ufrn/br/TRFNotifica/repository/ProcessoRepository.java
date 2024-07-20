package ufrn.br.TRFNotifica.repository;

import ufrn.br.TRFNotifica.base.BaseRepository;
import ufrn.br.TRFNotifica.model.Processo;

import java.util.Optional;

public interface ProcessoRepository extends BaseRepository<Processo> {
    Optional<Processo> findByIdentificador(String identificador);
}
