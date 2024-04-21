package ufrn.br.TRFNotifica.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ufrn.br.TRFNotifica.dto.LoginDTO;
import ufrn.br.TRFNotifica.dto.TokenResponseDTO;
import ufrn.br.TRFNotifica.service.TokenService;


@RestController
public class AuthController {
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthController(TokenService tokenService, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    // É o prórprio Spring quem vai instanciar o Authentication com base nos dados passados na solicitação
    @PostMapping("/token")
    public TokenResponseDTO getToken(@RequestBody LoginDTO loginDTO){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password()));
        String token = tokenService.generateToken(authentication);
        return TokenResponseDTO
                .builder()
                .username(loginDTO.username())
                .token(token)
                .build();
    }

}
