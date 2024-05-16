package ufrn.br.TRFNotifica.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufrn.br.TRFNotifica.base.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "processos_tbl")
public class Processo extends BaseModel {
    @Column(nullable = false)
    @NotNull(message = "O campo 'identificador' não pode ser nulo.")
    @NotBlank(message = "O campo 'identificador' não pode ser vazio.")
    private String identificador;

    @Column(nullable = false)
    @NotNull(message = "O campo 'numero' não pode ser nulo.")
    @NotBlank(message = "O campo 'numero' não pode ser vazio.")
    @Size(min = 20, max = 20, message = "O campo 'numero' deve conter exatamente 20 números.")
    private String numero;

    @Column(nullable = false)
    @NotNull(message = "O campo 'timestamp' não pode ser nulo.")
    @NotBlank(message = "O campo 'timestamp' não pode ser vazio.")
    private String timestamp;

    @Column(nullable = false)
    @NotNull(message = "O campo 'classeNome' não pode ser nulo.")
    @NotBlank(message = "O campo 'classeNome' não pode ser vazio.")
    private String classeNome;

    @Column(nullable = false)
    @NotNull(message = "O campo 'tribunal' não pode ser nulo.")
    @NotBlank(message = "O campo 'tribunal' não pode ser vazio.")
    private String tribunal;

    @Column(nullable = false)
    @NotNull(message = "O campo 'sistemaNome' não pode ser nulo.")
    @NotBlank(message = "O campo 'sistemaNome' não pode ser vazio.")
    private String sistemaNome;

    @Column(nullable = false)
    @NotNull(message = "O campo 'grau' não pode ser nulo.")
    @NotBlank(message = "O campo 'grau' não pode ser vazio.")
    private String grau;

    @Column(nullable = false)
    @NotNull(message = "O campo 'orgaoJulgadorNome' não pode ser nulo.")
    @NotBlank(message = "O campo 'orgaoJulgadorNome' não pode ser vazio.")
    private String orgaoJulgadorNome;

    @Column(nullable = false)
    @NotNull(message = "O campo 'dataAjuizamento' não pode ser nulo.")
    @NotBlank(message = "O campo 'dataAjuizamento' não pode ser vazio.")
    private String dataAjuizamento;

    @JsonManagedReference
    @Column(nullable = false)
    @OneToMany(mappedBy = "processo", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @Valid
    private List<Assunto> assuntos;

    @JsonManagedReference
    @OneToMany(mappedBy = "processo", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @Valid
    private List<Movimentacao> movimentacaos;

    //@ManyToMany(mappedBy="processos", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    //private Set<Usuario> usuarios;
}
