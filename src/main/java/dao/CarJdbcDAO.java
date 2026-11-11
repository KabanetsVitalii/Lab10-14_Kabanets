package dao;

import model.Car;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CarJdbcDAO implements CarDao {

    @Override
    public Car add(Car car) {
        String sql = "INSERT INTO cars (brand, model, car_year) VALUES (?, ?, ?)";
        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setInt(3, car.getYear());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("БД не повернула ID");
                car.setId(keys.getInt(1));
                return car;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка додавання автомобіля до MySQL", ex);
        }
    }

    @Override
    public List<Car> findAll() {
        String sql = "SELECT id, brand, model, car_year FROM cars ORDER BY id";
        List<Car> cars = new ArrayList<>();
        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                cars.add(read(rs));
            }
            return cars;
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка читання автомобілів з MySQL", ex);
        }
    }

    @Override
    public Optional<Car> findById(long id) {
        String sql = "SELECT id, brand, model, car_year FROM cars WHERE id = ?";
        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(read(rs));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка пошуку за ID", ex);
        }
        return Optional.empty();
    }

    @Override
    public void update(Car car) {
        String sql = "UPDATE cars SET brand = ?, model = ?, car_year = ? WHERE id = ?";
        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setInt(3, car.getYear());
            ps.setLong(4, car.getId());
            if (ps.executeUpdate() == 0) {
                throw new NoSuchElementException("Автомобіль не знайдено: " + car.getId());
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка оновлення автомобіля в MySQL", ex);
        }
    }

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM cars WHERE id = ?";
        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new NoSuchElementException("Автомобіль не знайдено: " + id);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка видалення автомобіля з MySQL", ex);
        }
    }

    @Override
    public List<Car> search(String field, String value, boolean partial) {
        String column = switch (field) {
            case "brand" -> "brand";
            case "model" -> "model";
            case "year" -> "car_year";
            default -> throw new IllegalArgumentException("Невідоме поле пошуку: " + field);
        };

        String sql = "SELECT id, brand, model, car_year FROM cars WHERE "
                + (partial ? column + " LIKE ? ESCAPE '!'" : column + " = ?")
                + " ORDER BY id";

        String parameter = partial ? "%" + escapeLike(value) + "%" : value;
        List<Car> result = new ArrayList<>();

        try (Connection cn = DBUtil.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, parameter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(read(rs));
                }
            }
            return result;
        } catch (SQLException ex) {
            throw new IllegalStateException("Помилка пошуку автомобілів в MySQL", ex);
        }
    }

    private static String escapeLike(String value) {
        return value.replace("!", "!!").replace("%", "!%")
                .replace("_", "!_");
    }

    private Car read(ResultSet rs) throws SQLException {
        return new Car(
                rs.getInt("id"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("car_year")
        );
    }
}