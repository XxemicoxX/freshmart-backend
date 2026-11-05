package pe.freshmart.pedido;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

/** RF-12. Boleta PDF con OpenPDF. Documento de demostración. */
@Service
public class BoletaPdfService {
    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    private static final Font NEG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NOTA = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generar(Pedido p, Pago pago, Comprobante c, String cliente, String correo, BigDecimal igv) {
        var out = new ByteArrayOutputStream();
        var doc = new Document(PageSize.A4, 40, 40, 40, 40);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();
            doc.add(new Paragraph("FRESHMART S.A.C.", TITULO));
            doc.add(new Paragraph("Boleta de venta electrónica " + c.getSerie() + "-" + c.getNumero(), NEG));
            doc.add(new Paragraph(" "));
            fila(doc, "Pedido", p.getCodigo());
            fila(doc, "Fecha de compra", p.getFechaPedido().format(FECHA));
            fila(doc, "Cliente", cliente + (correo == null ? "" : " (" + correo + ")"));
            fila(doc, "Tienda de recojo", p.getTienda().getNombre() + " - " + p.getTienda().getDireccion());
            fila(doc, "Método de pago", pago.getMetodo() == MetodoPago.TARJETA ? "Tarjeta de crédito/débito" : "Yape");
            fila(doc, "Estado de la transacción", pago.getEstado() == EstadoPago.APROBADO ? "Aprobada" : pago.getEstado().name());
            fila(doc, "Código de transacción", pago.getCodigoTransaccion());
            doc.add(new Paragraph(" "));
            var t = new PdfPTable(new float[] {5, 1.2f, 2, 2});
            t.setWidthPercentage(100);
            for (String h : new String[] {"Producto", "Cant.", "P. unit. (S/)", "Subtotal (S/)"}) t.addCell(celda(h, NEG, Element.ALIGN_LEFT));
            for (var d : p.getDetalles()) {
                t.addCell(celda(d.getNombreProducto(), NORMAL, Element.ALIGN_LEFT));
                t.addCell(celda(String.valueOf(d.getCantidad()), NORMAL, Element.ALIGN_RIGHT));
                t.addCell(celda(d.getPrecioUnitario().toPlainString(), NORMAL, Element.ALIGN_RIGHT));
                t.addCell(celda(d.getSubtotal().toPlainString(), NORMAL, Element.ALIGN_RIGHT));
            }
            doc.add(t);
            doc.add(new Paragraph(" "));
            String pct = igv.multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString();
            derecha(doc, "Subtotal: S/ " + p.getSubtotal().toPlainString(), NORMAL);
            derecha(doc, "IGV (" + pct + "%): S/ " + p.getImpuestos().toPlainString(), NORMAL);
            derecha(doc, "TOTAL: S/ " + p.getTotal().toPlainString(), NEG);
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Documento generado por un sistema de demostración. Pago simulado, sin validez tributaria.", NOTA));
            doc.close();
        } catch (DocumentException e) { throw new IllegalStateException("No se pudo generar la boleta", e); }
        return out.toByteArray();
    }

    private void fila(Document d, String k, String v) {
        var p = new Paragraph(); p.add(new Chunk(k + ": ", NEG)); p.add(new Chunk(v == null ? "-" : v, NORMAL)); d.add(p);
    }
    private void derecha(Document d, String texto, Font f) { var p = new Paragraph(texto, f); p.setAlignment(Element.ALIGN_RIGHT); d.add(p); }
    private PdfPCell celda(String t, Font f, int alin) { var c = new PdfPCell(new Phrase(t, f)); c.setHorizontalAlignment(alin); c.setPadding(4); return c; }
}
