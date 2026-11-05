-- Datos FICTICIOS de demostración (precios en soles, S/ con IGV aparte)
INSERT INTO roles (nombre) VALUES ('CLIENTE'), ('PERSONAL');
INSERT INTO tienda (nombre, direccion, telefono, horario_atencion) VALUES
 ('Freshmart San Juan de Lurigancho', 'Av. Ficticia 123, San Juan de Lurigancho', '01-5550101', 'Lun-Dom 8:00-22:00'),
 ('Freshmart Miraflores', 'Calle Ejemplo 456, Miraflores', '01-5550102', 'Lun-Dom 8:00-23:00'),
 ('Freshmart San Miguel', 'Jr. Demo 789, San Miguel', '01-5550103', 'Lun-Sáb 9:00-21:00');
-- Orden fijo: los ids 1..10 se usan abajo
INSERT INTO categoria (nombre, descripcion) VALUES
 ('Alimentos Frescos', 'Productos frescos y de calidad para el día a día'),
 ('Abarrotes', 'Todo lo esencial para la despensa'),
 ('Bebidas', 'Refresca tu día con nuestras mejores bebidas'),
 ('Congelados', 'Practicidad y sabor fresco cuando lo necesitas'),
 ('Limpieza del hogar', 'Mantén tu hogar limpio y ordenado'),
 ('Cuidado personal', 'Todo para tu higiene y bienestar diario'),
 ('Bebés', 'Cuidado para los más pequeños del hogar'),
 ('Mascotas', 'Lo mejor para cuidar y alimentar a tus mascotas'),
 ('Hogar y Cocina', 'Utensilios y productos para tu cocina y hogar'),
 ('Otros/Especiales', 'Productos dulces, ofertas y más por descubrir');
INSERT INTO producto (id_categoria, nombre, descripcion, precio, calificacion_promedio, imagen_url) VALUES
 (1,'Manzana Fuji 1 kg','Manzana fuji crocante y dulce',4.50,4.6,'/img/manzana.png'),
 (1,'Plátano de seda 1 kg','Plátano maduro, ideal para el desayuno',3.80,4.4,'/img/platano.png'),
 (1,'Pollo entero 1 kg','Pollo fresco de granja',11.90,4.5,'/img/pollo.png'),
 (2,'Arroz extra 5 kg','Arroz de grano largo',24.90,4.7,'/img/arroz.png'),
 (2,'Aceite vegetal 1 L','Aceite vegetal para cocinar',9.50,4.3,'/img/aceite.png'),
 (2,'Fideos spaghetti 500 g','Pasta de sémola de trigo',3.20,4.2,'/img/fideos.png'),
 (3,'Agua mineral 625 ml','Agua sin gas',1.80,4.1,'/img/agua.png'),
 (3,'Gaseosa cola 1.5 L','Bebida gasificada sabor cola',6.50,4.0,'/img/gaseosa.png'),
 (3,'Jugo de naranja 1 L','Jugo de naranja sin azúcar añadida',5.90,4.3,'/img/jugo.png'),
 (4,'Nuggets de pollo 400 g','Nuggets congelados listos para freír',12.50,4.2,'/img/nuggets.png'),
 (4,'Helado de vainilla 1 L','Helado cremoso de vainilla',13.90,4.5,'/img/helado.png'),
 (5,'Detergente líquido 2 L','Detergente para ropa de uso diario',18.90,4.4,'/img/detergente.png'),
 (5,'Lejía 1 L','Desinfectante para el hogar',4.50,4.1,'/img/lejia.png'),
 (5,'Lavavajilla 750 ml','Lavavajilla concentrado',7.20,4.3,'/img/lavavajilla.png'),
 (6,'Shampoo 400 ml','Shampoo para todo tipo de cabello',14.90,4.2,'/img/shampoo.png'),
 (6,'Jabón de tocador x3','Pack de tres jabones',6.90,4.0,'/img/jabon.png'),
 (6,'Pasta dental 100 ml','Protección anticaries',5.50,4.4,'/img/pasta-dental.png'),
 (7,'Pañales talla M x30','Pañales ultra absorbentes',39.90,4.6,'/img/panales.png'),
 (8,'Alimento para perro 3 kg','Alimento balanceado para perros adultos',32.00,4.5,'/img/perro.png'),
 (9,'Esponja multiuso x3','Pack de tres esponjas',4.20,4.0,'/img/esponja.png'),
 (9,'Set de tápers x5','Recipientes herméticos reutilizables',19.90,4.3,'/img/tapers.png'),
 (10,'Chocolate en barra 100 g','Chocolate de leche',4.90,4.6,'/img/chocolate.png');
INSERT INTO inventario (id_producto, id_tienda, stock) SELECT p.id_producto, t.id_tienda, 50 FROM producto p CROSS JOIN tienda t;
-- Producto agotado en una tienda para probar la disponibilidad
UPDATE inventario SET stock = 0 WHERE id_tienda = 1 AND id_producto = (SELECT id_producto FROM producto WHERE nombre = 'Helado de vainilla 1 L');
INSERT INTO pregunta_frecuente (categoria, pregunta, respuesta) VALUES
 ('Cuenta','¿Cómo creo una cuenta?','Desde la pantalla de inicio de sesión toca "Crear cuenta", completa el formulario y acepta los términos.'),
 ('Cuenta','¿Olvidé mi contraseña, qué hago?','Toca "¿Olvidaste tu contraseña?", ingresa tu correo y te enviaremos un enlace para crear una nueva.'),
 ('Pedidos','¿Hacen entregas a domicilio?','No. Freshmart funciona con recojo en la tienda que elijas al comprar.'),
 ('Pedidos','¿Cómo sé si mi pedido está listo?','Verás el estado en "Historial de compras" y recibirás una notificación cuando esté disponible para recojo.'),
 ('Pagos','¿Qué métodos de pago puedo usar?','Tarjeta de crédito/débito y Yape. En esta versión de demostración los pagos son simulados.'),
 ('Pagos','¿Dónde descargo mi boleta?','En la pantalla de confirmación y en el detalle de cada pedido del historial.'),
 ('Recojo','¿Qué debo llevar a la tienda?','Tu código de pedido y un documento de identidad.');
