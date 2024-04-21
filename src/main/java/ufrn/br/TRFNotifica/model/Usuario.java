package ufrn.br.TRFNotifica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufrn.br.TRFNotifica.base.BaseModel;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuarios_tbl")
public class Usuario extends BaseModel {
    @Column(nullable = false)
    @NotNull(message = "O campo 'nomeCompleto' não pode ser nulo.")
    @NotBlank(message = "O campo 'nomeCompleto' não pode ser vazio.")
    private String name;

    @Column(nullable = false)
    @NotNull(message = "O campo 'email' não pode ser nulo.")
    @NotBlank(message = "O campo 'email' não pode ser vazio.")
    private String email;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "notificacoes", joinColumns = { @JoinColumn(name = "usuario_id",
            referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "processo_id") })
    private List<Processo> processos = new ArrayList<>();

    public void addProcesso(Processo nProcesso) {
        processos.add(nProcesso);
        nProcesso.getUsuarios().add(this);
    }
    public void removeProcesso(Processo nProcesso) {
        processos.remove(nProcesso);
        nProcesso.getUsuarios().remove(this);
    }
}
