package ufrn.br.TRFNotifica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.NotificacaoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository repository;

    private final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);


    public List<Notificacao> findByProcessoId(String processoId) {
        return repository.findByProcessoId(processoId);
    }

    public Optional<Notificacao> findById(String id) {
        return repository.findById(id);
    }

    public List<Notificacao> findByUsuarioId(String usuarioId){
        return repository.findByUsuarioId(usuarioId);
    }

    public Optional<Notificacao> findByUsuarioIdAndProcessoId(String usuarioId, String processoId){
        return repository.findByUsuarioIdAndProcessoId(usuarioId, processoId);
    }

    public void save(Notificacao notificacao){
        repository.save(notificacao);
    }

    public void createAndSave(Usuario usuario, Processo processo) {
        if (usuario == null) {
            throw new IllegalArgumentException("O usuário não pode ser nulo.");
        }
        if (processo == null) {
            throw new IllegalArgumentException("O processo não pode ser nulo.");
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setProcesso(processo);
        notificacao.setUsuario(usuario);

        try {
            repository.save(notificacao);
        } catch (Exception e) {
            String msg = "Erro inesperado ao salvar a notificação: " + e.getMessage();
            logger.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void delete(String id) {
        try {
            Optional<Notificacao> notificacaoBd = repository.findById(id);
            if (notificacaoBd.isPresent()) {
                repository.deleteById(id);
            } else {
                throw new ChangeSetPersister.NotFoundException();
            }
        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao tentar deletar a notificação: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

}
