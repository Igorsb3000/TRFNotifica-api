package ufrn.br.TRFNotifica.handler;

import jakarta.servlet.UnavailableException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import ufrn.br.TRFNotifica.dto.ErrorMessageDTO;

import java.io.IOException;
import java.time.LocalDateTime;


@ControllerAdvice
public class ExceptionAdvisor {
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorMessageDTO> handleEntityNotFoundException(BadCredentialsException ex, WebRequest request) {
        String msg = "Falha na autenticação. Verifique seu usuário e senha.";
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    ResponseEntity<ErrorMessageDTO> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        String msg = "Usuário não encontrado.";
        // Retorna o status HTTP 404 (NOT_FOUND) para a exceção de nome de usuario nao encontrado
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    ResponseEntity<ErrorMessageDTO> handleNotFoundException(Exception ex, WebRequest request) {
        String msg = "O recurso não foi encontrado.";
        // Retorna o status HTTP 404 (NOT_FOUND) para quando um item nao foi encontrado
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnavailableException.class)
    ResponseEntity<ErrorMessageDTO> handleUnavailableException(Exception ex, WebRequest request) {
        String msg = "O servidor encontra-se temporariamente indisponível.";
        // Retorna o status HTTP 503 (INTERNAL_SERVER_ERROR) para quando a API do BNP estiver fora do ar
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    ResponseEntity<ErrorMessageDTO> handleHttpClientErrorException(Exception ex, WebRequest request) {
        String msg = "O servidor encontra-se temporariamente indisponível.";
        // Retorna o status HTTP 503 (INTERNAL_SERVER_ERROR) para quando a API do BNP estiver fora do ar
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(IOException.class)
    ResponseEntity<ErrorMessageDTO> handleIOException(Exception ex, WebRequest request) {
        String msg = "Houve um problema no envio da solicitação ao servidor.";
        // Retorna o status HTTP 503 (INTERNAL_SERVER_ERROR) para quando a API do BNP estiver fora do ar
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.SERVICE_UNAVAILABLE.value(), HttpStatus.SERVICE_UNAVAILABLE, msg, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorMessageDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
        // Retorna o status HTTP 400 (BAD_REQUEST) quando o for enviado ao end-point um objeto inválido (@Valid)
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST, "A solicitação não pôde ser processada devido a um erro de validação. " + errorMessage, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorMessageDTO> handleGenericException(Exception ex, WebRequest request) {
        String msg = "Ocorreu um erro interno no servidor. ";
        if(ex.getMessage().contains("Http") || ex.getMessage().contains("Exception")){
            msg += "Por favor, tente novamente mais tarde.";
        } else {
            msg += ex.getMessage();
        }
        // Retorna o status HTTP 500 (INTERNAL_SERVER_ERROR) para exceções genéricas
        ErrorMessageDTO error = new ErrorMessageDTO(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR, msg, request.getDescription(false));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

