package ufrn.br.TRFNotifica.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import ufrn.br.TRFNotifica.model.Assunto;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.model.Usuario;

import java.util.List;
import java.util.Set;

@Data
@Getter @Setter
public class ProcessoResponseDTO {
    private String id;
    private String identificador;
    private String numero;
    private String timestamp;
    private List<Assunto> assuntos;
    private List<Movimentacao> movimentacaos;
    // private Set<Usuario> usuarios;
}
