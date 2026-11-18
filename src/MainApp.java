import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Main.main(new String[0]);

        LoginDAO loginDAO = new LoginDAO();
        loginDAO.ensureDemoUser();
        loginDAO.ensureDemoAdmin();

        FXMLLoader loader = new FXMLLoader(
                MainApp.class.getResource("/LoginView.fxml"));

        Scene scene = new Scene(loader.load());

        stage.setTitle("Banking System - Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
