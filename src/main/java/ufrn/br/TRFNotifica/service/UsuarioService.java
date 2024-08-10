package ufrn.br.TRFNotifica.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.dto.UsuarioRequestDTO;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.UsuarioRepository;
import ufrn.br.TRFNotifica.util.CriptografiaUtil;

import java.net.URI;
import java.util.Optional;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioRepository> {
    @Autowired
    private CredenciaisService credenciaisService;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Value( "${app.client-secret}" )
    private String clientSecret;

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);


    public ResponseEntity<Usuario> registerNewUser(UsuarioRequestDTO newUserDTO) {
        try {
            // Validacao basica dos campos
            if (newUserDTO.getName() == null || newUserDTO.getName().isEmpty() ||
                    newUserDTO.getEmail() == null || newUserDTO.getEmail().isEmpty() ||
                    newUserDTO.getUsername() == null || newUserDTO.getUsername().isEmpty() ||
                    newUserDTO.getPassword() == null || newUserDTO.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Campos obrigatórios não preenchidos.");
            }

            Usuario u = new Usuario();
            u.setName(newUserDTO.getName());
            u.setEmail(newUserDTO.getEmail());

            Credenciais c = new Credenciais();
            c.setRoles("user");
            c.setUsername(newUserDTO.getUsername());
            c.setPassword(newUserDTO.getPassword());
            c.setUsuario(u);

            credenciaisService.create(c);

            URI uri = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(u.getId())
                    .toUri();

            return ResponseEntity.created(uri).body(u);
        } catch (IllegalArgumentException e) {
            String errorMessage = "Erro de validação ao cadastrar novo usuário: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public ResponseEntity<Void> updateUser(String username, UsuarioRequestDTO updateUser) {
        try {
            // Validacao basica dos campos do DTO
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio.");
            }

            Optional<Credenciais> credenciaisOpt = credenciaisService.findByUsername(username);
            if (credenciaisOpt.isEmpty()) {
                throw new EntityNotFoundException("Credenciais não encontradas para o usuário: " + username);
            }

            Credenciais credenciais = credenciaisOpt.get();
            Usuario usuario = this.findById(credenciais.getId());
            if (usuario == null) {
                throw new EntityNotFoundException("Usuário não encontrado.");
            }

            // Verifica a senha antiga antes de atualizar
            if (updateUser.getPassword() != null) {
                String decryptedOldPassword = CriptografiaUtil.decrypt(updateUser.getPassword(), clientSecret);
                boolean isOldPasswordValid = encoder.matches(decryptedOldPassword, credenciais.getPassword());

                if (!isOldPasswordValid) {
                    throw new IllegalArgumentException("Senha atual inválida! Verifique se a senha está correta e tente novamente.");
                }
            }

            // Atualiza os campos do usuário e credenciais
            if (updateUser.getEmail() != null) {
                usuario.setEmail(updateUser.getEmail());
            }
            if (updateUser.getName() != null) {
                usuario.setName(updateUser.getName());
            }
            this.update(usuario);

            if (updateUser.getUsername() != null) {
                credenciais.setUsername(updateUser.getUsername());
            }
            if (updateUser.getNewPassword() != null) {
                String newPassword = CriptografiaUtil.decrypt(updateUser.getNewPassword(), clientSecret);
                credenciais.setPassword(encoder.encode(newPassword));
            }
            credenciaisService.update(credenciais);

            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            String errorMessage = "Erro de validação: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        } catch (EntityNotFoundException e) {
            String errorMessage = "Recurso não encontrado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new EntityNotFoundException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public UsuarioRequestDTO searchUser(String username) {
        try {
            // Validacao basica do parametro de entrada
            if (username == null || username.isEmpty()) {
                throw new IllegalArgumentException("Nome de usuário não pode ser nulo ou vazio.");
            }

            // Buscar credenciais pelo nome de usuario
            Optional<Credenciais> credenciaisOpt = credenciaisService.findByUsername(username);
            if (credenciaisOpt.isEmpty()) {
                throw new EntityNotFoundException("Credenciais não encontradas para o usuário: " + username);
            }

            Credenciais credenciais = credenciaisOpt.get();

            // Buscar usuario pelo ID das credenciais
            Usuario usuario = this.findById(credenciais.getUsuario().getId());
            if (usuario == null) {
                throw new EntityNotFoundException("Usuário não encontrado.");
            }

            // Criar e retornar o DTO do usuário
            UsuarioRequestDTO dto = new UsuarioRequestDTO();
            dto.setName(usuario.getName());
            dto.setEmail(usuario.getEmail());
            dto.setUsername(credenciais.getUsername());

            return dto;
        } catch (IllegalArgumentException e) {
            String errorMessage = "Erro de validação: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        } catch (EntityNotFoundException e) {
            String errorMessage = "Recurso não encontrado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new EntityNotFoundException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


}
