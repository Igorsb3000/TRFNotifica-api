package ufrn.br.TRFNotifica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
public class ErrorMessageDTO {
    private LocalDateTime timestamp;
    private int code;
    private HttpStatus status;
    private String message;
    private String requestDescription;
}
