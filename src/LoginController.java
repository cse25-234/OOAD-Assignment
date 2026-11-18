import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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

            if ("ADMIN".equals(result.role())) {
                TextInputDialog resetDialog = new TextInputDialog();
                resetDialog.setTitle("Admin Login Reset");
                resetDialog.setHeaderText("Reset a locked login");
                resetDialog.setContentText("Username to unlock:");

                Optional<String> usernameToReset = resetDialog.showAndWait();

                if (usernameToReset.isPresent()) {
                    String username = usernameToReset.get().trim();

                    if (username.isBlank()) {
                        messageLabel.setText("Enter the username to reset.");
                    } else {
                        new LoginDAO().resetLock(username);
                        messageLabel.setText(
                                "Login reset. The user can try signing in again.");
                    }
                }

                return;
            }

            if (!"CUSTOMER".equals(result.role())
                    || result.customerId() == null) {
                messageLabel.setText(
                        "This login is not set up for customer access.");
                return;
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();

            // Start the session before loading AccountView.fxml.
            UserSession.start(result.customerId());

            Parent accountView = FXMLLoader.load(
                    getClass().getResource("/AccountView.fxml"));

            stage.setScene(new Scene(accountView));
            stage.setTitle("Banking System - My Accounts");

            UserSession.watch(stage.getScene(), () -> {
                try {
                    Parent loginView = FXMLLoader.load(
                            getClass().getResource("/LoginView.fxml"));
                    stage.setScene(new Scene(loginView));
                    stage.setTitle("Banking System - Login");
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            });

        } catch (SQLException e) {
            messageLabel.setText("Could not check the login.");
            e.printStackTrace();
        } catch (IOException | NullPointerException e) {
            messageLabel.setText("Could not open the account screen.");
            e.printStackTrace();
        }
    }
}