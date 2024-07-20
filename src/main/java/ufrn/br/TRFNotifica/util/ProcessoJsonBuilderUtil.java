package ufrn.br.TRFNotifica.util;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.configurationprocessor.json.JSONException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessoJsonBuilderUtil {

    public static String buildQuery(int size, Long searchAfter, String numero, Integer classeCodigo, Integer orgaoJulgadorCodigo) throws IOException, JsonProcessingException, JSONException {
        List<Query> mustQueries = new ArrayList<>();

        // Condicionalmente adiciona a query para numeroProcesso
        if (numero != null && !numero.isEmpty()) {// adicionar método para retorno match
            Query matchNumero = MatchQuery.of(m -> m
                    .field("numeroProcesso")
                    .query(numero)
            )._toQuery();
            mustQueries.add(matchNumero);

        } else if(classeCodigo != null && orgaoJulgadorCodigo != null){
            // Adiciona a query para classe.codigo
            Query matchClasse = MatchQuery.of(m -> m
                    .field("classe.codigo")
                    .query(classeCodigo)
            )._toQuery();
            mustQueries.add(matchClasse);

            // Adiciona a query para orgaoJulgador.codigo
            Query matchOrgaoJulgador = MatchQuery.of(m -> m
                    .field("orgaoJulgador.codigo")
                    .query(orgaoJulgadorCodigo)
            )._toQuery();
            mustQueries.add(matchOrgaoJulgador);
        }

        // Combina as queries usando bool must
        Query boolQuery = BoolQuery.of(b -> b
                .must(mustQueries)
        )._toQuery();

        // Define a ordenação por timestamp
        SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                        .field("@timestamp")
                        .order(SortOrder.Asc)
                )
        );

        // Monta o objeto de busca
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .size(size)
                .query(boolQuery)
                .sort(sortOptions);

        if (searchAfter != null) {
            searchRequestBuilder.searchAfter(searchAfter);
        }

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();
        String jsonString = JsonpUtils.toJsonString(searchRequestBuilder.build(), jsonpMapper);

        System.out.println("Requisicao montada: " + jsonString);
        return jsonString;
    }

    public static String getJsonStringByIdentificador(String identificadorProcesso) throws JSONException {
        Query matchIdentificador = MatchQuery.of(m -> m
                .field("id")
                .query(identificadorProcesso)
        )._toQuery();

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .query(matchIdentificador);

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper();
        String jsonString = JsonpUtils.toJsonString(searchRequestBuilder.build(), jsonpMapper);

        return jsonString;
    }

}
