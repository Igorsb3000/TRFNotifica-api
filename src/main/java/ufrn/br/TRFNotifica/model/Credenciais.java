package ufrn.br.TRFNotifica.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ufrn.br.TRFNotifica.base.BaseModel;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Entity
@Table(name = "credenciais_tbl")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Credenciais extends BaseModel implements UserDetails {
    @NotNull(message = "O campo 'username' não pode ser nulo.")
    @NotBlank(message = "O campo 'username' não pode ser vazio.")
    @Column(unique = true)
    String username;// alterar para usuario

    @NotNull(message = "O campo 'password' não pode ser nulo.")
    @NotBlank(message = "O campo 'password' não pode ser vazio.")
    String password; // alterar para senha

    String roles; // admin, user, personal
    // alterar para papeis

    @JsonBackReference
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @MapsId
    @JoinColumn(name = "id_user")
    Usuario usuario;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(roles.split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

