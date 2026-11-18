import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            LoginDAO.AuthResult result = new LoginDAO().authenticate(
                    usernameField.getText().trim(),
                    passwordField.getText());

            if (result.locked()) {
                messageLabel.setText(
                        "Account locked after 3 failed attempts. "
                                + "Ask an administrator to reset it.");
                return;
            }

            if (!result.authenticated()) {
                messageLabel.setText(
                        "Incorrect login. Attempts remaining: "
                                + result.attemptsRemaining());
                return;
            }

            if (!"CUSTOMER".equals(result.role())
                    || result.customerId() == null) {
                messageLabel.setText(
                        "This login is not set up for customer access.");
                return;
            }

            Parent accountView = FXMLLoader.load(
                    getClass().getResource("/AccountView.fxml"));

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(accountView));
            stage.setTitle("Banking System - My Accounts");

        } catch (SQLException e) {
            messageLabel.setText("Could not check the login.");
            e.printStackTrace();
        } catch (IOException | NullPointerException e) {
            messageLabel.setText("Could not open the account screen.");
            e.printStackTrace();
        }
    }
}