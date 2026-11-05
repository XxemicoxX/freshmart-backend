package pe.freshmart.pedido;

public enum EstadoPedido {
    PENDIENTE_PAGO("Pendiente de pago"),
    PAGO_APROBADO("Pago aprobado"),
    CONFIRMADO("Confirmado"),
    EN_PREPARACION("En preparación"),
    DISPONIBLE_RECOJO("Disponible para recojo"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedido(String etiqueta) { this.etiqueta = etiqueta; }
    public String getEtiqueta() { return etiqueta; }
    public boolean tienePago() {
        return this == PAGO_APROBADO || this == CONFIRMADO || this == EN_PREPARACION
            || this == DISPONIBLE_RECOJO || this == ENTREGADO;
    }
    public boolean puedePasarA(EstadoPedido nuevo) {
        return switch (this) {
            case PENDIENTE_PAGO -> nuevo == PAGO_APROBADO || nuevo == CANCELADO;
            case PAGO_APROBADO -> nuevo == CONFIRMADO || nuevo == CANCELADO;
            case CONFIRMADO -> nuevo == EN_PREPARACION || nuevo == CANCELADO;
            case EN_PREPARACION -> nuevo == DISPONIBLE_RECOJO;
            case DISPONIBLE_RECOJO -> nuevo == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
