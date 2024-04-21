package ufrn.br.TRFNotifica.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponseDTO {
    private String token;
    private String username;
}

