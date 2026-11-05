-- Freshmart S.A.C. - Esquema inicial (MySQL 8)
CREATE TABLE roles (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE permisos (id INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(60) NOT NULL UNIQUE, descripcion VARCHAR(150));
CREATE TABLE roles_permisos (
  id_rol INT NOT NULL, id_permiso INT NOT NULL, PRIMARY KEY (id_rol, id_permiso),
  FOREIGN KEY (id_rol) REFERENCES roles(id), FOREIGN KEY (id_permiso) REFERENCES permisos(id));

CREATE TABLE tienda (
  id_tienda INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL, direccion VARCHAR(255) NOT NULL,
  telefono VARCHAR(20), horario_atencion VARCHAR(100), activa BOOLEAN NOT NULL DEFAULT TRUE);

-- Modelo original: CLIENTE. Se generaliza a USUARIO (cliente/personal) para soportar roles.
CREATE TABLE usuario (
  id_usuario INT AUTO_INCREMENT PRIMARY KEY, nombres VARCHAR(100) NOT NULL, apellidos VARCHAR(100) NOT NULL,
  correo VARCHAR(150) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, telefono VARCHAR(20),
  fecha_nacimiento DATE, id_rol INT NOT NULL, id_tienda INT NULL COMMENT 'solo personal de tienda',
  estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO', fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_rol) REFERENCES roles(id), FOREIGN KEY (id_tienda) REFERENCES tienda(id_tienda));

CREATE TABLE categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(100) NOT NULL UNIQUE, descripcion VARCHAR(255));
CREATE TABLE producto (
  id_producto INT AUTO_INCREMENT PRIMARY KEY, id_categoria INT NOT NULL, nombre VARCHAR(150) NOT NULL,
  descripcion VARCHAR(255), precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  calificacion_promedio DECIMAL(2,1) DEFAULT 0, imagen_url VARCHAR(255), activo BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria), INDEX idx_prod_nombre (nombre));
-- Stock por tienda (el modelo original tenía stock en PRODUCTO; se separa porque el recojo es por tienda)
CREATE TABLE inventario (
  id_producto INT NOT NULL, id_tienda INT NOT NULL, stock INT NOT NULL CHECK (stock >= 0),
  PRIMARY KEY (id_producto, id_tienda),
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto), FOREIGN KEY (id_tienda) REFERENCES tienda(id_tienda));

CREATE TABLE carrito (
  id_carrito INT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL UNIQUE, id_tienda INT NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario), FOREIGN KEY (id_tienda) REFERENCES tienda(id_tienda));
CREATE TABLE detalle_carrito (
  id_detalle_carrito INT AUTO_INCREMENT PRIMARY KEY, id_carrito INT NOT NULL, id_producto INT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0), UNIQUE (id_carrito, id_producto),
  FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto));
  -- precio_unitario y subtotal se calculan en backend con el precio vigente

CREATE TABLE pedido (
  id_pedido INT AUTO_INCREMENT PRIMARY KEY, codigo VARCHAR(20) NOT NULL UNIQUE, id_usuario INT NULL, id_tienda INT NOT NULL,
  fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  estado ENUM('PENDIENTE_PAGO','PAGO_APROBADO','CONFIRMADO','EN_PREPARACION','DISPONIBLE_RECOJO','ENTREGADO','CANCELADO') NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL, impuestos DECIMAL(10,2) NOT NULL, total DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE SET NULL, FOREIGN KEY (id_tienda) REFERENCES tienda(id_tienda),
  INDEX idx_pedido_usuario (id_usuario, fecha_pedido));
CREATE TABLE detalle_pedido (
  id_detalle_pedido INT AUTO_INCREMENT PRIMARY KEY, id_pedido INT NOT NULL, id_producto INT NOT NULL,
  nombre_producto VARCHAR(150) NOT NULL, cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL, subtotal DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE, FOREIGN KEY (id_producto) REFERENCES producto(id_producto));
CREATE TABLE historial_estado_pedido (  -- trazabilidad de transiciones (historial de compras / seguimiento)
  id BIGINT AUTO_INCREMENT PRIMARY KEY, id_pedido INT NOT NULL, estado VARCHAR(30) NOT NULL,
  fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, id_usuario_actor INT NULL,
  FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido) ON DELETE CASCADE);

CREATE TABLE pago (  -- NUNCA se guardan datos de tarjeta; solo los últimos 4 dígitos como referencia
  id_pago INT AUTO_INCREMENT PRIMARY KEY, id_pedido INT NOT NULL, fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  metodo_pago ENUM('TARJETA','YAPE') NOT NULL, monto DECIMAL(10,2) NOT NULL,
  estado ENUM('APROBADO','RECHAZADO') NOT NULL, codigo_transaccion VARCHAR(30) UNIQUE, referencia VARCHAR(30),
  simulado BOOLEAN NOT NULL DEFAULT TRUE, FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido));
CREATE TABLE comprobante (
  id_comprobante INT AUTO_INCREMENT PRIMARY KEY, id_pago INT NOT NULL UNIQUE, tipo_comprobante VARCHAR(30) NOT NULL DEFAULT 'BOLETA',
  serie VARCHAR(10) NOT NULL, numero VARCHAR(20) NOT NULL, fecha_emision DATETIME NOT NULL, monto_total DECIMAL(10,2) NOT NULL,
  UNIQUE (serie, numero), FOREIGN KEY (id_pago) REFERENCES pago(id_pago));

CREATE TABLE notificacion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL, tipo VARCHAR(20) NOT NULL, titulo VARCHAR(120) NOT NULL,
  mensaje VARCHAR(255) NOT NULL, leida BOOLEAN NOT NULL DEFAULT FALSE, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE, INDEX idx_notif (id_usuario, leida));
CREATE TABLE pregunta_frecuente (
  id INT AUTO_INCREMENT PRIMARY KEY, categoria VARCHAR(60) NOT NULL, pregunta VARCHAR(200) NOT NULL, respuesta TEXT NOT NULL);
CREATE TABLE configuracion_usuario (
  id_usuario INT PRIMARY KEY, notif_pedidos BOOLEAN NOT NULL DEFAULT TRUE, notif_promociones BOOLEAN NOT NULL DEFAULT TRUE,
  notif_correo BOOLEAN NOT NULL DEFAULT FALSE, tema ENUM('CLARO','OSCURO') NOT NULL DEFAULT 'CLARO',
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE);
CREATE TABLE token_recuperacion (  -- diagrama de secuencia "Recuperar contraseña": token + vencimiento
  id BIGINT AUTO_INCREMENT PRIMARY KEY, id_usuario INT NOT NULL, token_hash CHAR(64) NOT NULL UNIQUE,
  vence_en DATETIME NOT NULL, usado BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE);
