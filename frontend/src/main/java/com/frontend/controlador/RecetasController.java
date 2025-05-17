package com.frontend.controlador;

import com.frontend.SessionManager;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

/*
 * Este controlador renderiza las recetas obtenidas del backend
 * y renderizarlas dinamicamente en el GridPane.
 */
public class RecetasController {

    @FXML
    private GridPane recetasGrid;

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
}
