package ufrn.br.TRFNotifica;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import ufrn.br.TRFNotifica.config.RsaKeyProperties;


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(RsaKeyProperties.class) // Para que a Classe RsaKeyProperties seja habilitada para cuidar da autenticacao via chaves publica e privada
public class TrfNotificaApplication {
	@Bean
	public ModelMapper mapper(){
		return new ModelMapper();
	}

	@Bean
	public ObjectMapper objectMapper(){
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
	}

	public static void main(String[] args) throws JSONException {
		SpringApplication.run(TrfNotificaApplication.class, args);
	}

}
