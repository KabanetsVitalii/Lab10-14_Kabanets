package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Car;
import service.CarService;
import java.util.List;
import java.util.Objects;

public class CarController {
    private CarService carService;
    private final ObservableList<Car> carList = FXCollections.observableArrayList();

    @FXML private TableView<Car> carTable;
    @FXML private TableColumn<Car, Long> colId;
    @FXML private TableColumn<Car, String> colBrand;
    @FXML private TableColumn<Car, String> colModel;
    @FXML private TableColumn<Car, Integer> colYear;

    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField yearField;
    @FXML private TextField queryField;
    @FXML private ComboBox<String> fieldBox;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId()).asObject());
        colBrand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrand()));
        colModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colYear.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getYear()).asObject());

        carTable.setItems(carList);

        fieldBox.getItems().setAll("Марка", "Модель", "Рік");
        fieldBox.getSelectionModel().selectFirst();

        carTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                brandField.setText(newVal.getBrand());
                modelField.setText(newVal.getModel());
                yearField.setText(String.valueOf(newVal.getYear()));
            }
        });
    }

    public void setCarService(CarService carService) {
        this.carService = Objects.requireNonNull(carService);
        refreshTable();
    }

    @FXML
    public void refreshTable() {
        if (carService == null) return;
        try {
            carList.setAll(carService.getAllCars());
            statusLabel.setText("Успішно завантажено записів: " + carList.size());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            carService.addCar(brandField.getText(), modelField.getText(), yearField.getText());
            refreshTable();
            clearFields();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        Car selected = carTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Оберіть автомобіль для редагування.");
            return;
        }
        try {
            carService.updateCar(selected.getId(), brandField.getText(), modelField.getText(), yearField.getText());
            refreshTable();
            clearFields();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Car selected = carTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Оберіть автомобіль для видалення.");
            return;
        }
        try {
            carService.deleteCar(selected.getId());
            refreshTable();
            clearFields();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void searchCars() {
        query(true);
    }

    @FXML
    private void filterCars() {
        query(false);
    }

    private void query(boolean partial) {
        String text = queryField.getText().trim();
        if (text.isBlank()) {
            showError("Введіть значення для пошуку.");
            return;
        }
        try {
            List<Car> result = carService.searchCars(selectedFieldKey(), text, partial);
            carList.setAll(result);
            statusLabel.setText((partial ? "Знайдено: " : "Відфільтровано: ") + result.size());
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private String selectedFieldKey() {
        return switch (fieldBox.getValue()) {
            case "Марка" -> "brand";
            case "Модель" -> "model";
            case "Рік" -> "year";
            default -> throw new IllegalArgumentException("Оберіть поле для пошуку.");
        };
    }

    private void clearFields() {
        carTable.getSelectionModel().clearSelection();
        brandField.clear();
        modelField.clear();
        yearField.clear();
    }

    private void showError(String message) {
        statusLabel.setText("Помилка: " + message);
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}