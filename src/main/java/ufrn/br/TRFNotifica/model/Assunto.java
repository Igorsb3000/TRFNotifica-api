package ufrn.br.TRFNotifica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "assuntos_tbl")
public class Assunto extends BaseModel {
    @Column(nullable = false)
    @NotNull(message = "O campo 'codigo' não pode ser nulo.")
    private Integer codigo;

    @Column(nullable = false)
    @NotNull(message = "O campo 'nome' não pode ser nulo.")
    @NotBlank(message = "O campo 'nome' não pode ser vazio.")
    private String nome;

    @ManyToOne
    @JoinColumn(name = "processo_id")
    private Processo processo;
}
