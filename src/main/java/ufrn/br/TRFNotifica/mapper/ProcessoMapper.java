package ufrn.br.TRFNotifica.mapper;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO.Movimento;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.dto.ProcessoResponseDTO;
import ufrn.br.TRFNotifica.model.Assunto;
import ufrn.br.TRFNotifica.model.Movimentacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.repository.AssuntoRepository;
import ufrn.br.TRFNotifica.repository.ProcessoRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@AllArgsConstructor
public class ProcessoMapper {
    private final ModelMapper mapper;

    @Autowired
    private ProcessoRepository repository;

    @Autowired
    private AssuntoRepository assuntoRepository;
    /**
     * Converte um ProcessoRequestDTO em Processo
     * @param dto
     * @return processo
     */
    public Processo toProcesso(ProcessoRequestDTO dto){
        Processo processo = new Processo();
        List<Assunto> assuntos = new ArrayList<>();
        List<Movimentacao> movimentacoes = new ArrayList<>();

        if(dto.getNumeroProcesso() != null){
            processo.setNumero(dto.getNumeroProcesso());
        }

        if(dto.getGrau() != null){
            processo.setGrau(dto.getGrau());
        }

        if(dto.getId() != null){
            processo.setIdentificador(dto.getId());
        }

        if(dto.getTribunal() != null){
            processo.setTribunal(dto.getTribunal());
        }

        if(dto.getDataAjuizamento() != null){
            processo.setDataAjuizamento(dto.getDataAjuizamento());
        }
        //processo.setDataHoraUltimaAtualizacao(dto.getDataHoraUltimaAtualizacao());

        if (dto.getClasse() != null) {
            processo.setClasseCodigo(dto.getClasse().getCodigo());
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
            processo.setOrgaoJulgadorCodigo(dto.getOrgaoJulgador().getCodigo());
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
            processo.setMovimentacoes(movimentacoes);
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


    public Optional<Processo> copyData(Processo origem, Processo destino) {
        // Atualizando movimentações existentes, caso seja detectado alteração no atributo DataHora
        for(int i = 0; i < destino.getMovimentacoes().size(); i++){
            // API
            Instant instantProcessoOrigem = Instant.parse(origem.getMovimentacoes().get(i).getDataHora());
            LocalDateTime dateTimeProcessoOrigem = LocalDateTime.ofInstant(instantProcessoOrigem, ZoneId.systemDefault());

            // BD
            Instant instantProcessoDestino = Instant.parse(destino.getMovimentacoes().get(i).getDataHora());
            LocalDateTime dateTimeProcessoDestino = LocalDateTime.ofInstant(instantProcessoDestino, ZoneId.systemDefault());

            if(dateTimeProcessoOrigem.isAfter(dateTimeProcessoDestino)){
                destino.getMovimentacoes().get(i).setDataHora(origem.getMovimentacoes().get(i).getDataHora());
                destino.getMovimentacoes().get(i).setCodigo(origem.getMovimentacoes().get(i).getCodigo());
                destino.getMovimentacoes().get(i).setNome(origem.getMovimentacoes().get(i).getNome());
            }
        }


        // caso 1: o assunto não existe no BD -> acrescentar
        // caso 2: o assunto existe no BD -> não fazer nada
        // 6 - 4 = 2
        // 6-2 = 4
        if(origem.getAssuntos().size() > destino.getAssuntos().size()){
            List<Assunto> novosAssuntosList = new ArrayList<>();
            int diffAssuntos = origem.getAssuntos().size() - destino.getAssuntos().size();

            for(int i = origem.getAssuntos().size() - diffAssuntos; i < origem.getAssuntos().size(); i++){
                Assunto novoAssunto = new Assunto();
                novoAssunto.setCodigo(origem.getAssuntos().get(i).getCodigo());
                novoAssunto.setNome(origem.getAssuntos().get(i).getNome());
                novoAssunto.setProcesso(destino);
                novosAssuntosList.add(novoAssunto);
            }

            if(!novosAssuntosList.isEmpty()){
                destino.setAssuntos(novosAssuntosList);
            }
        }

        if(origem.getMovimentacoes().size() > destino.getMovimentacoes().size()){
            List<Movimentacao> novasMovimentacoesList = new ArrayList<>();
            int diffMovimentacoes = origem.getMovimentacoes().size() - destino.getMovimentacoes().size();

            for(int i = origem.getMovimentacoes().size() - diffMovimentacoes; i < origem.getMovimentacoes().size(); i++){
                Movimentacao movimentacao = new Movimentacao();
                movimentacao.setCodigo(origem.getMovimentacoes().get(i).getCodigo());
                movimentacao.setNome(origem.getMovimentacoes().get(i).getNome());
                movimentacao.setDataHora(origem.getMovimentacoes().get(i).getDataHora());
                movimentacao.setProcesso(destino);
                novasMovimentacoesList.add(movimentacao);
            }

            if(!novasMovimentacoesList.isEmpty()){
                destino.setMovimentacoes(novasMovimentacoesList);
            }
        }

        if(!Objects.equals(destino.getTribunal(), origem.getTribunal())){
            destino.setTribunal(origem.getTribunal());
        }
        if(!Objects.equals(destino.getSistemaNome(), origem.getSistemaNome())){
            destino.setSistemaNome(origem.getSistemaNome());
        }
        if(!Objects.equals(destino.getDataAjuizamento(), origem.getDataAjuizamento())){
            destino.setDataAjuizamento(origem.getDataAjuizamento());
        }

        return Optional.of(destino);
    }

}
