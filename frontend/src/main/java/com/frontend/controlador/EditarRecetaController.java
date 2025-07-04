package com.frontend.controlador;

import java.io.File;
import java.time.LocalTime;

import com.frontend.AppConfig;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
 *  maneja el formulario de edicion de la receta
 */
public class EditarRecetaController {
    @FXML
    private TextField nombreField;
    @FXML
    private TextArea ingredientesArea;
    @FXML
    private TextArea instruccionesArea;
    @FXML
    private ComboBox<Integer> horasPreparacionComboBox;
    @FXML
    private ComboBox<Integer> minutosPreparacionComboBox;
    @FXML
    private ComboBox<Integer> horasCoccionComboBox;
    @FXML
    private ComboBox<Integer> minutosCoccionComboBox;
    @FXML
    private TextField porcionesField;
    @FXML
    private ImageView imagenPreview;
    private File nuevaImagenFile;

    private Receta recetaOriginal;
    private final InicioService inicioService = new InicioService();
    private RecetasController recetasController;

    @FXML
    public void initialize() {
        // Inicializar ComboBoxes
        for (int i = 0; i < 24; i++) {
            horasPreparacionComboBox.getItems().add(i);
            horasCoccionComboBox.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            minutosPreparacionComboBox.getItems().add(i);
            minutosCoccionComboBox.getItems().add(i);
        }
    }

    // genera un selector de archivos
    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));

        File selectedFile = fileChooser.showOpenDialog(nombreField.getScene().getWindow());
        // muestra una previsualización de la imagen seleccionada
        if (selectedFile != null) {
            try {
                Image imagen = new Image(selectedFile.toURI().toString());
                imagenPreview.setImage(imagen);
                nuevaImagenFile = selectedFile;
            } catch (Exception e) {
                mostrarAlerta("Error", "No se pudo cargar la imagen seleccionada");
            }
        }
    }

    public void setReceta(Receta receta, RecetasController controller) {
        this.recetaOriginal = receta;
        this.recetasController = controller;

        // Cargar los datos de la receta en el formulario de edicion
        nombreField.setText(receta.getNombre());
        ingredientesArea.setText(receta.getIngredientes());
        instruccionesArea.setText(receta.getInstrucciones());

        // Cargar tiempos
        horasPreparacionComboBox.setValue(receta.getTiempoPreparacion().getHour());
        minutosPreparacionComboBox.setValue(receta.getTiempoPreparacion().getMinute());
        horasCoccionComboBox.setValue(receta.getTiempoCoccion().getHour());
        minutosCoccionComboBox.setValue(receta.getTiempoCoccion().getMinute());

        porcionesField.setText(String.valueOf(receta.getPorciones()));

        // Cargar imagen actual si existe
        if (receta.getPathImg() != null && !receta.getPathImg().isEmpty()) {
            String imageUrl = AppConfig.getBackendBaseUrl() + receta.getPathImg();
            imagenPreview.setImage(new Image(imageUrl));
        }
    }

    @FXML
    private void confirmarEdicion() {
        try {
            Receta recetaEditada = Receta.builder()
                    .id(recetaOriginal.getId())
                    .nombre(nombreField.getText())
                    .ingredientes(ingredientesArea.getText())
                    .instrucciones(instruccionesArea.getText())
                    .tiempoPreparacion(LocalTime.of(
                            horasPreparacionComboBox.getValue(),
                            minutosPreparacionComboBox.getValue()))
                    .tiempoCoccion(LocalTime.of(
                            horasCoccionComboBox.getValue(),
                            minutosCoccionComboBox.getValue()))
                    .porciones(Integer.parseInt(porcionesField.getText()))
                    .creadorId(recetaOriginal.getCreadorId())
                    .build();

            inicioService.actualizarReceta(recetaEditada, nuevaImagenFile).thenAccept(response -> {
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        mostrarAviso("Éxito", "Receta actualizada correctamente");
                        // Actualizar la lista de recetas
                        recetasController.cargarRecetas();
                        // Cerrar la ventana de edición
                        ((Stage) nombreField.getScene().getWindow()).close();
                    } else {
                        mostrarAlerta("Error", "No se pudo actualizar la receta");
                    }
                });

            }).exceptionally(e -> {
                Platform.runLater(() -> {
                    System.err.println("Error inesperado: " + e.getMessage());
                    mostrarAlerta("Error", "Ocurrió un error inesperado al actualizar la receta");
                });
                return null;
            });
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al actualizar la receta: " + e.getMessage());
        }
    }

    @FXML
    private void cancelarEdicion() {
        ((Stage) nombreField.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAviso(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
