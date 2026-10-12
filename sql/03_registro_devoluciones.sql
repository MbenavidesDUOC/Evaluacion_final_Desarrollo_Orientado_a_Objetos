USE biblioteca;

ALTER TABLE prestamos
 ADD COLUMN fecha_devolucion_real DATE NULL,
 ADD COLUMN dias_atraso INT NOT NULL DEFAULT 0;

ALTER TABLE libros ADD CONSTRAINT chk_stock_no_negativo CHECK (stock >= 0);
