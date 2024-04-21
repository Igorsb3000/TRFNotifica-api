package ufrn.br.TRFNotifica.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;
import ufrn.br.TRFNotifica.config.ApiVersion;
import ufrn.br.TRFNotifica.dto.ProcessoRequestDTO;
import ufrn.br.TRFNotifica.service.ProcessoService;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    @PostMapping()
    public String getByNumero(@RequestBody ProcessoRequestDTO processoRequestDTO) throws JSONException {
        if(processoRequestDTO.getClasseCodigo() == null && processoRequestDTO.getOrgaoJulgadorCodigo() == null){
            return processoService.findByNumero(processoRequestDTO.getNumeroProcesso());
        }
        return processoService.findByClasseEOrgao(processoRequestDTO.getClasseCodigo(), processoRequestDTO.getOrgaoJulgadorCodigo());
    }

}
