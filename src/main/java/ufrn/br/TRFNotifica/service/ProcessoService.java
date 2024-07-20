package ufrn.br.TRFNotifica.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ufrn.br.TRFNotifica.mapper.ProcessoMapper;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;
import ufrn.br.TRFNotifica.util.ProcessoJsonBuilderUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    private final String urlDbJsonServer = "http://localhost:3000/hits";
    private final List<String> urls = new ArrayList<>();

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ProcessoMapper processoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchedulingService schedulingService;

    private static final Logger logger = LoggerFactory.getLogger(ProcessoService.class);


    @PostConstruct
    public void init() {
        //urls.add(urlDbJsonServer);
        urls.addAll(Arrays.asList(urlTrf1, urlTrf2, urlTrf3, urlTrf4, urlTrf5, urlTrf6));
    }
    private Integer size;

    private Integer nextTrf;
    //private Long sortValue = null;
    private final ArrayList<JSONObject> responseArray = new ArrayList<>();

    private JSONObject responseObject = new JSONObject();

    public Optional<Processo> findByIdentificador(String identificador){
        return repository.findByIdentificador(identificador);
    }

    public Processo salvarProcesso(Usuario usuario, Processo processo){
        // Processo já foi atualizado
        Processo processoExisteBD = verificarProcessoExisteBD(processo);
        // Verifica se existe notificacao entre processo e usuario
        Optional<Notificacao> notificacaoExiste = notificacaoService.findByUsuarioIdAndProcessoId(usuario.getId(), processo.getId());

        // Se o processo nao existe no banco, salvo o processo e crio a notificacao
        if(processoExisteBD == null){ // && notificacaoExiste.isEmpty()
            Processo processoSalvo = repository.save(processo);
            notificacaoService.createAndSave(usuario, processo);
            return processoSalvo;
            // Se o processo existe no BD, mas nao existe notificacao entre processo e usuario
        }
        // Se o processo existe e nao existe a notificacao, crio a notificacao apenas
        if(notificacaoExiste.isEmpty()){
            notificacaoService.createAndSave(usuario, processoExisteBD);
            return processoExisteBD;
        }
        // Se o processo e a notificacao existem, nao precisa fazer nada
        return processoExisteBD;
    }

    public Processo verificarProcessoExisteBD(Processo processo){
        List<Processo> processoList = repository.findAll();
        for(Processo processoBD : processoList){
            if(processoBD.getIdentificador().equalsIgnoreCase(processo.getIdentificador())){
                // O processo existe no BD, mas ele foi atualizado na API
                //Instant instantProcesso = Instant.parse(processo.getDataHoraUltimaAtualizacao());
                //LocalDateTime dateTimeProcesso = LocalDateTime.ofInstant(instantProcesso, ZoneId.systemDefault());
                //Instant instantProcessoBD = Instant.parse(processoBD.getDataHoraUltimaAtualizacao());
                //LocalDateTime dateTimeProcessoBD = LocalDateTime.ofInstant(instantProcessoBD, ZoneId.systemDefault());

                Movimentacao ultimaMovimentacaoApi = processo.getMovimentacoes().get(processo.getMovimentacoes().size()-1);
                Movimentacao ultimaMovimentacaoBd = processoBD.getMovimentacoes().get(processoBD.getMovimentacoes().size()-1);

                // Verificando se o timestamp do processoAPI foi atualizado na API, caso sim devo atualizar no meu processo local
                Instant instantUltimaMovimentacaoBd = Instant.parse(ultimaMovimentacaoBd.getDataHora());//processo.getDataHoraUltimaAtualizacao()
                LocalDateTime dateTimeUltimaMovimentacaoBd = LocalDateTime.ofInstant(instantUltimaMovimentacaoBd, ZoneId.systemDefault());

                Instant instantUltimaMovimentacaoApi = Instant.parse(ultimaMovimentacaoApi.getDataHora());//processoApi.getDataHoraUltimaAtualizacao()
                LocalDateTime dateTimeUltimaMovimentacaoApi = LocalDateTime.ofInstant(instantUltimaMovimentacaoApi, ZoneId.systemDefault());

                // Houve atualização no processo - timestamp
                if(dateTimeUltimaMovimentacaoApi.isAfter(dateTimeUltimaMovimentacaoBd)){
                    processo.setId(processoBD.getId());
                    Optional<Processo> processoAtualizado = processoMapper.copyData(processo, processoBD);
                    processoAtualizado.ifPresent(value -> repository.save(value));

                    // Houve novas movimentações no processo
                    if(processo.getMovimentacoes().size() > processoBD.getMovimentacoes().size()){
                        logger.info("Novas movimentações foram detectadas no processo: " + processo.getIdentificador());
                        // ENVIAR EMAIL
                        logger.info("Enviando e-mails sobre as novas movimentações...");
                        List<Movimentacao> novasMovimentacoesList = new ArrayList<>();
                        for (int i = processoBD.getMovimentacoes().size(); i < processo.getMovimentacoes().size(); i++) {
                            novasMovimentacoesList.add(processo.getMovimentacoes().get(i));
                        }

                        processoAtualizado.ifPresent(value -> {
                            try {
                                schedulingService.sendMail(processoAtualizado.get(), novasMovimentacoesList);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                    if(processoAtualizado.isPresent()){
                        return processoAtualizado.get();
                    }
                }
                return processoBD;
            }
        }
        return null;
    }

    public String find(int size, Long searchAfter, int nextTrf, BuscaProcessoRequestDTO buscaProcessoRequestDTO) throws JSONException, IOException {
        this.size = size;
        this.nextTrf = nextTrf;
        return searchApiPagination(searchAfter, buscaProcessoRequestDTO.getNumeroProcesso(), buscaProcessoRequestDTO.getClasseCodigo(), buscaProcessoRequestDTO.getOrgaoJulgadorCodigo());
    }

    public Processo getByIdentificadorLocal(String identificador) {
        Optional<Processo> processo = repository.findByIdentificador(identificador);
        if (processo.isPresent()) {
            return processo.get();
        } else {
            String errorMessage = "Erro ao buscar processo: Processo com identificador " + identificador + " não encontrado.";
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public String searchByIdentificador(String identificador) throws JSONException {
        String jsonString = ProcessoJsonBuilderUtil.getJsonStringByIdentificador(identificador);
        int firstUnderscoreIndex = identificador.indexOf('_');
        String orgao = identificador.substring(0, firstUnderscoreIndex);
        String url = "";

        switch (orgao){
            case "TRF1":
                url = urlTrf1;
                break;
            case "TRF2":
                url = urlTrf2;
                break;
            case "TRF3":
                url = urlTrf3;
                break;
            case "TRF4":
                url = urlTrf4;
                break;
            case "TRF5":
                url = urlTrf5;
                break;
            case "TRF6":
                url = urlTrf6;
                break;
        }
        try{
            var request = HttpRequest.newBuilder()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                    .uri(new URI(url))
                    .build();
            var response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (checkStatusErro(response)) {
                responseObject = getData(response.body());
                if(responseObject.getJSONArray("data").length() > 0){
                    return responseObject.get("data").toString();
                }
            }

        }catch (Exception e){
            throw new RuntimeException(e);
        }

        return null;
    }

    private String searchApiPagination(Long searchAfter, String numeroProcesso, Integer classeCodigo, Integer orgaoJulgadorCodigo) throws JSONException, IOException {
        String jsonString = ProcessoJsonBuilderUtil.buildQuery(size, searchAfter, numeroProcesso, classeCodigo, orgaoJulgadorCodigo);
        System.out.println("Requisição: " + jsonString);
        // No front:
        // Através do parametro nextTrf saberei qual o proximo TRF que será consultado na proxima busca paginada
        // Através dos objetos "TRF1", "TRF2", etc. saberei de qual TRF vinheram os processos buscados
        // Caso sejam retornados 10 processos que é o limite da página, retornarei o sortValue para ser usado como searchAfter na proxima busca,
        // para assim garantir a continuação da busca a partir do ultimo processo retornado anteriormente.

        while(this.nextTrf < 7){
            switch (this.nextTrf){
                case 1:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf1))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }

                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case 2:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf2))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                case 3:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf3))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case 4:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf4))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case 5:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf5))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case 6:
                    try {
                        var request = HttpRequest.newBuilder()
                                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                                .header(HttpHeaders.AUTHORIZATION, authorization)
                                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                                .uri(new URI(urlTrf6))
                                .build();
                        var response = HttpClient.newHttpClient()
                                .send(request, HttpResponse.BodyHandlers.ofString());

                        if (checkStatusErro(response)) {
                            responseObject = getData(response.body());
                            if(responseObject.getJSONArray("data").length() > 0){
                                return responseObject.toString();
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            }
        }
        this.nextTrf = 1;
        return responseObject.toString();
    }

