package pe.freshmart.auth;

import static pe.freshmart.auth.AuthDtos.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@RequestBody RegistroRequest request) { auth.registrar(request); }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) { return auth.login(request); }

    @PostMapping("/recuperar")
    public void recuperar(@RequestBody RecuperarRequest request) { auth.solicitarRecuperacion(request.correo()); }

    @PostMapping("/restablecer")
    public void restablecer(@RequestBody RestablecerRequest request) { auth.restablecer(request); }
}
