package ufrn.br.TRFNotifica.util;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import org.springframework.boot.configurationprocessor.json.JSONException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessoJsonBuilderUtil {

    public static String buildQuery(int size, Long searchAfter, String numero, Integer classeCodigo, Integer orgaoJulgadorCodigo) {
        List<Query> mustQueries = new ArrayList<>();

        // Adiciona as queries conforme os parametros
        if (numero != null && !numero.isEmpty()) {
            mustQueries.add(createMatchQuery("numeroProcesso", numero));
        } else if (classeCodigo != null && orgaoJulgadorCodigo != null) {
            mustQueries.add(createMatchQuery("classe.codigo", classeCodigo));
            mustQueries.add(createMatchQuery("orgaoJulgador.codigo", orgaoJulgadorCodigo));
        }

        // Combina as queries usando bool must
        Query boolQuery = BoolQuery.of(b -> b
                .must(mustQueries)
        )._toQuery();

        // Define a ordenacao por timestamp
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
        return jsonString;
    }

    private static Query createMatchQuery(String field, String value) {
        return MatchQuery.of(m -> m
                .field(field)
                .query(value)
        )._toQuery();
    }

    private static Query createMatchQuery(String field, Integer value) {
        return MatchQuery.of(m -> m
                .field(field)
                .query(value)
        )._toQuery();
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

    public static String formatProcessNumber(String rawProcessNumber) {
        if (rawProcessNumber == null || rawProcessNumber.length() != 20) {
            throw new IllegalArgumentException("O processo deve ter 20 caracteres.");
        }

        StringBuilder sb = new StringBuilder(rawProcessNumber);
        sb.insert(7, '-');
        sb.insert(10, '.');
        sb.insert(15, '.');
        sb.insert(17, '.');
        sb.insert(20, '.');
        return sb.toString();
    }

}
