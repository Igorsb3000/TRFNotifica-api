package ufrn.br.TRFNotifica.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ufrn.br.TRFNotifica.config.ApiVersion;
import ufrn.br.TRFNotifica.dto.BuscaProcessoRequestDTO;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.dto.ProcessoResponseDTO;
import ufrn.br.TRFNotifica.mapper.ProcessoMapper;
import ufrn.br.TRFNotifica.model.Credenciais;
import ufrn.br.TRFNotifica.model.Notificacao;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.service.CredenciaisService;
import ufrn.br.TRFNotifica.service.NotificacaoService;
import ufrn.br.TRFNotifica.service.ProcessoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import ufrn.br.TRFNotifica.service.UsuarioService;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    private final CredenciaisService credenciaisService;

    private final UsuarioService usuarioService;

    private final NotificacaoService notificacaoService;

    private final ProcessoMapper processoMapper;

    private final ObjectMapper mapper;

    @PostMapping()// /processos?page=1
    public String getByNumero(@RequestParam (defaultValue = "1") String page, @RequestBody BuscaProcessoRequestDTO buscaProcessoRequestDTO) throws JSONException {
        return processoService.find(page, buscaProcessoRequestDTO);
    }

    @Transactional
    @PostMapping("/save")
    public ProcessoResponseDTO save(@RequestHeader("Username") String username, @RequestBody String processoString) throws JSONException, JsonProcessingException {
        Usuario usuario = null;
        JSONObject processoJson = new JSONObject(processoString);
        JSONObject source = processoJson.getJSONObject("_source");

        ProcessoRequestDTO dto = mapper.readValue(source.toString(), ProcessoRequestDTO.class);
        Processo processo = processoMapper.toProjeto(dto);

        Optional<Credenciais> credenciais = credenciaisService.findByUsername(username);
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getId());
        }
        Processo processoSalvo = processoService.create(processo);
        notificacaoService.createAndSave(usuario, processoSalvo);
        ProcessoResponseDTO processoResponseDTO = processoMapper.toProcessoResponseDTO(processoSalvo);
        return processoResponseDTO;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@RequestHeader("Username") String username, @PathVariable String id) {
        Optional<Credenciais> credenciais = credenciaisService.findByUsername(username);
        Optional<Notificacao> notificacao = null;
        Usuario usuario = null;
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getId());
        }
        if(usuario != null){
            notificacao = notificacaoService.findByUsuarioIdAndProcessoId(usuario.getId(), id);
        }
        notificacaoService.delete(notificacao.get().getId());
        processoService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
