package com.frontend.controlador;

import java.io.IOException;

import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import com.frontend.servicio.AuthService;

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
    private ValidationSupport validationSupport;

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

        // Verificar si hay errores de validación antes de continuar
        if (validationSupport.isInvalid()) {
            // Recopilar todos los mensajes de error de los campos validados
            StringBuilder errores = new StringBuilder();
            validationSupport.getValidationResult().getErrors().forEach(validationMessage -> {
                errores.append("- ").append(validationMessage.getText()).append("\n");
            });
            mostrarAlerta("Error de validación", "Por favor, corrige los siguientes errores:\n\n" + errores.toString());
            return;
        }

        authService.login(correo, password).thenAccept(loginSuccessful -> {
            if (loginSuccessful) {
                // Asegurarse de que la carga de la pantalla se ejecute en el hilo de JavaFX
                javafx.application.Platform.runLater(this::cargarPantallaInicio);
            } else {
                // Mostrar alerta en el hilo de JavaFX
                javafx.application.Platform
                        .runLater(
                                () -> mostrarAlerta("Error de login", "Correo electronico o contraseña incorrectos."));
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

    @FXML
    public void initialize() {
        configurarValidaciones();
    };

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());

        // valida la contraseña
        validationSupport.registerValidator(passwordField, true, Validator.createRegexValidator(
                "La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, un número y un caracter especial.",
                "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
                null));

        validationSupport.registerValidator(correoField, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'correo' es obligatorio"),
                Validator.createRegexValidator(
                        "Campo correo no válido. Asegúrate de incluir '@' y un dominio válido (ej. .com, .org).",
                        ".+@.+\\.[a-zA-Z]{2,6}",
                        null)));

    }

    private void cargarPantallaRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recetas - Registro");
            stage.setResizable(false);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            mostrarAlerta("Error al cargar", "No se pudo cargar la pantalla de registro.");
            e.printStackTrace();
        }
    }

    private void cargarPantallaInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Recetas - Inicio");
            stage.show();
            stage.centerOnScreen();
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