//    public JSONObject getData(String responseBody) throws Exception {
//        JSONArray jsonArray;
//        JSONObject result = new JSONObject();
//        JsonNode responseJson, arrayHits;
//        String jsonString;
//        Integer totalResults;
//        Long sortValue;
//
//        // EXTRAIR MÉTODOS
//        // Extraindo os processos do array hits do objeto hits
//        responseJson = objectMapper.readTree(responseBody);
//        arrayHits = responseJson.path("hits").path("hits");
//        totalResults = Integer.valueOf(objectMapper.writeValueAsString(responseJson.path("hits").path("total").path("value")));
//        jsonString = objectMapper.writeValueAsString(arrayHits);
//        jsonArray = new JSONArray(jsonString);
//        result.put("data", jsonArray);
//        result.put("totalResults", totalResults);
//
//        // Caso haja 10 itens na página retornada, pego o sortValue para continuar a busca paginada
//        if(size != null && jsonArray.length() == size){
//            JSONArray tempArray = jsonArray.getJSONObject(jsonArray.length()-1).getJSONArray("sort");
//            sortValue = (Long) tempArray.get(0);
//            result.put("sortValue", sortValue);
//        }
//
//        // Caso o tamanho do array seja menor que o size da pagina: incremento nextTrf para buscar no TRF seguinte
//        if(size != null && jsonArray.length() < size || jsonArray.length() == 0){
//            this.nextTrf++;
//        }
//
//        // Retorno em qual TRF a busca deve continuar
//        result.put("nextTrf", this.nextTrf);
//        return result;
//    }

    public JSONObject getData(String responseBody) {
        JSONObject result = new JSONObject();
        try {
            // Extraindo os processos do array hits do objeto hits
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode arrayHits = responseJson.path("hits").path("hits");
            int totalResults = responseJson.path("hits").path("total").path("value").asInt();
            String jsonString = objectMapper.writeValueAsString(arrayHits);
            JSONArray jsonArray = new JSONArray(jsonString);

            result.put("data", jsonArray);
            result.put("totalResults", totalResults);

            // Caso haja 10 itens na página retornada, pego o sortValue para continuar a busca paginada
            if (size != null && jsonArray.length() == size) {
                JSONArray tempArray = jsonArray.getJSONObject(jsonArray.length() - 1).getJSONArray("sort");
                long sortValue = tempArray.getLong(0);
                result.put("sortValue", sortValue);
            }

            // Caso o tamanho do array seja menor que o size da pagina: incremento nextTrf para buscar no TRF seguinte
            if (size != null && (jsonArray.length() < size || jsonArray.length() == 0)) {
                this.nextTrf++;
            }

            // Retorno em qual TRF a busca deve continuar
            result.put("nextTrf", this.nextTrf);
        } catch (JsonProcessingException e) {
            String errorMessage = "Erro ao processar o JSON: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } catch (JSONException e) {
            String errorMessage = "Erro ao manipular o JSON: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        return result;
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
        try {
            Optional<Processo> processo = repository.findById(id);
            processo.ifPresent(value -> repository.delete(value));
        } catch (EntityNotFoundException e){
            String errorMessage = "Processo não encontrado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new EntityNotFoundException(errorMessage, e);
        } catch (Exception e){
            String errorMessage = "Erro ao deletar processo: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


    public List<Processo> findProcessosByUsuarioId(String usuarioId){
        try {
            List<Notificacao> notificacaoList = notificacaoService.findByUsuarioId(usuarioId);
            List<Processo> processoList = new ArrayList<>();
            if(!notificacaoList.isEmpty()){
                for(Notificacao n : notificacaoList){
                    String processoId = n.getProcesso().getId();
                    Optional<Processo> p = repository.findById(processoId);
                    p.ifPresent(processoList::add);
                }
            }
            return processoList;
        } catch (EntityNotFoundException e){
            String errorMessage = "Notificação não encontrada: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new EntityNotFoundException(errorMessage, e);
        } catch (Exception e){
            String errorMessage = "Erro ao deletar processo: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


}
