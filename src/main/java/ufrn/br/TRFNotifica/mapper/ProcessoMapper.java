package ufrn.br.TRFNotifica.mapper;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO.Movimento;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.dto.ProcessoResponseDTO;
import ufrn.br.TRFNotifica.model.Assunto;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.model.Processo;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class ProcessoMapper {
    private final ModelMapper mapper;

    /**
     * Converte um ProcessoRequestDTO em Processo
     * @param dto
     * @return processo
     */
    public Processo toProjeto(ProcessoRequestDTO dto){
        Processo processo = new Processo();
        List<Assunto> assuntos = new ArrayList<>();
        List<Movimentacao> movimentacoes = new ArrayList<>();

        processo.setNumero(dto.getNumeroProcesso());
        processo.setGrau(dto.getGrau());
        processo.setIdentificador(dto.getId());
        processo.setTribunal(dto.getTribunal());
        processo.setTimestamp(dto.getTimestamp());
        processo.setDataAjuizamento(dto.getDataAjuizamento());

        if (dto.getClasse() != null) {
            processo.setClasseNome(dto.getClasse().getNome());
        }
        if(dto.getAssuntos() != null){
            for(ProcessoRequestDTO.Assunto assunto : dto.getAssuntos()){
                Assunto assuntoTemp = new Assunto(assunto.getCodigo(), assunto.getNome(), processo);
                assuntos.add(assuntoTemp);
            }
            processo.setAssuntos(assuntos);
        }

        if (dto.getSistema() != null) {
            processo.setSistemaNome(dto.getSistema().getNome());
        }

        if (dto.getOrgaoJulgador() != null) {
            processo.setOrgaoJulgadorNome(dto.getOrgaoJulgador().getNome());
        }

        if (dto.getMovimentos() != null && !dto.getMovimentos().isEmpty()) {
            //Movimento movimento = dto.getMovimentos().get(dto.getMovimentos().size()-1);
            for(Movimento movimento : dto.getMovimentos()) {
                Movimentacao movimentacao = new Movimentacao();

                movimentacao.setNome(movimento.getNome());
                movimentacao.setCodigo(movimento.getCodigo());
                movimentacao.setDataHora(movimento.getDataHora());
                movimentacao.setProcesso(processo);
                movimentacoes.add(movimentacao);
            }
            processo.setMovimentacaos(movimentacoes);
        }

        return processo;
    }


    /**
     * Converte um Processo em ProcessoResponseDTO
     * @param processo
     * @return processoResponseDTO
     */
    public ProcessoResponseDTO toProcessoResponseDTO(Processo processo){
        ProcessoResponseDTO processoResponseDTO = mapper.map(processo, ProcessoResponseDTO.class);
        return processoResponseDTO;
    }

}
