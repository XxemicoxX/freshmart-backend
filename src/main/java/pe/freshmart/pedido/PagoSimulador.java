package pe.freshmart.pedido;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.YearMonth;
import org.springframework.stereotype.Component;
import pe.freshmart.common.ApiException;
import pe.freshmart.pedido.PedidoDtos.PagoRequest;

/**
 * SIMULACIÓN de pasarela de pago (no hay servicio externo real). Sin efectos secundarios.
 * Tarjeta: termina en 0002 -> rechazada; termina en 9995 -> fondos insuficientes; otra -> aprobada.
 * Yape: código 000000 -> rechazado; otro -> aprobado.
 */
@Component
public class PagoSimulador {
    public record Resultado(boolean aprobado, String mensaje, String referencia) {}

    public Resultado procesar(PagoRequest r) {
        return r.metodo() == MetodoPago.TARJETA ? tarjeta(r) : yape(r);
    }

    private Resultado tarjeta(PagoRequest r) {
        String n = r.numeroTarjeta() == null ? "" : r.numeroTarjeta().replaceAll("[\\s-]", "");
        if (!n.matches("\\d{16}")) throw malo("Ingresa los 16 dígitos de la tarjeta de prueba.");
        String v = r.vencimiento();
        if (v == null || !v.matches("(0[1-9]|1[0-2])/\\d{2}")) throw malo("Ingresa el vencimiento con el formato MM/AA.");
        if (YearMonth.of(2000 + Integer.parseInt(v.substring(3)), Integer.parseInt(v.substring(0, 2))).isBefore(YearMonth.now()))
            throw malo("La tarjeta de prueba está vencida. Usa una fecha vigente.");
        if (r.cvv() == null || !r.cvv().matches("\\d{3,4}")) throw malo("Ingresa el código de seguridad de 3 o 4 dígitos.");
        String ref = "**** " + n.substring(12);
        if (n.endsWith("0002")) return new Resultado(false, "El banco rechazó la tarjeta (simulación). Prueba con otra tarjeta o paga con Yape.", ref);
        if (n.endsWith("9995")) return new Resultado(false, "Fondos insuficientes (simulación). Prueba con otra tarjeta o paga con Yape.", ref);
        return new Resultado(true, "Pago aprobado (simulación).", ref);
    }

    private Resultado yape(PagoRequest r) {
        if (r.celular() == null || !r.celular().matches("9\\d{8}")) throw malo("Ingresa tu número de celular Yape (9 dígitos, empieza con 9).");
        if (r.codigoYape() == null || !r.codigoYape().matches("\\d{6}")) throw malo("Ingresa el código de aprobación de 6 dígitos.");
        String ref = "*****" + r.celular().substring(6);
        if (r.codigoYape().equals("000000")) return new Resultado(false, "Yape no aprobó el pago (simulación). Revisa el código o paga con tarjeta.", ref);
        return new Resultado(true, "Pago aprobado (simulación).", ref);
    }

    private ApiException malo(String m) { return new ApiException(BAD_REQUEST, m); }
}
