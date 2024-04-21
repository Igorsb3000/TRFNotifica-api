package ufrn.br.TRFNotifica.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.repository.CredenciaisRepository;

import java.util.Optional;


@Service
public class CredenciaisService extends BaseService<Credenciais, CredenciaisRepository> implements UserDetailsService {
    @Autowired
    CredenciaisRepository credenciaisRepository;
    @Autowired
    BCryptPasswordEncoder encoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Credenciais> credenciais = repository.findCredenciaisByUsername(username);
        if(credenciais.isPresent()){
            return credenciais.get();
        } else {
            throw new UsernameNotFoundException("O usuário com username = " + username + " não existe no sistema.");
        }
    }
    @Override
    @Transactional
    public Credenciais create(Credenciais c){
        c.setPassword(encoder.encode(c.getPassword()));
        return credenciaisRepository.save(c);
    }
}

