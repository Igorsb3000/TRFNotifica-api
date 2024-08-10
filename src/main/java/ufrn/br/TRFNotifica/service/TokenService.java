package ufrn.br.TRFNotifica.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.dto.LoginDTO;
import ufrn.br.TRFNotifica.dto.TokenResponseDTO;
import ufrn.br.TRFNotifica.util.CriptografiaUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    @Value( "${app.client-secret}" )
    private String clientSecret;

    @Autowired
    private final JwtEncoder encoder;

    @Autowired
    private final AuthenticationManager authenticationManager;

    private final Logger logger = LoggerFactory.getLogger(TokenService.class);


    public TokenResponseDTO getToken(HttpHeaders headers, HttpServletRequest request) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isEmpty()) {
            throw new IllegalArgumentException("Cabeçalho de Authorization está ausente ou vazio.");
        }

        LoginDTO loginDto = extrairCredenciais(authHeader);

        if (loginDto == null) {
            throw new IllegalArgumentException("Formato inválido de login.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.username(), loginDto.password())
            );
            String token = this.generateToken(authentication);

            return TokenResponseDTO.builder()
                    .username(loginDto.username())
                    .token(token)
                    .build();
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Usuário ou senha inválido.", e);
        } catch (AuthenticationException e) {
            throw new RuntimeException("Falha na autenticação.", e);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado na autenticação: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


    LoginDTO extrairCredenciais(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            throw new IllegalArgumentException("Cabeçalho de Authorization está ausente ou vazio.");
        }

        try {
            // Extrair e decodificar as credenciais do cabeçalho
            String crytoCredenciais = authHeader.substring("Basic ".length()).trim();
            String credentials = CriptografiaUtil.decrypt(crytoCredenciais, clientSecret);

            if (credentials == null || credentials.isEmpty()) {
                throw new IllegalArgumentException("Descriptografia das credenciais falhou ou o resultado é um valor vazio.");
            }

            String[] splitCredentials = credentials.split(":", 2);

            if (splitCredentials.length < 2 || splitCredentials[0].isEmpty() || splitCredentials[1].isEmpty()) {
                throw new IllegalArgumentException("Credencias em formato inválido.");
            }

            // Obter nome de usuário e senha
            String username = splitCredentials[0];
            String password = splitCredentials[1];
            return new LoginDTO(username, password);

        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao extrair e descriptografar credenciais: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }



    // Cria o token para o usuário se autenticar e o tempo de validade do token é de 1 hora
    public String generateToken(Authentication authentication) {
        try {
            Instant now = Instant.now();

            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .expiresAt(now.plus(1, ChronoUnit.HOURS))
                    .subject(authentication.getName()) // nome de usuario
                    .claim("scope", scope)
                    .build();

            return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao gerar o token JWT: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }
}