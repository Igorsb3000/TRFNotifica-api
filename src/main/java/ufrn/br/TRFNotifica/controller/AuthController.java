package ufrn.br.TRFNotifica.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ufrn.br.TRFNotifica.dto.TokenResponseDTO;
import ufrn.br.TRFNotifica.service.TokenService;


@RestController
public class AuthController {

    @Autowired
    private TokenService tokenService;


    // O proprio Spring vai instanciar o Authentication com base nos dados passados na solicitacao
    @PostMapping("/token")
    public TokenResponseDTO getToken(@RequestHeader HttpHeaders headers, HttpServletRequest request){
        return tokenService.getToken(headers, request);
    }

}
