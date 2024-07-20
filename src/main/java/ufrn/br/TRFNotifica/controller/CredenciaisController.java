package ufrn.br.TRFNotifica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ufrn.br.TRFNotifica.dto.UsuarioRequestDTO;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.service.CredenciaisService;


@RestController
@RequestMapping("/credenciais")
public class CredenciaisController {
    @Autowired
    CredenciaisService credenciaisService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UsuarioRequestDTO credenciaisDTO){
        Usuario u = new Usuario();
        u.setName(credenciaisDTO.getName());
        u.setEmail(credenciaisDTO.getEmail());

        Credenciais c = new Credenciais();
        //c.setRoles(credenciaisDTO.getRole());
        c.setUsername(credenciaisDTO.getUsername());
        c.setPassword(credenciaisDTO.getPassword());
        c.setUsuario(u);

        credenciaisService.create(c);

        return ResponseEntity.noContent().build();
    }

}

