package com.frontend.controlador;

import com.frontend.SessionManager;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

/*
 * Este controlador renderiza las recetas obtenidas del backend
 * y renderizarlas dinamicamente en el GridPane.
 */
public class RecetasController {

    @FXML
    private VBox listaRecetasPane;

    @FXML
    private VBox detallesRecetaPane;

    @FXML
    private Label tituloRecetaLabel;

    @FXML
    private Label nombreRecetaLabel;

    @FXML
    private Label ingredientesRecetaLabel;

    @FXML
    private Label instruccionesRecetaLabel;

    @FXML
    private GridPane recetasGrid;

    // variable para almacenar la receta seleccionada.
    private Receta recetaSeleccionada;

    private final InicioService inicioService = new InicioService();

    @FXML
    public void initialize() {
        cargarRecetas();
    }

    private void cargarRecetas() {
        try {
            // Llamar al servicio para obtener las recetas del usuario
            List<Receta> recetas = inicioService.obtenerRecetasDelUsuario();

            // Renderizar las recetas en el GridPane
            int row = 0;
            int col = 0;
            for (Receta receta : recetas) {
                VBox tarjeta = crearTarjetaReceta(receta);
                recetasGrid.add(tarjeta, col, row);

                col++;
                if (col == 3) { // Máximo 3 tarjetas por fila
                    col = 0;
                    row++;
                }
            }
        } catch (RuntimeException e) {
            // Mostrar el mensaje del backend en la consola
            System.err.println("Error del backend: " + e.getMessage());
            // Mostrar un mensaje amigable al usuario
            Platform.runLater(
                    () -> mostrarAlerta("Error", "No se pudieron cargar las recetas. Por favor, inténtalo de nuevo."));
        } catch (Exception e) {
            // Manejar errores inesperados
            System.err.println("Error inesperado: " + e.getMessage());
            Platform.runLater(
                    () -> mostrarAlerta("Error", "Ocurrió un error inesperado. Por favor, inténtalo más tarde."));
        }
    }

    private VBox crearTarjetaReceta(Receta receta) {
        VBox tarjeta = new VBox();
        tarjeta.setSpacing(10);
        tarjeta.setPadding(new javafx.geometry.Insets(10));
        tarjeta.setStyle(
                "-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        tarjeta.setOnMouseClicked(event -> {
            mostrarDetallesReceta(receta.getNombre(), receta.getIngredientes(), receta.getInstrucciones());
        });

        Label nombreLabel = new Label(receta.getNombre());
        nombreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label ingredientesLabel = new Label("Ingredientes: " + receta.getIngredientes());
        ingredientesLabel.setWrapText(true);

        Label tiempoLabel = new Label(
                "Tiempo: " + receta.getTiempoPreparacion() + " preparación, " + receta.getTiempoCoccion() + " cocción");

        tarjeta.getChildren().addAll(nombreLabel, ingredientesLabel, tiempoLabel);
        return tarjeta;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void volverAlInicio() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) recetasGrid.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Recetas - Inicio");
            stage.show();
        } catch (Exception e) {
            mostrarAlerta("Error", "No se pudo volver al inicio.");
        }
    }

    @FXML
    private void eliminarReceta() {
        if (recetaSeleccionada != null) {
            try {
                // Llamar al servicio para eliminar la receta
                inicioService.eliminarRecetaPorId(recetaSeleccionada.getId()).thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            mostrarAviso("Éxito", "Receta eliminada.");
                            volverALista(); // Volver a la lista después de eliminar
                        } else {
                            mostrarAlerta("Error", "No se pudo eliminar la receta. Por favor, inténtalo de nuevo.");
                        }
                    });
                }).exceptionally(e -> {
                    Platform.runLater(() -> {
                        System.err.println("Error inesperado: " + e.getMessage());
                        mostrarAlerta("Error", "Ocurrió un error inesperado. Por favor, inténtalo más tarde.");
                    });
                    return null;
                });
            } catch (Exception e) {
                System.err.println("Error al eliminar la receta: " + e.getMessage());
                mostrarAlerta("Error", "No se pudo eliminar la receta. Por favor, inténtalo de nuevo.");
            }
        }
    }

    // Método para mostrar los detalles de una receta seleccionada
    public void mostrarDetallesReceta(String nombre, String ingredientes, String instrucciones) {
        // Cargar los datos de la receta en los labels
        nombreRecetaLabel.setText("Nombre: " + nombre);
        ingredientesRecetaLabel.setText("Ingredientes: " + ingredientes);
        instruccionesRecetaLabel.setText("Instrucciones: " + instrucciones);

        // Guardar la receta seleccionada
        recetaSeleccionada = new Receta();
        recetaSeleccionada.setNombre(nombre);
        recetaSeleccionada.setIngredientes(ingredientes);
        recetaSeleccionada.setInstrucciones(instrucciones);

        // Animación para mostrar los detalles
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), listaRecetasPane);
        transition.setToX(-800); // Mover la lista fuera de la vista
        transition.setOnFinished(event -> {
            listaRecetasPane.setVisible(false);
            listaRecetasPane.setManaged(false);
            detallesRecetaPane.setVisible(true);
            detallesRecetaPane.setManaged(true);
        });
        transition.play();
    }

    // Método para volver a la lista de recetas con animación inversa
    @FXML
    private void volverALista() {
        // Animación para ocultar los detalles y volver a la lista
        TranslateTransition transitionDetalles = new TranslateTransition(Duration.millis(300), detallesRecetaPane);
        transitionDetalles.setToX(800); // Mover los detalles fuera de la vista
        transitionDetalles.setOnFinished(event -> {
            detallesRecetaPane.setVisible(false);
            detallesRecetaPane.setManaged(false);

            // Mostrar la lista de recetas con animación
            listaRecetasPane.setVisible(true);
            listaRecetasPane.setManaged(true);
            TranslateTransition transitionLista = new TranslateTransition(Duration.millis(300), listaRecetasPane);
            transitionLista.setToX(0); // Mover la lista de vuelta a su posición original
            transitionLista.play();
        });
        transitionDetalles.play();
    }

    private void mostrarAviso(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText("la eliminacion");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
