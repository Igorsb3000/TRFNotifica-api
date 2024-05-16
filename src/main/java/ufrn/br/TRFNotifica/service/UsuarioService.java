package ufrn.br.TRFNotifica.service;

import org.springframework.stereotype.Service;
import ufrn.br.TRFNotifica.base.BaseService;
import ufrn.br.TRFNotifica.model.Usuario;
import ufrn.br.TRFNotifica.repository.UsuarioRepository;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioRepository> {
}
