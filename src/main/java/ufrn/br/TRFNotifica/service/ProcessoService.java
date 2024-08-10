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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.dto.BuscaProcessoRequestDTO;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.mapper.ProcessoMapper;
import ufrn.br.TRFNotifica.model.*;
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

    private final List<String> urls = new ArrayList<>();

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private ProcessoMapper processoMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchedulingService schedulingService;

    @Autowired
    private CredenciaisService credenciaisService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(ProcessoService.class);


    @PostConstruct
    public void init() {
        //urls.add(urlDbJsonServer);
        urls.addAll(Arrays.asList(urlTrf1, urlTrf2, urlTrf3, urlTrf4, urlTrf5, urlTrf6));
    }
    private Integer size;
    private Integer nextTrf;
    JSONArray accumulatedResults = new JSONArray();
    int totalResults = 0;

    private JSONObject responseObject = new JSONObject();

    public Optional<Processo> findByIdentificador(String identificador){
        return repository.findByIdentificador(identificador);
    }

    public ResponseEntity save(String username, String processoString) throws JSONException {
        try {
            JSONObject processoJson = new JSONObject(processoString);

            Optional<Credenciais> credenciaisOpt = credenciaisService.findByUsername(username);
            if (credenciaisOpt.isEmpty()) {
                throw new EntityNotFoundException("Credenciais não encontradas para o usuário: " + username);
            }

            Credenciais credenciais = credenciaisOpt.get();
            Usuario usuario = usuarioService.findById(credenciais.getUsuario().getId());
            if (usuario == null) {
                throw new EntityNotFoundException("Usuário não encontrado.");
            }

            ProcessoRequestDTO dto = mapper.readValue(processoJson.toString(), ProcessoRequestDTO.class);
            Processo processo = processoMapper.toProcesso(dto);
            this.salvarProcesso(usuario, processo);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            String errorMessage = "Recurso não encontrado: " + e.getMessage();
            logger.error(errorMessage);
            throw new EntityNotFoundException(errorMessage, e);
        } catch (JSONException e) {
            String errorMessage = "Erro inesperado relacionado a um objeto JSON: " + e.getMessage();
            logger.error(errorMessage);
            throw new JSONException(errorMessage);
        } catch (Exception e) {
            String errorMessage = "Erro inesperado: " + e.getMessage();
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Transactional
    public void salvarProcesso(Usuario usuario, Processo processo) {
        try {
            // Verifica se o processo existe no banco de dados
            Processo processoExisteBD = verificarProcessoExisteBD(processo);

            if(processoExisteBD != null){
                Optional<Notificacao> notificacaoExiste = notificacaoService.findByUsuarioIdAndProcessoId(usuario.getId(), processoExisteBD.getId());
                // Se o processo existe no banco, mas nao existe a notificacao, cria apenas a notificacao
                if (notificacaoExiste.isEmpty()) {
                    notificacaoService.createAndSave(usuario, processoExisteBD);
                }
            } else {
                // Se nem o processo existe, entao deve-se criar o processo e a notificacao
                Processo processoSalvo = repository.save(processo);
                notificacaoService.createAndSave(usuario, processoSalvo);
            }
        } catch (Exception e) {
            String errorMessage = "Erro ao salvar o processo: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


    public Processo verificarProcessoExisteBD(Processo processo){
        //List<Processo> processoList = repository.findAll();
        Optional<Processo> processoBd = repository.findByIdentificador(processo.getIdentificador());
        //for(Processo processoBD : processoList){
            //if(processoBD.getIdentificador().equalsIgnoreCase(processo.getIdentificador())){
                // O processo existe no BD, mas ele foi atualizado na API
                //Instant instantProcesso = Instant.parse(processo.getDataHoraUltimaAtualizacao());
                //LocalDateTime dateTimeProcesso = LocalDateTime.ofInstant(instantProcesso, ZoneId.systemDefault());
                //Instant instantProcessoBD = Instant.parse(processoBD.getDataHoraUltimaAtualizacao());
                //LocalDateTime dateTimeProcessoBD = LocalDateTime.ofInstant(instantProcessoBD, ZoneId.systemDefault());
        if(processoBd.isPresent()){
            System.out.println("PROCESSO EXISTE NO BANCO DE DADOS");
            Movimentacao ultimaMovimentacaoApi = processo.getMovimentacoes().get(processo.getMovimentacoes().size()-1);
            Movimentacao ultimaMovimentacaoBd = processoBd.get().getMovimentacoes().get(processoBd.get().getMovimentacoes().size()-1);

            // Verificando se o timestamp do processoAPI foi atualizado na API, caso sim devo atualizar no meu processo local
            Instant instantUltimaMovimentacaoBd = Instant.parse(ultimaMovimentacaoBd.getDataHora());//processo.getDataHoraUltimaAtualizacao()
            LocalDateTime dateTimeUltimaMovimentacaoBd = LocalDateTime.ofInstant(instantUltimaMovimentacaoBd, ZoneId.systemDefault());

            Instant instantUltimaMovimentacaoApi = Instant.parse(ultimaMovimentacaoApi.getDataHora());//processoApi.getDataHoraUltimaAtualizacao()
            LocalDateTime dateTimeUltimaMovimentacaoApi = LocalDateTime.ofInstant(instantUltimaMovimentacaoApi, ZoneId.systemDefault());

            // Houve atualizacao no processo - timestamp
            if(dateTimeUltimaMovimentacaoApi.isAfter(dateTimeUltimaMovimentacaoBd)){
                System.out.println("PROCESSO FOI ATUALIZADO NA API");
                processo.setId(processoBd.get().getId());
                Optional<Processo> processoAtualizado = processoMapper.copyData(processo, processoBd.get());
                processoAtualizado.ifPresent(value -> repository.save(value));

                // Houve novas movimentações no processo
                if(processo.getMovimentacoes().size() > processoBd.get().getMovimentacoes().size()){
                    logger.info("Novas movimentações foram detectadas no processo: " + processo.getIdentificador());
                    logger.info("Enviando e-mails sobre as novas movimentações...");
                    List<Movimentacao> novasMovimentacoesList = new ArrayList<>();
                    // Otimizar, tentar buscar do banco para não usar o size - eliminar FOR
                    for (int i = processoBd.get().getMovimentacoes().size(); i < processo.getMovimentacoes().size(); i++) {
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

            } else {
                System.out.println("RETORNANDO PROCESSO DO BD");
                return processoBd.get();
            }
        }
        //}
        System.out.println("PROCESSO NÃO EXISTE NO BD");
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
        JSONArray accumulatedResults = new JSONArray();
        JSONObject responseObject = new JSONObject();
        this.totalResults = 0;

        while (this.nextTrf < 7) {
            String url = "";
            switch (this.nextTrf) {
                case 1: url = urlTrf1; break;
                case 2: url = urlTrf2; break;
                case 3: url = urlTrf3; break;
                case 4: url = urlTrf4; break;
                case 5: url = urlTrf5; break;
                case 6: url = urlTrf6; break;
            }

            try {
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
                    JSONArray data = responseObject.getJSONArray("data");

                    // Adiciona os resultados acumulados
                    for (int i = 0; i < data.length(); i++) {
                        accumulatedResults.put(data.get(i));
                    }

                    // Se a quantidade de resultados acumulados for igual ao size, retorna
                    if (data.length() == size) {
                        this.accumulatedResults = new JSONArray();
                        this.totalResults = 0;
                        responseObject.put("data", accumulatedResults);
                        return responseObject.toString();
                    }

                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        this.accumulatedResults = new JSONArray();
        this.nextTrf = 1;
        this.totalResults = 0;
        responseObject.put("data", accumulatedResults);
        return responseObject.toString();
    }


    public JSONObject getData(String responseBody) {
        JSONObject result = new JSONObject();

        try {
            // Extraindo os processos do array hits do objeto hits
            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode arrayHits = responseJson.path("hits").path("hits");
            String jsonString = objectMapper.writeValueAsString(arrayHits);
            JSONArray jsonArray = new JSONArray(jsonString);

            result.put("data", jsonArray);

            // Caso o tamanho do array seja menor que o size da pagina: incremento nextTrf para buscar no TRF seguinte
            if (jsonArray.length() < size || jsonArray.length() == 0) {
                this.nextTrf++;
                totalResults += responseJson.path("hits").path("total").path("value").asInt();
            } else {
                totalResults = responseJson.path("hits").path("total").path("value").asInt();
            }

            result.put("totalResults", totalResults);
            // Retorno em qual TRF a busca deve continuar
            result.put("nextTrf", this.nextTrf);
            // Caso haja 10 itens na página retornada, pego o sortValue para continuar a busca paginada
            if (jsonArray.length() == size) {
                JSONArray tempArray = jsonArray.getJSONObject(jsonArray.length() - 1).getJSONArray("sort");
                long sortValue = tempArray.getLong(0);
                result.put("sortValue", sortValue);
            }

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
    public ResponseEntity delete(String username, String identificador){
        try {
            Optional<Credenciais> credenciais = credenciaisService.findByUsername(username);
            Optional<Notificacao> notificacao;
            Optional<Processo> processoEncontrado;
            Usuario usuario = null;
            if(credenciais.isPresent()){
                usuario = usuarioService.findById(credenciais.get().getId());
            }
            if(usuario != null){
                processoEncontrado = this.findByIdentificador(identificador);
                if(processoEncontrado.isPresent()){
                    notificacao = notificacaoService.findByUsuarioIdAndProcessoId(usuario.getId(), processoEncontrado.get().getId());
                    if(notificacao.isPresent()){
                        notificacaoService.delete(notificacao.get().getId());
                        List<Notificacao> notificacaoProcessoList = notificacaoService.findByProcessoId(processoEncontrado.get().getId());
                        if(notificacaoProcessoList.isEmpty()){
                            Optional<Processo> processo = repository.findById(processoEncontrado.get().getId());
                            processo.ifPresent(value -> repository.delete(value));
                        }
                    }
                }
            }
            return ResponseEntity.noContent().build();
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


    public List<Processo> findProcessosByUsuario(String username){
        try{
            Usuario usuario = null;
            Optional<Credenciais> credenciais = credenciaisService.findByUsername(username);
            if(credenciais.isPresent()){
                usuario = usuarioService.findById(credenciais.get().getUsuario() .getId());
            }
            if(usuario != null){
                return this.findProcessosByUsuarioId(usuario.getId());
            }
            return null;
        }catch (Exception e){
            String errorMessage = "Erro ao buscar processos do usuário: " + e.getMessage();
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
