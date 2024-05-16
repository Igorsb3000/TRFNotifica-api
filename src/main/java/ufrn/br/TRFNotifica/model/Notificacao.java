package ufrn.br.TRFNotifica.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notificacoes_tbl")
public class Notificacao {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "processo_id", referencedColumnName = "id", nullable = false)
    private Processo processo;

    public Notificacao(Usuario usuario, Processo processo) {
        this.usuario = usuario;
        this.processo = processo;
    }
}
