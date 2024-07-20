package ufrn.br.TRFNotifica.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.repository.CredenciaisRepository;
import ufrn.br.TRFNotifica.util.CriptografiaUtil;

import java.util.Optional;


@Service
public class CredenciaisService extends BaseService<Credenciais, CredenciaisRepository> implements UserDetailsService {
    @Autowired
    CredenciaisRepository credenciaisRepository;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Value( "${app.client-secret}" )
    private String clientSecret;

    private static final Logger logger = LoggerFactory.getLogger(CredenciaisService.class);

    public Optional<Credenciais> findByUsername(String username){
        return credenciaisRepository.findCredenciaisByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Credenciais> credenciaisOptional = repository.findCredenciaisByUsername(username);

        return credenciaisOptional.orElseThrow(() ->
                new UsernameNotFoundException("Usuário com username '" + username + "' não encontrado no sistema."));
    }
    @Override
    @Transactional
    public Credenciais create(Credenciais credenciais) {
        try {
            // Verificação de entrada não nula
            if (credenciais == null) {
                throw new IllegalArgumentException("Credenciais não podem ser nulas");
            }
            String decryptedPassword = CriptografiaUtil.decrypt(credenciais.getPassword(), clientSecret);
            String encodedPassword = encoder.encode(decryptedPassword);
            credenciais.setPassword(encodedPassword);
            return credenciaisRepository.save(credenciais);
        } catch (Exception e) {
            String errorMessage = "Erro ao criar credenciais: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

}

