package ufrn.br.TRFNotifica.dto;

import lombok.Data;

@Data
public class BuscaProcessoRequestDTO {
    String numeroProcesso;
    Integer classeCodigo;
    Integer orgaoJulgadorCodigo;
}
