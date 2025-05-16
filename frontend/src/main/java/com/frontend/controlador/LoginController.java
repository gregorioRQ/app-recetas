package com.frontend.controlador;

import java.io.IOException;

import com.frontend.servicio.AuthService;
import com.frontend.servicio.InicioService;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField correoField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private final AuthService authService = new AuthService();

    @FXML
    protected void handleLoginButtonAction() {
        String correo = correoField.getText();
        String password = passwordField.getText();

        authService.login(correo, password).thenAccept(loginSuccessful -> {
            if (loginSuccessful) {
                // Asegurarse de que la carga de la pantalla se ejecute en el hilo de JavaFX
                javafx.application.Platform.runLater(this::cargarPantallaInicio);
            } else {
                // Mostrar alerta en el hilo de JavaFX
                javafx.application.Platform
                        .runLater(() -> mostrarAlerta("Error de login", "Nombre de usuario o contraseña incorrectos."));
            }
        }).exceptionally(error -> {
            // Mostrar alerta en el hilo de JavaFX
            javafx.application.Platform
                    .runLater(() -> mostrarAlerta("Error de comunicación", "No se pudo comunicar con el servidor."));
            error.printStackTrace();
            return null;
        });
    }

    @FXML
    protected void handleRegisterButtonClick() {
        cargarPantallaRegistro();
    }

    private void cargarPantallaRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recetas - Registro");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error al cargar", "No se pudo cargar la pantalla de registro.");
            e.printStackTrace();
        }
    }

    private void cargarPantallaInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            InicioController inicioController = loader.getController();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recetas - Inicio");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error al cargar", "No se pudo cargar la pantalla de inicio.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

}
