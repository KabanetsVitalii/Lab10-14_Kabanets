package dao;

import model.Car;
import java.util.List;
import java.util.Optional;

public interface CarDao {
    Car add(Car car);
    List<Car> findAll();
    Optional<Car> findById(long id);
    void update(Car car);
    void delete(long id);
    List<Car> search(String field, String value, boolean partial);
}