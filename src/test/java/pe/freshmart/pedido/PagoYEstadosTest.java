package pe.freshmart.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static pe.freshmart.pedido.EstadoPedido.*;

import org.junit.jupiter.api.Test;
import pe.freshmart.common.ApiException;
import pe.freshmart.pedido.PedidoDtos.PagoRequest;

class PagoYEstadosTest {
    private final PagoSimulador sim = new PagoSimulador();
    private PagoRequest tarjeta(String n) { return new PagoRequest(MetodoPago.TARJETA, n, "12/39", "123", null, null); }
    private PagoRequest yape(String cel, String cod) { return new PagoRequest(MetodoPago.YAPE, null, null, null, cel, cod); }

    @Test void tarjetaDePruebaEsAprobadaYSoloGuardaUltimosDigitos() {
        var r = sim.procesar(tarjeta("4111 1111 1111 1111"));
        assertTrue(r.aprobado());
        assertEquals("**** 1111", r.referencia());
    }
    @Test void tarjetaTerminadaEn0002EsRechazada() { assertFalse(sim.procesar(tarjeta("4000000000000002")).aprobado()); }
    @Test void tarjetaConFormatoInvalidoEsErrorDeValidacion() { assertThrows(ApiException.class, () -> sim.procesar(tarjeta("1234"))); }
    @Test void yapeAprobadoYRechazado() {
        assertTrue(sim.procesar(yape("987654321", "123456")).aprobado());
        assertFalse(sim.procesar(yape("987654321", "000000")).aprobado());
    }
    @Test void yapeConCelularInvalidoEsErrorDeValidacion() { assertThrows(ApiException.class, () -> sim.procesar(yape("123", "123456"))); }

    @Test void transicionesValidasDeEstado() {
        assertTrue(PENDIENTE_PAGO.puedePasarA(PAGO_APROBADO));
        assertTrue(CONFIRMADO.puedePasarA(EN_PREPARACION));
        assertTrue(DISPONIBLE_RECOJO.puedePasarA(ENTREGADO));
    }
    @Test void transicionesInvalidasDeEstado() {
        assertFalse(PENDIENTE_PAGO.puedePasarA(CONFIRMADO));
        assertFalse(PENDIENTE_PAGO.puedePasarA(ENTREGADO));
        assertFalse(ENTREGADO.puedePasarA(CANCELADO));
        assertFalse(EN_PREPARACION.puedePasarA(CANCELADO));
    }
}
