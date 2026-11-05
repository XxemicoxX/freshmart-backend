-- Sesiones: revocación de tokens (logout) e invalidación global (cambio de contraseña)
ALTER TABLE usuario ADD COLUMN sesiones_desde DATETIME NULL;
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVO','ELIMINADO'));
CREATE TABLE token_revocado (
  jti CHAR(36) PRIMARY KEY, id_usuario INT NOT NULL, vence_en DATETIME NOT NULL, INDEX idx_revocado_vence (vence_en));
-- Notificaciones: enlace opcional (p. ej. a una categoría) y tipos controlados
ALTER TABLE notificacion ADD COLUMN enlace VARCHAR(100) NULL;
ALTER TABLE notificacion ADD CONSTRAINT ck_notif_tipo CHECK (tipo IN ('PEDIDO','PROMOCION','SISTEMA'));
