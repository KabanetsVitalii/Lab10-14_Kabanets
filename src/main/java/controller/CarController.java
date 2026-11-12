package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Car;
import service.CarService;
import util.CarValidator;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CarController {
    private static final Logger LOG = Logger.getLogger(CarController.class.getName());

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

    @FXML private Button searchBtn, filterBtn, resetBtn, addBtn, updateBtn, deleteBtn;
    @FXML private Button validateBtn, cancelBtn;
    @FXML private ProgressBar progress;

    private Task<?> currentTask;
    private boolean busy;

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

        progress.setProgress(0);
        progress.setVisible(false);
        progress.setManaged(false);
        cancelBtn.setDisable(true);
    }

    public void setCarService(CarService carService) {
        this.carService = Objects.requireNonNull(carService);
        refreshTable();
    }

    @FXML
    public void refreshTable() {
        if (carService == null) return;
        runAsync("Завантаження списку...", carService::getAllCars, records -> {
            carList.setAll(records);
            statusLabel.setText("Показано всі записи: " + carList.size());
        });
    }

    @FXML
    private void handleAdd() {
        String brand = brandField.getText();
        String model = modelField.getText();
        String year = yearField.getText();

        runAsync("Додавання автомобіля...", () -> {
            carService.addCar(brand, model, year);
            return null;
        }, ignored -> {
            clearFields();
            refreshTable();
        });
    }

    @FXML
    private void handleEdit() {
        Car selected = carTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Оберіть автомобіль для редагування.");
            return;
        }
        long id = selected.getId();
        String brand = brandField.getText();
        String model = modelField.getText();
        String year = yearField.getText();

        runAsync("Оновлення автомобіля...", () -> {
            carService.updateCar(id, brand, model, year);
            return null;
        }, ignored -> {
            clearFields();
            refreshTable();
        });
    }

    @FXML
    private void handleDelete() {
        Car selected = carTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Оберіть автомобіль для видалення.");
            return;
        }
        long id = selected.getId();
        runAsync("Видалення автомобіля...", () -> {
            carService.deleteCar(id);
            return null;
        }, ignored -> {
            clearFields();
            refreshTable();
        });
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
        String field = selectedFieldKey();
        runAsync(partial ? "Пошук..." : "Фільтрація...",
                () -> carService.searchCars(field, text, partial),
                result -> {
                    carList.setAll(result);
                    statusLabel.setText((partial ? "Знайдено: " : "Відфільтровано: ") + result.size());
                });
    }

    @FXML
    private void validateCars() {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                updateMessage("Отримання записів із MySQL...");
                List<Car> cars = carService.getAllCars();
                int total = cars.size();
                if (total == 0) {
                    updateProgress(1, 1);
                    return "Реєстр порожній";
                }
                int invalid = 0;
                for (int i = 0; i < total; i++) {
                    if (isCancelled()) return "Перевірку скасовано";
                    Car car = cars.get(i);
                    List<String> problems = CarValidator.check(car);
                    if (!problems.isEmpty()) {
                        invalid++;
                        LOG.warning("ID=" + car.getId() + ": " + String.join("; ", problems));
                    }
                    Thread.sleep(60); // Навчальна затримка для прогрес-бару
                    updateProgress(i + 1, total);
                    updateMessage("Перевірено " + (i + 1) + " із " + total);
                }
                return "Перевірено: " + total + ", проблемних записів: " + invalid;
            }
        };
        startTask(task, "Перевірка автомобілів...", true, true, result -> {
            statusLabel.setText(result);
        });
    }

    @FXML
    private void cancelValidation() {
        if (currentTask != null) currentTask.cancel(true);
    }

    private <T> void runAsync(String message, Callable<T> work, Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call();
            }
        };
        startTask(task, message, false, false, onSuccess);
    }

    private <T> void startTask(Task<T> task, String message, boolean determinate, boolean cancellable, Consumer<T> onSuccess) {
        if (busy) return;
        currentTask = task;
        setBusy(true, cancellable);
        LOG.info("Початок: " + message);

        if (determinate) {
            progress.progressProperty().bind(task.progressProperty());
            statusLabel.textProperty().bind(task.messageProperty());
        } else {
            progress.setProgress(-1);
            statusLabel.setText(message);
        }

        task.setOnSucceeded(event -> {
            finishTask();
            LOG.info("Завершено: " + message);
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(event -> {
            finishTask();
            Throwable error = task.getException();
            LOG.log(Level.SEVERE, "Помилка: " + message, error);
            showError(error == null ? "Невідома помилка" : error.getMessage());
        });

        task.setOnCancelled(event -> {
            finishTask();
            LOG.info("Скасовано: " + message);
            statusLabel.setText("Перевірку скасовано");
        });

        Thread thread = new Thread(task, "car-background-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void finishTask() {
        if (statusLabel.textProperty().isBound()) statusLabel.textProperty().unbind();
        if (progress.progressProperty().isBound()) progress.progressProperty().unbind();
        setBusy(false, false);
        currentTask = null;
        progress.setProgress(0);
    }

    private void setBusy(boolean active, boolean cancellable) {
        busy = active;
        for (Button b : new Button[]{addBtn, updateBtn, deleteBtn, searchBtn, filterBtn, resetBtn, validateBtn}) {
            if (b != null) b.setDisable(active);
        }
        if (cancelBtn != null) cancelBtn.setDisable(!active || !cancellable);
        progress.setVisible(active);
        progress.setManaged(active);
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