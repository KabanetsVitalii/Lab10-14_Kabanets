CREATE DATABASE IF NOT EXISTS car_registry
 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE car_registry;

CREATE TABLE IF NOT EXISTS cars (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    car_year INT NOT NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- Тестове наповнення
INSERT INTO cars (brand, model, car_year) VALUES
                                              ('Toyota', 'Camry', 2020),
                                              ('BMW', 'X5', 2022),
                                              ('Ford', 'Focus', 2018);