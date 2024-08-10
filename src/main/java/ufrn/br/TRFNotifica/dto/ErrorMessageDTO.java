package ufrn.br.TRFNotifica.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
public class ErrorMessageDTO {
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int code;
    private HttpStatus status;
    private String message;
    private String requestDescription;
}
