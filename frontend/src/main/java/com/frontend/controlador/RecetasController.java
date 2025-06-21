package com.frontend.controlador;

import com.frontend.AppConfig;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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

    /*
     * actualiza la vista que muestra la receta con todos sus detalles cuando
     * el usuario edita alguno de sus campos.
     */
    public void actualizarDetallesReceta(Receta receta) {
        Platform.runLater(() -> {
            nombreRecetaLabel.setText("Nombre: " + receta.getNombre());
            ingredientesRecetaLabel.setText("Ingredientes: " + receta.getIngredientes());
            instruccionesRecetaLabel.setText("Instrucciones: " + receta.getInstrucciones());
            // Actualizar la receta seleccionada con los nuevos datos
            recetaSeleccionada = receta;
        });
    }

    public void cargarRecetas() {
        try {

            // Guardar el ID de la receta seleccionada actualmente (si existe)
            Long recetaSeleccionadaId = recetaSeleccionada != null ? recetaSeleccionada.getId() : null;

            // Limpiar el grid antes de cargar nuevas recetas
            recetasGrid.getChildren().clear();

            // Llamar al servicio para obtener las recetas del usuario
            List<Receta> recetas = inicioService.obtenerRecetasDelUsuario();
            if (recetas.isEmpty()) {
                // Mostrar un mensaje amigable si no hay recetas
                Label mensaje = new Label("No has creado ninguna receta todavía");
                mensaje.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555;");
                recetasGrid.add(mensaje, 0, 0);
                GridPane.setColumnSpan(mensaje, 3);
            } else {
                // Renderizar las recetas en el GridPane
                int row = 0;
                int col = 0;
                for (Receta receta : recetas) {
                    VBox tarjeta = crearTarjetaReceta(receta);
                    recetasGrid.add(tarjeta, col, row);

                    // Si esta es la receta que estaba seleccionada, actualizar sus detalles
                    if (recetaSeleccionadaId != null && receta.getId().equals(recetaSeleccionadaId)) {
                        actualizarDetallesReceta(receta);
                    }

                    col++;
                    if (col == 3) { // Máximo 3 tarjetas por fila
                        col = 0;
                        row++;
                    }
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

    /*
     * crea una previsualizacion para cada receta añadiendo algunos datos
     * y metodos
     */
    private VBox crearTarjetaReceta(Receta receta) {
        VBox tarjeta = new VBox();
        tarjeta.setSpacing(10);
        tarjeta.setPadding(new javafx.geometry.Insets(10));
        tarjeta.setStyle(
                "-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5;");
        tarjeta.setOnMouseClicked(event -> {
            mostrarDetallesReceta(receta);
        });

        // crear y ajusta un ImagenView para la imagen de la receta
        ImageView imagenView = new ImageView();
        imagenView.setFitWidth(200);
        imagenView.setFitHeight(150);
        imagenView.setPreserveRatio(true);

        // carga la imagen si existe la ruta
        if (receta.getPathImg() != null && !receta.getPathImg().isEmpty()) {
            try {
                String baseUrl = AppConfig.getBackendBaseUrl();
                String imagePath = receta.getPathImg();

                // Asegurarse de que el path comience con /
                if (!imagePath.startsWith("/")) {
                    imagePath = "/" + imagePath;
                }

                String imagenUrl = baseUrl + imagePath;

                System.out.println("URL de imagen codificada: " + imagenUrl);

                Image imagen = new Image(imagenUrl, true);
                imagenView.setImage(imagen);

                // Manejar errores de carga
                imagen.errorProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        System.err.println("Error cargando imagen: " + imagen.getException());
                        Platform.runLater(() -> cargarImagenPorDefecto(imagenView));
                    }
                });

                // Manejar carga exitosa
                imagen.progressProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue.doubleValue() == 1.0) {
                        System.out.println("Imagen cargada exitosamente: " + imagenUrl);
                    }
                });

                imagenView.setImage(imagen);

            } catch (Exception e) {
                System.err.println("No se pudo cargar la imagen: " + e.getMessage());
                cargarImagenPorDefecto(imagenView);
            }
        } else {
            cargarImagenPorDefecto(imagenView);
        }

        Label nombreLabel = new Label(receta.getNombre());
        nombreLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        tarjeta.getChildren().addAll(imagenView, nombreLabel);

        // Configurar el crecimiento vertical
        VBox.setVgrow(imagenView, Priority.ALWAYS);
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
                            // Limpiar el GridPane
                            recetasGrid.getChildren().clear();
                            // Recarga las recetas
                            cargarRecetas();
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

    /*
     * abre la vista editar-receta y envia los datos (receta y una instancia
     * del controlador)
     */
    @FXML
    private void editarReceta() {
        if (recetaSeleccionada != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/editar-receta.fxml"));
                Parent root = loader.load();

                EditarRecetaController editarController = loader.getController();
                editarController.setReceta(recetaSeleccionada, this);

                Stage stage = new Stage();
                stage.setTitle("Editar Receta");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                mostrarAlerta("Error", "No se pudo abrir el formulario de edición");
                e.printStackTrace();
            }
        }
    }

    // Renderiza los detalles completos de la receta cuando el usuario
    // hace clic en la receta previsualizada
    public void mostrarDetallesReceta(Receta receta) {
        // Cargar los datos de la receta en los labels
        nombreRecetaLabel.setText("Nombre: " + receta.getNombre());
        ingredientesRecetaLabel.setText("Ingredientes: " + receta.getIngredientes());
        instruccionesRecetaLabel.setText("Instrucciones: " + receta.getInstrucciones());

        // Guardar la receta seleccionada
        recetaSeleccionada = receta;

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
        // Limpiar la receta seleccionada
        recetaSeleccionada = null;

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

    private void cargarImagenPorDefecto(ImageView imageView) {
        try {
            Image defaultImage = new Image(getClass().getResourceAsStream("/resources/imagenes/no-imagen.png"));
            imageView.setImage(defaultImage);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen por defecto: " + e.getMessage());
        }
    }
}
