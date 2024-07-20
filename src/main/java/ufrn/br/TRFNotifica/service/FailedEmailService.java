package ufrn.br.TRFNotifica.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.model.FailedEmail;
import ufrn.br.TRFNotifica.repository.FailedEmailRepository;

import java.util.List;

@Service
public class FailedEmailService {
    @Autowired
    private FailedEmailRepository repository;

    @Autowired
    private MailService mailService;
    private final Logger logger = LoggerFactory.getLogger(FailedEmailService.class);


    public void saveFailedEmail(String email, String subject, String body) {
        try {
            // Validação dos parâmetros de entrada
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("O email não pode ser nulo ou vazio.");
            }
            if (subject == null || subject.isEmpty()) {
                throw new IllegalArgumentException("O assunto não pode ser nulo ou vazio.");
            }
            if (body == null || body.isEmpty()) {
                throw new IllegalArgumentException("A mensagem não pode ser nula ou vazia.");
            }

            // Criação do objeto FailedEmail e configuração de seus campos
            FailedEmail failedEmail = new FailedEmail();
            failedEmail.setEmail(email);
            failedEmail.setTitulo(subject);
            failedEmail.setMensagem(body);
            repository.save(failedEmail);
        } catch (IllegalArgumentException e) {
            String errorMessage = "Erro de validação ao salvar email falhado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Erro inesperado ao salvar email falhado: " + e.getMessage();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @Scheduled(cron = "0 */2 * * * *") // A cada 2 minutos
    public void retryFailedEmails() {
        List<FailedEmail> failedEmails = repository.findAll();

        if (failedEmails.isEmpty()) {
            logger.info("Nenhum e-mail falhado para reenviar.");
            return;
        }

        for (FailedEmail failedEmail : failedEmails) {
            try {
                mailService.sendSimpleMessage(failedEmail.getEmail(),
                        failedEmail.getTitulo(), failedEmail.getMensagem());
                repository.delete(failedEmail);
            } catch (Exception e) {
                String errorMessage = "Erro ao reenviar e-mail para: " + failedEmail.getEmail() + " - " + e.getMessage();
                logger.error(errorMessage, e);
                throw new RuntimeException(errorMessage, e);
            }
        }
    }

}
