package ufrn.br.TRFNotifica;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import ufrn.br.TRFNotifica.config.RsaKeyProperties;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;
import ufrn.br.TRFNotifica.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
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

	public static void main(String[] args) throws JSONException {
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
