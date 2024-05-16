package ufrn.br.TRFNotifica.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufrn.br.TRFNotifica.base.BaseModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    //@ManyToMany(cascade = {CascadeType.ALL})
    //@JoinTable(name = "notificacoes_tbl", joinColumns = { @JoinColumn(name = "usuario_id", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "processo_id") })
    //private Set<Processo> processos;

    /*
    public void addProcesso(Processo nProcesso) {
        processos.add(nProcesso);
        nProcesso.getUsuarios().add(this);
    }
    public void removeProcesso(Processo nProcesso) {
        processos.remove(nProcesso);
        nProcesso.getUsuarios().remove(this);
    }
     */
}
