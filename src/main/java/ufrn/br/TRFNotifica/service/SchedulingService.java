package ufrn.br.TRFNotifica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.UnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.mapper.ProcessoMapper;
import ufrn.br.TRFNotifica.messages.EmailMessages;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.repository.NotificacaoRepository;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class SchedulingService {
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

    @Autowired
    private ProcessoMapper processoMapper;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private ProcessoRepository processoRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private FailedEmailService failedEmailService;

    @Value("${task.scheduling.cron}")
    private static String FIRST_DAY_MOUNTH;// Dia 01 de cada mes, as 18:00 horas

    private static final String EVERY_THIRTY_SECONDS = "*/30 * * * * *";

    private final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    String url = "http://localhost:3000/hits";


    /*
    @Scheduled(cron = EVERY_THIRTY_SECONDS)
    public void buscarProcessos() throws Exception {
        List<Processo> processosList = processoRepository.findAll();

        if(!processosList.isEmpty()){
            for(Processo processo : processosList){
                Processo processoApi = new Processo();
                int quantidadeMovimentacoesProcessoBd = processo.getMovimentacoes().size();

                if(processo.getTribunal().equalsIgnoreCase("TRF1")){
                    logger.info("Enviando requisição ao TRF1");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf1);
                } else if(processo.getTribunal().equalsIgnoreCase("TRF2")){
                    logger.info("Enviando requisição ao TRF2");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf2);
                } else if(processo.getTribunal().equalsIgnoreCase("TRF3")){
                    logger.info("Enviando requisição ao TRF3");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf3);
                } else if(processo.getTribunal().equalsIgnoreCase("TRF4")){
                    logger.info("Enviando requisição ao TRF4");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf4);
                } else if(processo.getTribunal().equalsIgnoreCase("TRF5")){
                    logger.info("Enviando requisição ao TRF5");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf5);
                } else if(processo.getTribunal().equalsIgnoreCase("TRF6")){
                    logger.info("Enviando requisição ao TRF6");
                    // sendRequestToApi(processo.getIdentificador(), urlTrf6);
                }

                var response = sendRequestToApi(processo.getIdentificador(), url);

                if(checkStatusErro(response)){
                    JSONObject responseProcesso = getProcess(response.body());
                    // Para versão API DataJud: JSONObject processoJson = new JSONObject(responseProcesso.toString());
                    JSONObject source = responseProcesso.getJSONObject("_source");
                    ProcessoRequestDTO dto = mapper.readValue(source.toString(), ProcessoRequestDTO.class);
                    processoApi = processoMapper.toProcesso(dto);
                }
                if(processoApi.getMovimentacoes() != null){
                    Movimentacao ultimaMovimentacaoApi = processoApi.getMovimentacoes().get(processoApi.getMovimentacoes().size()-1);
                    Movimentacao ultimaMovimentacaoBd = processo.getMovimentacoes().get(processo.getMovimentacoes().size()-1);

                    // Verificando se o timestamp do processoAPI foi atualizado na API, caso sim devo atualizar no meu processo local
                    Instant instantUltimaMovimentacaoBd = Instant.parse(ultimaMovimentacaoBd.getDataHora());//processo.getDataHoraUltimaAtualizacao()
                    LocalDateTime dateTimeUltimaMovimentacaoBd = LocalDateTime.ofInstant(instantUltimaMovimentacaoBd, ZoneId.systemDefault());

                    Instant instantUltimaMovimentacaoApi = Instant.parse(ultimaMovimentacaoApi.getDataHora());//processoApi.getDataHoraUltimaAtualizacao()
                    LocalDateTime dateTimeUltimaMovimentacaoApi = LocalDateTime.ofInstant(instantUltimaMovimentacaoApi, ZoneId.systemDefault());

                    // Houve atualização no processo - timestamp
                    if(dateTimeUltimaMovimentacaoApi.isAfter(dateTimeUltimaMovimentacaoBd)){
                        logger.info("Atualizando processo do BD...");
                        Optional<Processo> processoAtualizado = processoMapper.copyData(processoApi, processo);
                        processoAtualizado.ifPresent(value -> processoRepository.save(value));

                        if(processoApi.getMovimentacoes().size() > quantidadeMovimentacoesProcessoBd){
                            List<Movimentacao> novasMovimentacoesList = new ArrayList<>();
                            int i = quantidadeMovimentacoesProcessoBd;

                            while(i < processoApi.getMovimentacoes().size()){
                                novasMovimentacoesList.add(processoApi.getMovimentacoes().get(i));
                                i++;
                            }

                            processoAtualizado.ifPresent(value -> sendMail(processoAtualizado.get(), novasMovimentacoesList));
                        }

                    } else {
                        logger.info("O processo com identificador: " + processo.getIdentificador() + " não tem atualização!");
                    }
                }


            }
        } else {
            logger.info("Não existem processos na base de dados. Logo, não poderei buscar por atualizações.");
        }
    }
    */

    @Scheduled(cron = EVERY_THIRTY_SECONDS)
    public void buscarProcessos() {
        try {
            int minInterval = 1000;  // 1 segundo
            int maxInterval = 10000; // 10 segundos
            Random random = new Random();
            int waitTime;
            List<Processo> processosList = processoRepository.findAll();

            if (processosList.isEmpty()) {
                logger.info("Não existem processos na base de dados. Logo, não poderei buscar por atualizações.");
                return;
            }

            for (Processo processo : processosList) {
                try {
                    Processo processoApi = new Processo();
                    int quantidadeMovimentacoesProcessoBd = processo.getMovimentacoes().size();

                    String tribunal = processo.getTribunal().toUpperCase();
                    switch (tribunal) {
                        case "TRF1":
                            logger.info("Enviando requisição ao TRF1");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf1);
                            // break;
                        case "TRF2":
                            logger.info("Enviando requisição ao TRF2");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf2);
                            // break;
                        case "TRF3":
                            logger.info("Enviando requisição ao TRF3");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf3);
                            // break;
                        case "TRF4":
                            logger.info("Enviando requisição ao TRF4");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf4);
                            // break;
                        case "TRF5":
                            logger.info("Enviando requisição ao TRF5");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf5);
                            // break;
                        case "TRF6":
                            logger.info("Enviando requisição ao TRF6");
                            // sendRequestToApi(processo.getIdentificador(), urlTrf6);
                            // break;
                    }

                    var response = sendRequestToApi(processo.getIdentificador(), url);

                    if (checkStatusErro(response)) {
                        JSONObject responseProcesso = getProcess(response.body());
                        JSONObject source;
                        ProcessoRequestDTO dto;
                        if(responseProcesso.getJSONObject("_source") != null){
                            source = responseProcesso.getJSONObject("_source");
                            dto = mapper.readValue(source.toString(), ProcessoRequestDTO.class);
                        } else {
                            dto = mapper.readValue(responseProcesso.toString(), ProcessoRequestDTO.class);
                        }
                        processoApi = processoMapper.toProcesso(dto);
                    }

                    if (processoApi.getMovimentacoes() != null && !processoApi.getMovimentacoes().isEmpty()) {
                        Movimentacao ultimaMovimentacaoApi = processoApi.getMovimentacoes().get(processoApi.getMovimentacoes().size() - 1);
                        Movimentacao ultimaMovimentacaoBd = processo.getMovimentacoes().get(processo.getMovimentacoes().size() - 1);

                        Instant instantUltimaMovimentacaoBd = Instant.parse(ultimaMovimentacaoBd.getDataHora());
                        LocalDateTime dateTimeUltimaMovimentacaoBd = LocalDateTime.ofInstant(instantUltimaMovimentacaoBd, ZoneId.systemDefault());

                        Instant instantUltimaMovimentacaoApi = Instant.parse(ultimaMovimentacaoApi.getDataHora());
                        LocalDateTime dateTimeUltimaMovimentacaoApi = LocalDateTime.ofInstant(instantUltimaMovimentacaoApi, ZoneId.systemDefault());

                        if (dateTimeUltimaMovimentacaoApi.isAfter(dateTimeUltimaMovimentacaoBd)) {
                            logger.info("Alterações detectadas. Atualizando processo no BD...");
                            Optional<Processo> processoAtualizado = processoMapper.copyData(processoApi, processo);
                            processoAtualizado.ifPresent(value -> processoRepository.save(value));

                            if (processoApi.getMovimentacoes().size() > quantidadeMovimentacoesProcessoBd) {
                                List<Movimentacao> novasMovimentacoesList = new ArrayList<>();
                                for (int i = quantidadeMovimentacoesProcessoBd; i < processoApi.getMovimentacoes().size(); i++) {
                                    novasMovimentacoesList.add(processoApi.getMovimentacoes().get(i));
                                }

                                processoAtualizado.ifPresent(value -> {
                                    try {
                                        sendMail(processoAtualizado.get(), novasMovimentacoesList);
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        } else {
                            logger.info("O processo com identificador: " + processo.getIdentificador() + " não tem atualização!");
                        }
                        // Espera o tempo aleatório antes de processar o próximo item
                        waitTime = minInterval + random.nextInt(maxInterval - minInterval + 1);
                        Thread.sleep(waitTime);
                    }
                } catch (Exception e) {
                    String errorMessage = "Erro ao manipular o processo de identificador: " + processo.getIdentificador() + " - " + e.getMessage();
                    logger.error(errorMessage, e);
                    throw new RuntimeException(errorMessage, e);
                }
            }
        } catch (Exception e) {
            String errorMessage = "Erro ao buscar processos na base de dados: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }



//    private void sendMail177777777(Processo processo, List<Movimentacao> novasMovimentacoesList) {
//        logger.info("Enviando emails....");
//        List<Notificacao> notificacoesList = notificacaoRepository.findByProcessoId(processo.getId());
//        Map<String, String> userContactInfoMap = new HashMap<>();
//        String tituloEmail = "Movimentação do Processo nº ";
//        // Buscar emails dos usuarios interessados em receber notificação sobre o processo
//        for(Notificacao notificacao : notificacoesList){
//            //emailsList.add(notificacao.getUsuario().getEmail());
//            String email = notificacao.getUsuario().getEmail();
//            String name = notificacao.getUsuario().getName();
//            userContactInfoMap.put(email, name);
//        }
//        // Enviar a nova movimentacao para todos os e-mails dos usuários interessados
//        for(Movimentacao movimentacao : novasMovimentacoesList){
//            for(Map.Entry<String, String> userContact : userContactInfoMap.entrySet()){
//                try {
//                    mailService.sendSimpleMessage(userContact.getKey(),
//                            tituloEmail + processo.getNumero(),
//                            EmailMessages.messageToNewMovement(movimentacao, userContact.getValue()));
//                } catch (Exception e) {
//                    logger.error("Erro ao enviar e-mail para: " + userContact.getKey(), e);
//                    failedEmailService.saveFailedEmail(userContact.getKey(), tituloEmail + processo.getNumero(),
//                            EmailMessages.messageToNewMovement(movimentacao, userContact.getValue()));
//                }
//                /*mailService.sendSimpleMessage(userContact.getKey(),
//                        "Movimentação do Processo nº " + processo.getNumero(),
//                        EmailMessages.messageToNewMovement(movimentacao, userContact.getValue()));*/
//            }
//        }
//    }



    public void sendMail(Processo processo, List<Movimentacao> novasMovimentacoesList) {
        logger.info("Iniciando o envio dos e-mails...");
        List<Notificacao> notificacoesList = notificacaoRepository.findByProcessoId(processo.getId());
        Map<String, String> userContactInfoMap = buildUserContactInfoMap(notificacoesList);

        for (Movimentacao movimentacao : novasMovimentacoesList) {
            sendMovementNotifications(processo, movimentacao, userContactInfoMap);
        }
    }

    private Map<String, String> buildUserContactInfoMap(List<Notificacao> notificacoesList) {
        Map<String, String> userContactInfoMap = new HashMap<>();
        for (Notificacao notificacao : notificacoesList) {
            String email = notificacao.getUsuario().getEmail();
            String name = notificacao.getUsuario().getName();
            userContactInfoMap.put(email, name);
        }
        return userContactInfoMap;
    }

    private void sendMovementNotifications(Processo processo, Movimentacao movimentacao, Map<String, String> userContactInfoMap) {
        String tituloEmail = "Movimentação do Processo nº " + processo.getNumero();

        for (Map.Entry<String, String> userContact : userContactInfoMap.entrySet()) {
            String recipientEmail = userContact.getKey();
            String recipientName = userContact.getValue();
            String emailContent = EmailMessages.messageToNewMovement(movimentacao, recipientName);

            try {
                mailService.sendSimpleMessage(recipientEmail, tituloEmail, emailContent);
            } catch (Exception e) {
                String errorMessage = "Erro ao enviar e-mail para: " + recipientEmail;
                logger.error(errorMessage, e);
                saveFailedEmail(recipientEmail, tituloEmail, emailContent);
                throw new RuntimeException(errorMessage, e);
            }
        }
    }

    private void saveFailedEmail(String email, String subject, String body) {
        try {
            failedEmailService.saveFailedEmail(email, subject, body);
        } catch (Exception e) {
            String errorMessage = "Erro ao salvar e-mail: " + e;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }


    public JSONObject getProcess(String jsonStringProcess) throws JSONException {
        JSONArray arrayJsonProcess = new JSONArray(jsonStringProcess);
        JSONObject response = new JSONObject();

        if (arrayJsonProcess.length() > 0) {
            response = arrayJsonProcess.getJSONObject(0);
        }
        return response;
    }


    private HttpResponse<String> sendRequestToApi(String processoIdentificador, String url) throws IOException, InterruptedException, URISyntaxException, JSONException {
        /*
        Versão para API DataJud
         */
        /*String jsonString = ProcessoJsonBuilderUtil.getJsonStringByIdentificador(processoIdentificador);
        var request = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .uri(new URI(url))
                .build();
         /*
         Versão para JSON Server
         */
        var request = HttpRequest.newBuilder()
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .GET()
                .uri(new URI(url + "?_id=" + processoIdentificador))
                .build();

        var response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

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
}
