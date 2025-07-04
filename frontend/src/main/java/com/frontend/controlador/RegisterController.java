package com.frontend.controlador;

import java.io.IOException;
import java.time.LocalDate;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import javafx.application.Platform;

public class RegisterController {

    private final AuthService authService = new AuthService();
    private final ObjectMapper objectMapper;
    private ValidationSupport validationSupport;

    public RegisterController() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDate
    }

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
    public void initialize() {
        configurarValidaciones();
    };

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());

        // validar el nombre
        validationSupport.registerValidator(nombreField, true,
                Validator.combine(
                        Validator.createEmptyValidator("El campo 'nombre' es obligatorio"),
                        Validator.createRegexValidator(
                                "El campo 'nombre' solo puede contener letras y no puede exceder los 20 caracteres",
                                "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{1,20}$",
                                null)));

        // validar el apellido
        validationSupport.registerValidator(apellidoField, true,
                Validator.combine(
                        Validator.createEmptyValidator("El campo 'apellido' es obligatorio"),
                        Validator.createRegexValidator(
                                "El campo 'apellido' solo puede contener letras y no puede exceder los 20 caracteres",
                                "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]{1,20}$",
                                null)));

        // valida el correo
        validationSupport.registerValidator(correoField, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'correo' es obligatorio"),
                Validator.createRegexValidator(
                        "Campo correo no válido. Asegúrate de incluir '@' y un dominio válido (ej. .com, .org).",
                        ".+@.+\\.[a-zA-Z]{2,6}",
                        null)));

        // valida la contraseña
        validationSupport.registerValidator(passwordField, true, Validator.createRegexValidator(
                "La contraseña debe tener al menos 8 caracteres, incluyendo una mayúscula, un número y un caracter especial.",
                "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
                null));

        // valida la fecha de nacimiento
        validationSupport.registerValidator(fechaNacimientoField, true,
                Validator.createEmptyValidator("La fecha de nacimiento es obligatoria"));
    }

    @FXML
    private void handleRegisterButtonClick() {
        // Verificar si hay errores de validación antes de continuar
        if (validationSupport.isInvalid()) {
            // Recopilar todos los mensajes de error de los campos validados
            StringBuilder errores = new StringBuilder();
            validationSupport.getValidationResult().getErrors().forEach(validationMessage -> {
                errores.append("- ").append(validationMessage.getText()).append("\n");
            });
            mostrarError("Error de validación", "Por favor, corrige los siguientes errores:\n\n" + errores.toString());
            return;
        }

        String nombre = nombreField.getText();
        String apellido = apellidoField.getText();
        String correo = correoField.getText();
        String password = passwordField.getText();
        LocalDate fechaNac = fechaNacimientoField.getValue();

        authService.enviarCredencialesRegistro(nombre, password, correo, apellido, fechaNac)
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            mostrarMensajeExito("Registro exitoso", "Usuario registrado correctamente");
                            cargarPantallaLogin();
                        } else {
                            mostrarError("Error", "No se pudo procesar la respuesta del servidor");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> mostrarError("Error de conexión",
                            "No se pudo conectar con el servidor. Intente más tarde."));
                    return null;
                });
    }

    private void mostrarMensajeExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void handleVolverLoginButtonClick() {
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
