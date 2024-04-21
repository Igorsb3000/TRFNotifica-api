package ufrn.br.TRFNotifica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ufrn.br.TRFNotifica.config.RsaKeyProperties;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class) // Para que a Classe RsaKeyProperties seja habilitada para cuidar da autenticação via chaves publica e privada
public class TrfNotificaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrfNotificaApplication.class, args);
	}

}
