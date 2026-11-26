CREATE TABLE categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    imagen VARCHAR(255) NOT NULL DEFAULT '/img/placeholder.png',
    texto_alternativo VARCHAR(150) NOT NULL
);