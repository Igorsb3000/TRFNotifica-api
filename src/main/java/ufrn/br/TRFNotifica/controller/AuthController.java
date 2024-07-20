package ufrn.br.TRFNotifica.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ufrn.br.TRFNotifica.dto.LoginDTO;
import ufrn.br.TRFNotifica.dto.TokenResponseDTO;
import ufrn.br.TRFNotifica.service.TokenService;
import ufrn.br.TRFNotifica.util.CriptografiaUtil;


@RestController
public class AuthController {
    @Value( "${app.client-secret}" )
    private String clientSecret;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthController(TokenService tokenService, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    // É o prórprio Spring quem vai instanciar o Authentication com base nos dados passados na solicitação
    @PostMapping("/token")
    public TokenResponseDTO getToken(@RequestHeader HttpHeaders headers, HttpServletRequest request){
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        LoginDTO loginDto = extrairCredenciais(authHeader);

        assert loginDto != null;

        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password())
                );
        String token = tokenService.generateToken(authentication);

        return TokenResponseDTO
                .builder()
                .username(loginDto.username())
                .token(token)
                .build();

    }
    /*public TokenResponseDTO getToken(@RequestBody LoginDTO loginDTO){
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginDTO.username(), loginDTO.password()));
        String token = tokenService.generateToken(authentication);
        return TokenResponseDTO
                .builder()
                .username(loginDTO.username())
                .token(token)
                .build();
    }*/
    LoginDTO extrairCredenciais(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            // Extrair e decodificar as credenciais do cabeçalho
            String crytoCredenciais = authHeader.substring("Basic ".length()).trim();
            String credentials = CriptografiaUtil.decrypt(crytoCredenciais, clientSecret);
            assert credentials != null;
            String[] splitCredentials = credentials.split(":", 2);

            // Obter nome de usuário e senha
            String username = splitCredentials[0];
            String password = splitCredentials[1];
            return new LoginDTO(username, password);
        }
        return null;
    }

}
