package com.frontend.controlador;

import java.time.LocalDate;

import com.frontend.servicio.AuthService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
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
    private Button registerButton;

    private final AuthService authService = new AuthService();

    @FXML
    protected void handleRegisterButtonClick() {
        // Capturar los valores de los campos
        String nombre = nombreField.getText();
        String apellido = apellidoField.getText();
        String correo = correoField.getText();
        String password = passwordField.getText();
        LocalDate fechaNacimiento = fechaNacimientoField.getValue();

        authService.enviarCredencialesRegistro(nombre, password, correo, apellido, fechaNacimiento);
    }
}
