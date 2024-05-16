package ufrn.br.TRFNotifica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.NotificacaoRepository;

import java.util.Optional;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository repository;

    public void createAndSave(Usuario usuario, Processo processo){
        Notificacao notificacao =  new Notificacao(usuario, processo);
        this.save(notificacao);
    }


    public Optional<Notificacao> findByUsuarioIdAndProcessoId(String usuarioId, String processoId){
        return repository.findByUsuarioIdAndProcessoId(usuarioId, processoId);
    }

    public void save(Notificacao notificacao){
        repository.save(notificacao);
    }

    public void delete(String id){
        Optional<Notificacao> notificacaoBd = repository.findById(id);
        if(notificacaoBd.isPresent()){
            repository.deleteById(id);
        } else {
            throw new RuntimeException("Notificação não encontrada!");
        };
    }

}
