-- Se reemplazan los ENUM nativos por VARCHAR + CHECK (compatibles con la validación de Hibernate y fáciles de migrar)
ALTER TABLE pedido MODIFY estado VARCHAR(30) NOT NULL;
ALTER TABLE pedido ADD CONSTRAINT ck_pedido_estado CHECK (estado IN
 ('PENDIENTE_PAGO','PAGO_APROBADO','CONFIRMADO','EN_PREPARACION','DISPONIBLE_RECOJO','ENTREGADO','CANCELADO'));
ALTER TABLE pago MODIFY metodo_pago VARCHAR(20) NOT NULL;
ALTER TABLE pago MODIFY estado VARCHAR(20) NOT NULL;
ALTER TABLE pago ADD CONSTRAINT ck_pago_metodo CHECK (metodo_pago IN ('TARJETA','YAPE'));
ALTER TABLE pago ADD CONSTRAINT ck_pago_estado CHECK (estado IN ('APROBADO','RECHAZADO','REEMBOLSADO'));
ALTER TABLE configuracion_usuario MODIFY tema VARCHAR(10) NOT NULL DEFAULT 'CLARO';
ALTER TABLE configuracion_usuario ADD CONSTRAINT ck_config_tema CHECK (tema IN ('CLARO','OSCURO'));
