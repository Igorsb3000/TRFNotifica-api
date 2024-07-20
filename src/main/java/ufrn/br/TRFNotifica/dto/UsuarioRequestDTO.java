package ufrn.br.TRFNotifica.dto;

import lombok.Data;

@Data
public class UsuarioRequestDTO {
    String name; // Usuario
    String email; // Usuario
    String username; // Credenciais
    String password; // Credenciais
    String newPassword;
}
