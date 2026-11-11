package service;

import dao.CarDao;
import model.Car;
import java.time.Year;
import java.util.List;
import java.util.Objects;

public class CarService {
    private final CarDao carDao;

    public CarService(CarDao carDao) {
        this.carDao = Objects.requireNonNull(carDao);
    }

    public List<Car> getAllCars() {
        return carDao.findAll();
    }

    public void addCar(String brand, String model, String yearStr) {
        int year = validateAndParseYear(yearStr);
        validateCarData(brand, model);
        carDao.add(new Car(0, brand.trim(), model.trim(), year));
    }

    public void updateCar(long id, String brand, String model, String yearStr) {
        int year = validateAndParseYear(yearStr);
        validateCarData(brand, model);
        carDao.update(new Car(id, brand.trim(), model.trim(), year));
    }

    public void deleteCar(long id) {
        carDao.delete(id);
    }

    public List<Car> searchCars(String field, String value, boolean partial) {
        if (value == null || value.isBlank()) return getAllCars();
        return carDao.search(field, value.trim(), partial);
    }

    private void validateCarData(String brand, String model) {
        if (brand == null || brand.isBlank() || model == null || model.isBlank()) {
            throw new IllegalArgumentException("Марка та модель не можуть бути порожніми!");
        }
    }

    private int validateAndParseYear(String yearStr) {
        try {
            int year = Integer.parseInt(yearStr.trim());
            int currentYear = Year.now().getValue();
            if (year < 1900 || year > currentYear) {
                throw new IllegalArgumentException("Рік повинен бути в діапазоні від 1900 до " + currentYear + "!");
            }
            return year;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Рік має бути цілим числом!");
        }
    }
}