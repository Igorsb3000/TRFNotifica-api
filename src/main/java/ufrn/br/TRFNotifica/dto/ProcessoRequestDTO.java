package ufrn.br.TRFNotifica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class ProcessoRequestDTO {
    private String numeroProcesso;
    private Classe classe;
    private Sistema sistema;
    private Formato formato;
    private String tribunal;
    @JsonProperty("@timestamp")
    private String timestamp;
    private String dataHoraUltimaAtualizacao;
    private String grau;
    private String dataAjuizamento;
    private List<Movimento> movimentos;
    private String id;
    private Integer nivelSigilo;
    private OrgaoJulgador orgaoJulgador;
    private List<Assunto> assuntos;

    // Matar as de fora
    @Getter
    public static class Classe {
        private Integer codigo;
        private String nome;
    }

    @Getter
    public static class Assunto {
        private Integer codigo;
        private String nome;
    }

    @Getter
    public static class Formato {
        private Integer codigo;
        private String nome;
    }

    @Getter
    public static class Movimento {
        private Integer codigo;
        private String nome;
        private String dataHora;
        private List<ComplementoTabelado> complementosTabelados;
    }

    @Getter
    public static class ComplementoTabelado {
        private Integer codigo;
        private Integer valor;
        private String nome;
        private String descricao;
    }


    @Getter
    public static class OrgaoJulgador {
        private Integer codigoMunicipioIBGE;
        private Integer codigo;
        private String nome;
    }

    @Getter
    public static class Sistema {
        private Integer codigo;
        private String nome;
    }

}



