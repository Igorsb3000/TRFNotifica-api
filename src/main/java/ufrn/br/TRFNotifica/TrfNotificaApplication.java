package ufrn.br.TRFNotifica;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.parameters.P;
import ufrn.br.TRFNotifica.config.RsaKeyProperties;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;
import ufrn.br.TRFNotifica.repository.UsuarioRepository;

import java.util.Optional;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class) // Para que a Classe RsaKeyProperties seja habilitada para cuidar da autenticação via chaves publica e privada
public class TrfNotificaApplication {
	@Bean
	public ModelMapper mapper(){
		return new ModelMapper();
	}

	@Bean
	public ObjectMapper objectMapper(){
		return new ObjectMapper();
	}
	public static void main(String[] args) {
		SpringApplication.run(TrfNotificaApplication.class, args);
	}


	@Autowired
	UsuarioRepository usuarioRepository;

	@Autowired
	ProcessoRepository processoRepository;


	// PAREI AQUI, DEU ERRO
	/*
	@PostConstruct
	public void started() {
		Optional<Usuario> usuarioBd = usuarioRepository.findById("983b331a-001e-4f21-9b4e-3a293120b4df");
		Usuario usuario = new Usuario();
		Optional<Processo> processoBd = processoRepository.findById("a37b679b-d75f-480d-ac96-fb8c9fcc612c");
		Processo processo = new Processo();
		if(usuarioBd.isPresent() && processoBd.isPresent()){
			usuario = usuarioBd.get();
			processo = processoBd.get();
			usuario.addProcesso(processo);
		}
		usuarioRepository.save(usuario);



	}*/
}
