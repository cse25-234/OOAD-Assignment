import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
        } else {
            messageLabel.setText(
                    "Login screen connected. Authentication comes next.");
        }
    }
}
