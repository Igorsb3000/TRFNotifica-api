package ufrn.br.TRFNotifica.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.UnavailableException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.dto.BuscaProcessoRequestDTO;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;
import ufrn.br.TRFNotifica.repository.UsuarioRepository;
import ufrn.br.TRFNotifica.util.ProcessoJsonBuilderUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ProcessoService extends BaseService<Processo, ProcessoRepository> {
    @Value( "${authorization}" )
    private String authorization;

    @Value( "${url.trf1}" )
    private String urlTrf1;

    @Value( "${url.trf2}" )
    private String urlTrf2;

    @Value( "${url.trf3}" )
    private String urlTrf3;

    @Value( "${url.trf4}" )
    private String urlTrf4;

    @Value( "${url.trf5}" )
    private String urlTrf5;

    @Value( "${url.trf6}" )
    private String urlTrf6;

    private String urlDbJsonServer = "http://localhost:3000/hits";
    private final List<String> urls = new ArrayList<>();

    @Autowired
    private UsuarioRepository usuarioRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProcessoService.class);

    @PostConstruct
    public void init() {
        //urls.add(urlDbJsonServer);
        urls.addAll(Arrays.asList(urlTrf1, urlTrf2, urlTrf3, urlTrf4, urlTrf5, urlTrf6));
    }
    private final Integer size = 10;
    private Long sortValue = null;
    private ArrayList<JSONObject> responseObject = new ArrayList<>();

    private void salvarProcesso(){

    }

    public String find(String page, BuscaProcessoRequestDTO buscaProcessoRequestDTO) throws JSONException {
        if(buscaProcessoRequestDTO.getClasseCodigo() == null && buscaProcessoRequestDTO.getOrgaoJulgadorCodigo() == null){
            return findByNumero(buscaProcessoRequestDTO.getNumeroProcesso());
        }
        return findByClasseEOrgao(page, buscaProcessoRequestDTO.getClasseCodigo(), buscaProcessoRequestDTO.getOrgaoJulgadorCodigo());
    }


    public String findByClasseEOrgao(String page, Integer classeCodigo, Integer orgaoJulgadorCodigo) throws JSONException {
        if(Objects.equals(page, "1")){
            return searchFirstPage(classeCodigo, orgaoJulgadorCodigo);
        }
        return searchOthersPages(classeCodigo, orgaoJulgadorCodigo);
    }

    public String findByNumero(String numeroProcesso) throws JSONException {
        String jsonString = ProcessoJsonBuilderUtil.getJsonString(numeroProcesso);
        logger.info("Request formatado: " + jsonString);
        try {
            for(String url : urls) {
                var request = HttpRequest.newBuilder()
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .uri(new URI(url))
                        .build();

                /*
                var request = HttpRequest.newBuilder()
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .GET()
                        .uri(new URI(url + "?_id=" + numeroProcesso))
                        .build();
                */
                System.out.println("REQUISIÇÃO: " + request.toString());

                var response = HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());
                logger.info("RESPOSTA BODY: " + response.body());

                if(checkStatusErro(response)){
                    responseObject.addAll(getHits(response.body().toString()));
                }
            }
            return responseObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String searchFirstPage(Integer classeCodigo, Integer orgaoJulgadorCodigo) throws JSONException {
        String jsonString = ProcessoJsonBuilderUtil.getJsonString(classeCodigo, orgaoJulgadorCodigo, size);
        logger.info("Request formatado: " + jsonString);
        try {
            for(String url : urls){
                logger.info("URL: " + url);
                var request = HttpRequest.newBuilder()
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .uri(new URI(url))
                        .build();

                var response = HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());
                logger.info(response.body());

                if(checkStatusErro(response)){
                    responseObject.addAll(getHits(response.body().toString()));
                }
            }
            return responseObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



    private String searchOthersPages(Integer classeCodigo, Integer orgaoJulgadorCodigo) throws JSONException {
        String jsonString = ProcessoJsonBuilderUtil.getJsonString(classeCodigo, orgaoJulgadorCodigo, size, sortValue);
        logger.info("Request formatado: " + jsonString);
        try {
            for(String url : urls){
                var request = HttpRequest.newBuilder()
                        .header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .header(HttpHeaders.AUTHORIZATION, authorization)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .uri(new URI(url))
                        .build();

                var response = HttpClient.newHttpClient()
                        .send(request, HttpResponse.BodyHandlers.ofString());
                logger.info(response.body());

                if(checkStatusErro(response)){
                    responseObject.addAll(getHits(response.body().toString()));
                }
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
        JSONArray hitsArray = new JSONArray();
        hitsArray = hitsObj.getJSONArray("hits");
        JSONObject totalObj = hitsObj.getJSONObject("total");

        int total = totalObj.getInt("value");
        String relation = totalObj.getString("relation");

        ArrayList<JSONObject> response = new ArrayList<>();
        logger.info("total = " + total);
        logger.info("relation = " + relation);
        logger.info(String.valueOf(!relation.equalsIgnoreCase("eq")));

        if (hitsArray.length() == 0) {
            return response;
        }
        for(int i = 0; i < hitsArray.length(); i++){
            response.add(hitsArray.getJSONObject(i));

            // Se chegar ao limite da página = 10, pego o valor de Sort
            if(hitsArray.length() == size){
                /*if(i == hitsArray.length()-1){
                    JSONArray temp = hitsArray.getJSONObject(i).getJSONArray("sort");
                    sortValue = (Long) temp.get(0);
                    logger.info("Valor Sort: " + temp.get(0));
                }*/
            }
        }
        return response;
    }

    private boolean checkStatusErro(HttpResponse<String> code) throws Exception {
        if (code.statusCode() !=  HttpStatus.OK.value() || code.toString().isEmpty()){
            if(code.statusCode() == HttpStatus.SERVICE_UNAVAILABLE.value()){
                throw new UnavailableException("Serviço temporariamente indisponível.");
            }
            if (code.statusCode() == HttpStatus.UNAUTHORIZED.value()) {
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Não autorizado: verifique suas credenciais.");
            }
            if (code.statusCode() == HttpStatus.FORBIDDEN.value()) {
                throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "Proibido: você não tem permissão para acessar este recurso.");
            }
            if (code.statusCode() == HttpStatus.BAD_REQUEST.value()) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Requisição inválida: verifique a sintaxe da sua requisição.");
            }
            if (code.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.");
            }
            if (code.statusCode() == HttpStatus.NOT_FOUND.value()) {
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Registro não encontrado.");
            }
            throw new RuntimeException("Erro: " + code.body());
        } else {
            return true;
        }
    }

    @Transactional
    public void delete(String id){
        Optional<Processo> processo = repository.findById(id);
        if(processo.isPresent()){
            repository.delete(processo.get());
        } else {
            throw new EntityNotFoundException("Não foi possível deletar processo!");
        }
    }

}
