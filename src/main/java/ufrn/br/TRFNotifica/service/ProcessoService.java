package ufrn.br.TRFNotifica.service;

import jakarta.servlet.UnavailableException;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessoService extends BaseService<Processo, ProcessoRepository> {
    private final String authorization = "APIKey cDZHYzlZa0JadVREZDJCendQbXY6SkJlTzNjLV9TRENyQk1RdnFKZGRQdw==";
    private final String urlTrf1 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf1/_search";
    private final String urlTrf2 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf2/_search";
    private final String urlTrf3 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf3/_search";
    private final String urlTrf4 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf4/_search";
    private final String urlTrf5 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf5/_search";
    private final String urlTrf6 = "https://api-publica.datajud.cnj.jus.br/api_publica_trf6/_search";

    public String findByNumero(String numeroProcesso) throws JSONException {
        JSONObject match = new JSONObject();
        match.put("numeroProcesso", numeroProcesso);
        JSONObject query = new JSONObject();
        query.put("match", match);
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("query", query);
        System.out.println("JSON ENVIADO: " + jsonObj);
        String jsonString = jsonObj.toString();

        try {
            var request = HttpRequest.newBuilder()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .uri(new URI(urlTrf1))
                    .build();

            var response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            ArrayList<JSONObject> responseObject = getHits(response.body().toString());

            if (response.statusCode() != 200){
                if(response.statusCode() == HttpStatus.SERVICE_UNAVAILABLE.value()){
                    throw new UnavailableException("Serviço temporariamente indisponível.");
                }
                if (response.statusCode() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Não autorizado: verifique suas credenciais.");
                }
                if (response.statusCode() == HttpStatus.FORBIDDEN.value()) {
                    throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Proibido: você não tem permissão para acessar este recurso.");
                }
                if (response.statusCode() == HttpStatus.BAD_REQUEST.value()) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique a sintaxe da sua requisição.");
                }
                if (response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.");
                }
                throw new RuntimeException("Erro ao salvar recurso: " + response.body());
            }
            return responseObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public String findByClasseEOrgao(Integer classeCodigo, Integer orgaoJulgadorCodigo) throws JSONException {
        // Cria o objeto JSON principal
        JSONObject queryObject = new JSONObject();

        // Cria o objeto "bool"
        JSONObject boolObject = new JSONObject();

        // Cria o array "must"
        JSONArray mustArray = new JSONArray();

        // Adiciona os objetos "match" ao array "must"
        JSONObject matchClasse = new JSONObject();
        matchClasse.put("match", new JSONObject().put("classe.codigo", classeCodigo));
        mustArray.put(matchClasse);

        JSONObject matchOrgaoJulgador = new JSONObject();
        matchOrgaoJulgador.put("match", new JSONObject().put("orgaoJulgador.codigo", orgaoJulgadorCodigo));
        mustArray.put(matchOrgaoJulgador);

        // Adiciona o array "must" ao objeto "bool"
        boolObject.put("must", mustArray);

        // Adiciona o objeto "bool" ao objeto "query"
        queryObject.put("bool", boolObject);

        // Cria o objeto JSON final com a estrutura especificada
        JSONObject finalObject = new JSONObject();
        finalObject.put("query", queryObject);

        System.out.println("JSON ENVIADO: " + finalObject);
        String jsonString = finalObject.toString();

        try {
            var request = HttpRequest.newBuilder()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .uri(new URI(urlTrf1))
                    .build();

            var response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            ArrayList<JSONObject> responseObject = getHits(response.body().toString());

            if (response.statusCode() != 200){
                if(response.statusCode() == HttpStatus.SERVICE_UNAVAILABLE.value()){
                    throw new UnavailableException("Serviço temporariamente indisponível.");
                }
                if (response.statusCode() == HttpStatus.UNAUTHORIZED.value()) {
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Não autorizado: verifique suas credenciais.");
                }
                if (response.statusCode() == HttpStatus.FORBIDDEN.value()) {
                    throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Proibido: você não tem permissão para acessar este recurso.");
                }
                if (response.statusCode() == HttpStatus.BAD_REQUEST.value()) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique a sintaxe da sua requisição.");
                }
                if (response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                    throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.");
                }
                throw new RuntimeException("Erro ao salvar recurso: " + response.body());
            }
            return responseObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public ArrayList<JSONObject> getHits(String jsonString) throws JSONException {
        JSONObject jsonObj = new JSONObject(jsonString);
        JSONObject hitsObj = jsonObj.getJSONObject("hits");
        JSONArray hitsArray = hitsObj.getJSONArray("hits");
        JSONObject totalObj = hitsObj.getJSONObject("total");

        int total = totalObj.getInt("value");
        String relation = totalObj.getString("relation");
        ArrayList<JSONObject> response = new ArrayList<>();
        System.out.println("total = " + total);
        System.out.println("relation = " + relation);
        System.out.println(!relation.equalsIgnoreCase("eq"));
        if(total == 1 && relation.equalsIgnoreCase("eq")){
            response.add(hitsArray.getJSONObject(0));
        } else if(total > 1) {
            response.add(hitsArray.getJSONObject(0));
            /*
            for(int i = 0; i < total; i++){
                response.add(hitsArray.getJSONObject(i));
            }*/
            // Defina o tamanho do lote (batch size)
            int batchSize = 100; // Por exemplo, processe 100 objetos por lote

            // Processo em lotes
            for (int i = 0; i < total; i += batchSize) {
                // Obtenha o sub-list do lote atual
                JSONArray batch = hitsArray.put(i, Math.min(i + batchSize, total));

                // Processar o lote atual
                processBatch(batch);
            }
        }
        return response;
    }

    // Método para processar um lote de objetos JSON
    public static void processBatch(JSONArray batch) throws JSONException {
        // Implemente o processamento para cada lote aqui
        for (int i = 0; i < batch.length(); i++) {
            JSONObject obj = batch.getJSONObject(i);
            // Processa o objeto JSON
            System.out.println(obj.toString());
            // Adicione seu processamento específico aqui
        }
    }
}
