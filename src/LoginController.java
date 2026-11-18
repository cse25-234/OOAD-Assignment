import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        if (usernameField.getText().isBlank()
                || passwordField.getText().isBlank()) {
            messageLabel.setText("Enter both username and password.");
            return;
        }

        try {
            Parent accountView = FXMLLoader.load(
                    getClass().getResource("/AccountView.fxml"));

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(accountView));
            stage.setTitle("Banking System - My Accounts");
        } catch (IOException | NullPointerException e) {
            messageLabel.setText("Could not open the account screen.");
            e.printStackTrace();
        }
    }
}