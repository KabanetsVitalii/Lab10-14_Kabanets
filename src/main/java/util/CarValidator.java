package util;

import model.Car;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public final class CarValidator {
    private CarValidator() {}

    public static List<String> check(Car car) {
        List<String> problems = new ArrayList<>();
        if (car == null) {
            problems.add("відсутній об’єкт автомобіля");
            return problems;
        }
        if (car.getBrand() == null || car.getBrand().isBlank()) {
            problems.add("марка порожня");
        }
        if (car.getModel() == null || car.getModel().isBlank()) {
            problems.add("модель порожня");
        }

        int currentYear = Year.now().getValue();
        if (car.getYear() < 1900 || car.getYear() > currentYear) {
            problems.add("рік поза межами діапазону (1900–" + currentYear + ")");
        }
        return problems;
    }
}