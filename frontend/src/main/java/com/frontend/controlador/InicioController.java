package com.frontend.controlador;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.frontend.SessionManager;
import com.frontend.servicio.AuthService;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class InicioController {

    private final InicioService inicioService;

    // constructor sin armumentos para javaFX e inicializacion de campos final
    public InicioController() {
        this.inicioService = new InicioService();
    }

    // constructor con argumentos para pruebas unitarias
    public InicioController(InicioService inicioService) {
        this.inicioService = inicioService;
    }

    @FXML
    private Label saludoLabel;

    @FXML
    private VBox formularioReceta;

    @FXML
    private TextField nombreRecetaField;

    @FXML
    private TextField ingredientesField;

    @FXML
    private TextField instruccionesField;

    private void mostrarSaludo() {
        if (SessionManager.getInstance().getAuthToken() == null) {
            DecodedJWT jwt = JWT.decode(SessionManager.getInstance().getAuthToken());
            String username = jwt.getClaim("username").asString();
            saludoLabel.setText("Hola, " + username);
        } else {
            saludoLabel.setText("Hola, Usuario"); // Mensaje por defecto si no hay token
        }
    }

    @FXML
    private void mostrarFormulario() {
        // Alternar visibilidad del formulario
        boolean isVisible = formularioReceta.isVisible();
        formularioReceta.setVisible(!isVisible);
        formularioReceta.setManaged(!isVisible);
    }

    @FXML
    private void guardarReceta() {
        // Obtener datos del formulario
        String nombre = nombreRecetaField.getText();
        String ingredientes = ingredientesField.getText();
        String instrucciones = instruccionesField.getText();
        Long creadorId = null;

        if (SessionManager.getInstance().getAuthToken() != null) {
            DecodedJWT jwt = JWT.decode(SessionManager.getInstance().getAuthToken());
            String subject = jwt.getSubject(); // El subject contiene el ID del usuario (como String)
            try {
                creadorId = Long.parseLong(subject);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el ID del usuario desde el token: " + e.getMessage());
                // Manejar el error apropiadamente
                return;
            }
        } else {
            System.err.println("No hay token de autenticacion disponible.");
            // Manejar el caso en que no hay token
            return;
        }
        // crear la receta ocn los datos recopilados del formulario
        Receta recetaGuardar = Receta.builder()
                .nombre(nombre)
                .ingredientes(ingredientes)
                .instrucciones(instrucciones)
                .creadorId(creadorId)
                .build();

        if (creadorId != null) {
            System.out.println("Receta " + recetaGuardar.getNombre() + " creadorID " + recetaGuardar.getCreadorId());
            inicioService.enviarRecetaAlBackend(recetaGuardar);
        }

        // Limpiar campos y ocultar formulario
        nombreRecetaField.clear();
        ingredientesField.clear();
        instruccionesField.clear();
        formularioReceta.setVisible(false);
        formularioReceta.setManaged(false);
    }
}
