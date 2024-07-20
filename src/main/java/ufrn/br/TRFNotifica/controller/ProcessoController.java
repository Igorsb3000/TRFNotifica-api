package ufrn.br.TRFNotifica.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

import java.io.IOException;
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

    /*
    @PostMapping()// /processos?page=1
    public String getByNumero(@RequestParam (defaultValue = "1") String page, @RequestBody BuscaProcessoRequestDTO buscaProcessoRequestDTO) throws JSONException {
        return processoService.find(page, buscaProcessoRequestDTO);
    }

     */
    @GetMapping("/{identificador}")
    public String getByIdentificador(@PathVariable String identificador) throws JSONException {
        return processoService.searchByIdentificador(identificador);
    }

    @GetMapping("/local/{identificador}")
    public Processo getByIdentificadorLocal(@PathVariable String identificador) throws JSONException {
        return processoService.getByIdentificadorLocal(identificador);
    }

    @PostMapping()// /processos?page=1
    public String getByNumero(@RequestParam int size,
                              @RequestParam(defaultValue = "1") int nextTrf,
                              @RequestParam(required = false) Long searchAfter,
                              @RequestBody BuscaProcessoRequestDTO buscaProcessoRequestDTO) throws JSONException, IOException {
        return processoService.find(size, searchAfter, nextTrf, buscaProcessoRequestDTO);
    }

    @GetMapping()
    public List<Processo> findProcessosByUsuarioId(@AuthenticationPrincipal UserDetails userDetails){
        Usuario usuario = null;
        Optional<Credenciais> credenciais = credenciaisService.findByUsername(userDetails.getUsername());
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getUsuario() .getId());
        }
        if(usuario != null){
            return processoService.findProcessosByUsuarioId(usuario.getId());
        }
        return null;
    }

    @Transactional
    @PostMapping("/save")
    public ResponseEntity save(@AuthenticationPrincipal UserDetails userDetails, @RequestBody String processoString) throws JSONException, JsonProcessingException {
        Usuario usuario = null;
        JSONObject processoJson = new JSONObject(processoString);
        //JSONObject source = processoJson.getJSONObject("_source");

        Optional<Credenciais> credenciais = credenciaisService.findByUsername(userDetails.getUsername());
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getId());
        }
        ProcessoRequestDTO dto = mapper.readValue(processoJson.toString(), ProcessoRequestDTO.class);
        Processo processo = processoMapper.toProcesso(dto);
        processoService.salvarProcesso(usuario, processo);
        return ResponseEntity.noContent().build();//processoMapper.toProcessoResponseDTO(processoSalvo);
    }

    @DeleteMapping("/{identificador}")
    public ResponseEntity delete(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String identificador) {
        //System.out.println("user details: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        //System.out.println("user details: " + userDetails.getUsername());
        Optional<Credenciais> credenciais = credenciaisService.findByUsername(userDetails.getUsername());
        Optional<Notificacao> notificacao = null;
        Optional<Processo> processoEncontrado;
        Usuario usuario = null;
        if(credenciais.isPresent()){
            usuario = usuarioService.findById(credenciais.get().getId());
        }
        if(usuario != null){
            processoEncontrado = processoService.findByIdentificador(identificador);
            if(processoEncontrado.isPresent()){
                notificacao = notificacaoService.findByUsuarioIdAndProcessoId(usuario.getId(), processoEncontrado.get().getId());
                if(notificacao.isPresent()){
                    notificacaoService.delete(notificacao.get().getId());
                    List<Notificacao> notificacaoProcessoList = notificacaoService.findByProcessoId(processoEncontrado.get().getId());
                    if(notificacaoProcessoList.isEmpty()){
                        processoService.delete(processoEncontrado.get().getId());
                    }
                }
            }
        }
        return ResponseEntity.noContent().build();
    }



}
