package pe.freshmart.notificacion;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/faq")
public class FaqController {
    private final FaqRepository preguntas;

    public FaqController(FaqRepository preguntas) { this.preguntas = preguntas; }

    @GetMapping
    public List<PreguntaFrecuente> listar() { return preguntas.findAllByOrderByCategoriaAscIdAsc(); }
}
