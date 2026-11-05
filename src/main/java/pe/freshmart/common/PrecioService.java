package pe.freshmart.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Única fuente de verdad de subtotal/IGV/total. Los precios del catálogo NO incluyen IGV (como en el prototipo). */
@Service
public class PrecioService {
    public record Totales(BigDecimal subtotal, BigDecimal impuestos, BigDecimal total) {}

    private final BigDecimal igv;
    public PrecioService(@Value("${app.igv-rate}") BigDecimal igv) { this.igv = igv; }

    public Totales calcular(BigDecimal subtotal) {
        BigDecimal s = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal i = s.multiply(igv).setScale(2, RoundingMode.HALF_UP);
        return new Totales(s, i, s.add(i));
    }
    public BigDecimal getIgv() { return igv; }
}
