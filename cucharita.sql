CREATE DATABASE cucharita;

USE cucharita;

CREATE TABLE recipes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    ingredients JSON NOT NULL,
    steps JSON NOT NULL,
    imageUri VARCHAR(500),
    isFavorite TINYINT(1) DEFAULT 0
);
