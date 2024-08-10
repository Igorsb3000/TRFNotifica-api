package ufrn.br.TRFNotifica.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ufrn.br.TRFNotifica.config.ApiVersion;
import ufrn.br.TRFNotifica.dto.BuscaProcessoRequestDTO;
import ufrn.br.TRFNotifica.model.Processo;
import ufrn.br.TRFNotifica.service.CredenciaisService;
import ufrn.br.TRFNotifica.service.NotificacaoService;
import ufrn.br.TRFNotifica.service.ProcessoService;
import ufrn.br.TRFNotifica.service.UsuarioService;

import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/processos")
public class ProcessoController {

    @Autowired
    private ProcessoService processoService;

    @Autowired
    private CredenciaisService credenciaisService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private NotificacaoService notificacaoService;

//    @Autowired
//    private ProcessoMapper processoMapper;
//
//    @Autowired
//    private ObjectMapper mapper;


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
    public List<Processo> findProcessosByUsuario(@AuthenticationPrincipal UserDetails userDetails){
        return processoService.findProcessosByUsuario(userDetails.getUsername());
    }

    @PostMapping("/save")
    public ResponseEntity save(@AuthenticationPrincipal UserDetails userDetails, @RequestBody String processoString) throws JSONException, JsonProcessingException {
        return processoService.save(userDetails.getUsername(), processoString);
    }

    @DeleteMapping("/{identificador}")
    public ResponseEntity deleteProcessoByUsuario(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String identificador) {
        return processoService.delete(userDetails.getUsername(), identificador);
    }



}
