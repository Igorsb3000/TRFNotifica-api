package ufrn.br.TRFNotifica.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ufrn.br.TRFNotifica.config.ApiVersion;
import ufrn.br.TRFNotifica.dto.UsuarioRequestDTO;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.service.CredenciaisService;
import ufrn.br.TRFNotifica.service.UsuarioService;
import ufrn.br.TRFNotifica.util.CriptografiaUtil;

import java.net.URI;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CredenciaisService credenciaisService;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value( "${app.client-secret}" )
    private String clientSecret;

    @PostMapping("/save")
    public ResponseEntity<Usuario> cadastrarUsuario(@RequestBody UsuarioRequestDTO newUser) {
        Usuario u = new Usuario();
        u.setName(newUser.getName());
        u.setEmail(newUser.getEmail());

        Credenciais c = new Credenciais();
        c.setRoles("user");
        c.setUsername(newUser.getUsername());
        c.setPassword(newUser.getPassword());
        c.setUsuario(u);

        try{
            credenciaisService.create(c);
        }catch (Exception e){
            throw new RuntimeException("Erro ao cadastrar novo usuário: " + e.getMessage());
        }

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(u.getId())
                .toUri();

        return ResponseEntity.created(uri).body(u);
        //return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity atualizarUsuario(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsuarioRequestDTO updateUser){
        Optional<Credenciais> credenciais = credenciaisService.findByUsername(userDetails.getUsername());
        Usuario usuario = null;

        if(credenciais.isPresent() ){
            usuario = usuarioService.findById(credenciais.get().getId());

            // Verifica a senha antiga antes de atualizar
            if (updateUser.getPassword() != null) {
                String decryptedOldPassword = CriptografiaUtil.decrypt(updateUser.getPassword(), clientSecret);
                boolean isOldPasswordValid = encoder.matches(decryptedOldPassword, credenciais.get().getPassword());

                if (!isOldPasswordValid) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Senha atual inválida! Verifique se a senha está correta e tente novamente.");
                }
            }

            if(usuario != null){
                if(updateUser.getEmail() != null){
                    usuario.setEmail(updateUser.getEmail());
                }
                if(updateUser.getName() != null){
                    usuario.setName(updateUser.getName());
                }
                usuarioService.update(usuario);
                if(updateUser.getUsername() != null){
                    credenciais.get().setUsername(updateUser.getUsername());
                }
                if(updateUser.getNewPassword() != null){
                    String newPassword = CriptografiaUtil.decrypt(updateUser.getNewPassword(), clientSecret);
                    credenciais.get().setPassword(encoder.encode(newPassword));
                }
                credenciaisService.update(credenciais.get());
            }

        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public UsuarioRequestDTO buscarUsuario(@AuthenticationPrincipal UserDetails userDetails){
        Optional<Credenciais> credenciais = credenciaisService.findByUsername(userDetails.getUsername());
        Usuario usuario = null;
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getId());
        }
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName(usuario.getName());
        dto.setEmail(usuario.getEmail());
        dto.setUsername(credenciais.get().getUsername());
        return dto;
    }

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarUsuario(@PathVariable String id){
        credenciaisService.delete(id);
        return ResponseEntity.noContent().build();
    }*/

}
