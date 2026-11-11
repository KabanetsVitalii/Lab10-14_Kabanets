import controller.CarController;
import dao.CarDao;
import dao.CarJdbcDAO;
import service.CarService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        CarDao dao = new CarJdbcDAO();
        CarService service = new CarService(dao);

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/car-view.fxml"));
        Parent root = loader.load();

        CarController controller = loader.getController();
        controller.setCarService(service);

        stage.setScene(new Scene(root, 700, 500));
        stage.setTitle("Система обліку автомобілів — JDBC та FXML (ЛР11)");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}