package com.frontend.controlador;

import java.io.IOException;
import java.time.LocalDate;

import com.frontend.servicio.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField nombreField;

    @FXML
    private TextField apellidoField;

    @FXML
    private TextField correoField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private DatePicker fechaNacimientoField;

    @FXML
    private Button volverLoginButton;

    @FXML
    protected void handleRegisterButtonClick() {
        // Capturar los valores de los campos
        String nombre = nombreField.getText();
        String apellido = apellidoField.getText();
        String correo = correoField.getText();
        String password = passwordField.getText();
        LocalDate fechaNacimiento = fechaNacimientoField.getValue();

        authService.enviarCredencialesRegistro(nombre, password, correo, apellido, fechaNacimiento);

        // Después del registro (exitoso o fallido, según la respuesta del backend),
        // podrías volver a la pantalla de login o mostrar un mensaje.
        mostrarAlerta("Información", "Solicitud de registro enviada.");
        cargarPantallaLogin(); // Volver al login después de la solicitud
    }

    @FXML
    protected void handleVolverLoginButtonClick() {
        cargarPantallaLogin();
    }

    private void cargarPantallaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) volverLoginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recetas - Login");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error al cargar", "No se pudo cargar la pantalla de login.");
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
