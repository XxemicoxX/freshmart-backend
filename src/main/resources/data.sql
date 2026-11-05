INSERT INTO roles (nombre) VALUES ('CLIENTE'), ('PERSONAL');
INSERT INTO tienda (nombre, direccion, telefono, horario_atencion, activa)
VALUES ('Freshmart Demo', 'Av. Ejemplo 123, Lima', '01-5550101', 'Lun-Dom 8:00-22:00', TRUE);
INSERT INTO categoria (nombre, descripcion) VALUES ('Abarrotes', 'Productos básicos para el hogar');
INSERT INTO producto (id_categoria, nombre, descripcion, precio, calificacion_promedio, imagen_url, activo)
VALUES (1, 'Arroz extra 1 kg', 'Arroz de grano largo', 5.90, 4.5, '/img/arroz.png', TRUE);
INSERT INTO inventario (id_producto, id_tienda, stock) VALUES (1, 1, 50);
INSERT INTO pregunta_frecuente (categoria, pregunta, respuesta)
VALUES ('Pedidos', '¿Cómo recojo mi pedido?', 'Presenta el código del pedido y un documento de identidad en la tienda elegida.');
