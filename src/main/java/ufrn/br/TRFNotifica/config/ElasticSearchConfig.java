package ufrn.br.TRFNotifica.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

    /*
    @Bean
    public RestClientBuilder client() {
        return new RestClientBuilder(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }*/
}
