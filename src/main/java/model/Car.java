package model;

import javafx.beans.property.*;
public class Car {
    private final LongProperty id;
    private final StringProperty brand;
    private final StringProperty model;
    private final IntegerProperty year;
    public Car(long id, String brand, String model, int year) {
        this.id = new SimpleLongProperty(id);
        this.brand = new SimpleStringProperty(brand);
        this.model = new SimpleStringProperty(model);
        this.year = new SimpleIntegerProperty(year);
    }
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }

    public String getBrand() { return brand.get(); }
    public void setBrand(String brand) { this.brand.set(brand); }
    public StringProperty brandProperty() { return brand; }

    public String getModel() { return model.get(); }
    public void setModel(String model) { this.model.set(model); }
    public StringProperty modelProperty() { return model; }

    public int getYear() { return year.get(); }
    public void setYear(int year) { this.year.set(year); }
    public IntegerProperty yearProperty() { return year; }
}