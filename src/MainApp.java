import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
         Database.initialize();
         Main.main(new String[0]);
new LoginDAO().ensureDemoUser();
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