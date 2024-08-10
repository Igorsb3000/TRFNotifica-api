package ufrn.br.TRFNotifica.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ufrn.br.TRFNotifica.config.ApiVersion;
import ufrn.br.TRFNotifica.dto.UsuarioRequestDTO;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.service.CredenciaisService;
import ufrn.br.TRFNotifica.service.UsuarioService;


@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CredenciaisService credenciaisService;

    @PostMapping
    public ResponseEntity<Usuario> cadastrarUsuario(@RequestBody UsuarioRequestDTO newUser) {
        return usuarioService.registerNewUser(newUser);
    }

    @GetMapping("/checkUsername/{username}")
    public boolean checkUsername(@PathVariable String username){
        return credenciaisService.existsByUsername(username);
    }

    @PutMapping
    public ResponseEntity<Void> atualizarUsuario(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsuarioRequestDTO updateUser){
        return usuarioService.updateUser(userDetails.getUsername(), updateUser);
    }

    @GetMapping
    public UsuarioRequestDTO buscarUsuario(@AuthenticationPrincipal UserDetails userDetails){
        return usuarioService.searchUser(userDetails.getUsername());
    }

}
