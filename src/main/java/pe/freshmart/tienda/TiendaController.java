package pe.freshmart.tienda;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.freshmart.common.ApiException;

@RestController
@RequestMapping("/api/v1/tiendas")
public class TiendaController {
	private final TiendaRepository tiendas;

	public TiendaController(TiendaRepository tiendas) { this.tiendas = tiendas; }

	@GetMapping
	public List<TiendaDto> listar() { return tiendas.findAllByActivaTrueOrderByNombre().stream().map(TiendaDto::de).toList(); }

	@GetMapping("/{id}")
	public TiendaDto detalle(@PathVariable Integer id) {
		return tiendas.findByIdAndActivaTrue(id).map(TiendaDto::de)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La tienda no existe o no está disponible."));
	}

	public record TiendaDto(Integer id, String nombre, String direccion, String telefono, String horarioAtencion) {
		public static TiendaDto de(Tienda tienda) {
			return new TiendaDto(tienda.getId(), tienda.getNombre(), tienda.getDireccion(),
				tienda.getTelefono(), tienda.getHorarioAtencion());
		}
	}
}
