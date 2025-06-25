package com.frontend.controlador;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.frontend.SessionManager;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
    private Button btnCerrarSesion;

    @FXML
    private Button btnCrearReceta;

    @FXML
    private FlowPane flowPaneContainer;

    @FXML
    private HBox hboxInicioContainer;

    @FXML
    private VBox vboxInicioContainer;

    @FXML
    public void initialize() {
        mostrarSaludo();
        cargarRecetas();
    }

    public void cargarRecetas() {
        flowPaneContainer.getChildren().clear();
        try {
            List<Receta> recetas = inicioService.obtenerRecetasDelUsuario();
            if (recetas.isEmpty()) {
                Label mensaje = new Label("No hay recetas aun, prueba crear una");
                mensaje.setStyle("-fx-font-size: 38px; -fx-text-fill: #aad19e; -fx-font-weight: bold;");
                // Centrar el texto dentro del label
                mensaje.setMaxWidth(Double.MAX_VALUE);
                mensaje.setMaxHeight(Double.MAX_VALUE);
                mensaje.setAlignment(javafx.geometry.Pos.CENTER);

                // Centrar el contenido del FlowPane
                flowPaneContainer.setAlignment(javafx.geometry.Pos.CENTER);
                flowPaneContainer.getChildren().add(mensaje);
            } else {
                for (Receta receta : recetas) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/card.fxml"));
                    Parent rootNode = loader.load();
                    CardController controller = loader.getController();
                    controller.setDatos(receta);
                    controller.setOnRecetaEliminada(this::cargarRecetas);
                    flowPaneContainer.getChildren().add(rootNode);
                }
            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar las recetas.");
        }

    }

    private void mostrarSaludo() {
        if (SessionManager.getInstance().getAuthToken() != null) {
            DecodedJWT jwt = JWT.decode(SessionManager.getInstance().getAuthToken());
            String username = jwt.getClaim("nombre").asString();
            saludoLabel.setText("Hola, " + username);
        } else {
            saludoLabel.setText("Hola, Usuario");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    void onCerrarSesion(ActionEvent event) {
        inicioService.cerrarSesion().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    // Redirigir al usuario a la pantalla de inicio de sesión
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) saludoLabel.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Inicio de Sesión");
                        stage.show();
                    } catch (IOException e) {
                        mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.");
                        e.printStackTrace();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo cerrar la sesión. Por favor, inténtalo de nuevo.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Ocurrió un error inesperado al cerrar la sesión.");
            });
            return null;
        });
    }

    @FXML
    void onCrearReceta(ActionEvent event) {
        abrirFormulario();
    }

    private void abrirFormulario() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/formularioCrearReceta.fxml"));
            Parent root = loader.load();

            // Pasa el callback al controlador del formulario
            FormularioCrearRecetaController controller = loader.getController();
            controller.setOnRecetaCreada(this::cargarRecetas);

            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.setTitle("Crear receta");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
