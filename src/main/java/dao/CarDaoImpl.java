package dao;

import model.Car;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarDaoImpl implements CarDao {
    private final List<Car> cars = new ArrayList<>();
    private long currentId = 1;

    public CarDaoImpl() {
        // Початкові тестові дані
        add(new Car(0, "Toyota", "Camry", 2020));
        add(new Car(0, "BMW", "X5", 2022));
    }

    @Override
    public Car add(Car car) {
        car.setId(currentId++);
        cars.add(car);
        return car;
    }

    @Override
    public List<Car> findAll() {
        return new ArrayList<>(cars);
    }

    @Override
    public Optional<Car> findById(long id) {
        return cars.stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public void update(Car car) {
        findById(car.getId()).ifPresent(existing -> {
            existing.setBrand(car.getBrand());
            existing.setModel(car.getModel());
            existing.setYear(car.getYear());
        });
    }

    @Override
    public void delete(long id) {
        cars.removeIf(c -> c.getId() == id);
    }

    @Override
    public List<Car> search(String field, String value, boolean partial) {
        if (value == null || value.isBlank()) {
            return findAll();
        }
        String lowerVal = value.toLowerCase();
        return cars.stream().filter(car -> {
            String target = switch (field) {
                case "brand" -> car.getBrand();
                case "model" -> car.getModel();
                case "year" -> String.valueOf(car.getYear());
                default -> "";
            };
            if (target == null) return false;
            String lowerTarget = target.toLowerCase();
            return partial ? lowerTarget.contains(lowerVal) : lowerTarget.equals(lowerVal);
        }).toList();
    }
}