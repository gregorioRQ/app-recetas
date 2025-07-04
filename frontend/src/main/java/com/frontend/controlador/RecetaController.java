package com.frontend.controlador;

import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.util.Duration;

public class RecetaController {
    @FXML
    private Button btnAtras;

    @FXML
    private Button btnEliminar;

    @FXML
    private HBox hboxContainer;

    @FXML
    private ImageView imgView;

    @FXML
    private Label lblIngredientes;

    @FXML
    private Label lblNombreReceta;

    @FXML
    private Label lblTiempoCoccion;

    @FXML
    private Label lblTiempoPreparacion;

    @FXML
    private ScrollPane scrollPaneIngredientes;

    @FXML
    private VBox vBoxContainer;

    @FXML
    private TextFlow textFlowPreparacion;

    @FXML
    private Text text;

    @FXML
    private TextFlow textFlowIngredientes;

    @FXML
    private Text textFlorPreparacionText;

    private Receta receta;
    private InicioService inicioService = new InicioService();
    // esta funcion callback se ejecuta cuando la receta se crea con exito
    // viene de InicioController
    private Runnable onRecetaEliminada;

    public void setOnRecetaEliminada(Runnable callback) {
        this.onRecetaEliminada = callback;
    }

    @FXML
    void onEliminarReceta(ActionEvent event) {
        eliminarReceta(receta.getId());
        mostrarDialogoExito("Receta eliminada");

    }

    @FXML
    void onIrAtras(ActionEvent event) {

    }

    public void setDatos(Receta receta) {
        this.receta = receta;
        lblNombreReceta.setText(receta.getNombre());
        establecerImagen(receta.getPathImg());

        lblTiempoCoccion.setText("Tiempo de cocción: " + receta.getTiempoCoccion().toString());
        lblTiempoPreparacion.setText("Tiempo de preparación: " + receta.getTiempoPreparacion().toString());
        textFlowPreparacion.getChildren().clear();
        text.setText(receta.getInstrucciones());
        textFlowPreparacion.getChildren().add(text);

        textFlowIngredientes.getChildren().clear();
        textFlorPreparacionText.setText(receta.getIngredientes());
        textFlowIngredientes.getChildren().add(textFlorPreparacionText);

    }

    private void establecerImagen(String pathImg) {
        Image image = null;

        if (pathImg != null && !pathImg.isEmpty()) {
            if (pathImg.startsWith("/")) {
                pathImg = "http://localhost:8080" + pathImg;
            }
            try {
                image = new Image(pathImg, true);
                // Si la imagen no se carga, Image.getException() no es null
                if (image.isError()) {
                    image = null;
                }
            } catch (IllegalArgumentException e) {
                image = null;
            }
        }

        if (image == null) {
            // imagen por defecto
            image = new Image(getClass().getResource("/imagenes/imgdefecto.png").toExternalForm());
        }

        imgView.setImage(image);
    }

    private void eliminarReceta(Long id) {
        inicioService.eliminarRecetaPorId(id);
    }

    private void mostrarDialogoExito(String mensaje) {
        VBox contenido = new VBox(10);
        contenido.setAlignment(Pos.CENTER);

        Label iconoLabel = new Label("✓");
        iconoLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: rgb(161, 237, 235);");

        Label mensajeLabel = new Label(mensaje);
        mensajeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white");

        contenido.getChildren().addAll(iconoLabel, mensajeLabel);
        contenido.setStyle(
                "-fx-padding: 20;" +
                        "-fx-background-color: #529a5c; -fx-border-width: 3px;-fx-border-color:  #0d345b");
        contenido.setPrefWidth(300);

        Stage dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setAlwaysOnTop(true);
        dialogStage.setScene(new Scene(contenido));
        dialogStage.show();

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            dialogStage.close();
            ((Stage) btnEliminar.getScene().getWindow()).close();
            onRecetaEliminada.run();
        });
        delay.play();

    }

}
