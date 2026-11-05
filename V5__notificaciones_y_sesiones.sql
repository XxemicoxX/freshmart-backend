ALTER TABLE notificacion ADD COLUMN enlace VARCHAR(255) NULL;
CREATE TABLE token_revocado (
  jti VARCHAR(100) NOT NULL PRIMARY KEY,
  id_usuario INT NOT NULL,
  vence_en DATETIME NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
  INDEX idx_token_revocado_vence (vence_en)
);
