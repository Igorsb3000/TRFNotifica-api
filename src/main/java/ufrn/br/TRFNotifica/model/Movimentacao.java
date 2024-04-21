package ufrn.br.TRFNotifica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufrn.br.TRFNotifica.base.BaseModel;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movimentacoes_tbl")
public class Movimentacao extends BaseModel {
    @NotNull(message = "O campo 'codigo' não pode ser nulo.")
    @NotEmpty(message = "O campo 'codigo' não pode ser vazio.")
    private String codigo;

    @NotNull(message = "O campo 'nome' não pode ser nulo.")
    @NotEmpty(message = "O campo 'nome' não pode ser vazio.")
    private String nome;

    @NotNull(message = "O campo 'dataHora' não pode ser nulo.")
    @NotEmpty(message = "O campo 'dataHora' não pode ser vazio.")
    private String dataHora;

    @OneToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
