package pe.freshmart.carrito;

import static pe.freshmart.carrito.CarritoDtos.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {
    private final CarritoService carrito;

    public CarritoController(CarritoService carrito) { this.carrito = carrito; }

    @GetMapping
    public CarritoDto ver(@AuthenticationPrincipal Integer uid) { return carrito.ver(uid); }

    @PostMapping("/productos/{productoId}")
    public CarritoDto agregar(@AuthenticationPrincipal Integer uid, @PathVariable int productoId,
                              @RequestBody CantidadRequest request) {
        return carrito.agregar(uid, productoId, request.cantidad());
    }

    @PutMapping("/productos/{productoId}")
    public CarritoDto actualizar(@AuthenticationPrincipal Integer uid, @PathVariable int productoId,
                                 @RequestBody CantidadRequest request) {
        return carrito.actualizar(uid, productoId, request.cantidad());
    }

    @DeleteMapping("/productos/{productoId}")
    public CarritoDto eliminar(@AuthenticationPrincipal Integer uid, @PathVariable int productoId) {
        return carrito.eliminar(uid, productoId);
    }

    @DeleteMapping
    public CarritoDto vaciar(@AuthenticationPrincipal Integer uid) { return carrito.vaciar(uid); }

    @PutMapping("/tienda/{tiendaId}")
    public CarritoDto seleccionarTienda(@AuthenticationPrincipal Integer uid, @PathVariable int tiendaId) {
        return carrito.seleccionarTienda(uid, tiendaId);
    }
}
