package pe.freshmart.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PrecioServiceTest {
    private final PrecioService precios = new PrecioService(new BigDecimal("0.18"));

    @Test void calculaIgvDel18PorCiento() {
        var t = precios.calcular(new BigDecimal("1.00"));
        assertEquals(new BigDecimal("0.18"), t.impuestos());
        assertEquals(new BigDecimal("1.18"), t.total());
    }
    @Test void calculaTotalDeUnCarritoMixto() {
        var t = precios.calcular(new BigDecimal("19.00"));
        assertEquals(new BigDecimal("3.42"), t.impuestos());
        assertEquals(new BigDecimal("22.42"), t.total());
    }
    @Test void redondeaHalfUp() {
        var t = precios.calcular(new BigDecimal("0.05"));
        assertEquals(new BigDecimal("0.01"), t.impuestos());
        assertEquals(new BigDecimal("0.06"), t.total());
    }
}
