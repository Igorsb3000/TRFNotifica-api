package ufrn.br.TRFNotifica.messages;

import ufrn.br.TRFNotifica.model.Assunto;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.util.ProcessoJsonBuilderUtil;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmailMessages {
    public static String messageToNewMovement(Movimentacao movimentacao, String nameUser) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(movimentacao.getDataHora());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String formattedDate = zonedDateTime.format(dateFormatter);
        String formattedTime = zonedDateTime.format(timeFormatter);

        List<Assunto> assuntos = movimentacao.getProcesso().getAssuntos();
        StringBuilder assuntosBuilder = new StringBuilder();

        for (int i = 0; i < assuntos.size(); i++) {
            Assunto assunto = assuntos.get(i);
            if (i == assuntos.size() - 1) {
                // Último assunto
                assuntosBuilder.append("\n- ").append(assunto.getCodigo()).append(": ").append(assunto.getNome()).append(".");
            } else {
                // Assuntos anteriores
                assuntosBuilder.append("\n- ").append(assunto.getCodigo()).append(": ").append(assunto.getNome()).append(";");
            }
        }

        return "OLÁ, " + nameUser.toUpperCase() + "!\n\n"
                + "Percebemos que houve uma nova movimentação no processo de número " + ProcessoJsonBuilderUtil.formatProcessNumber(movimentacao.getProcesso().getNumero())
                + ", cujos assuntos são: " + assuntosBuilder
                + "\n\n"
                + "DETALHES DA MOVIMENTAÇÃO:\n"
                + "► Código: " + movimentacao.getCodigo() + ";\n"
                + "► Nome: " + movimentacao.getNome() + ";\n"
                + "► Data: " + formattedDate + ";\n"
                + "► Hora: " + formattedTime + ".";
    }

}
