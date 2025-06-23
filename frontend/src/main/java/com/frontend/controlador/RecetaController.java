package com.frontend.controlador;

import com.shared.modelos.Receta;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class RecetaController {
    @FXML
    private Button btnAtras;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnModificar;

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
    void onEliminarReceta(ActionEvent event) {

    }

    @FXML
    void onIrAtras(ActionEvent event) {

    }

    @FXML
    void onModificarReceta(ActionEvent event) {

    }

    public void setDatos(Receta receta) {
        lblNombreReceta.setText(receta.getNombre());
        establecerImagen(receta.getPathImg());
        lblIngredientes.setText(receta.getIngredientes());
        lblTiempoCoccion.setText("Tiempo de cocción: " + receta.getTiempoCoccion().toString());
        lblTiempoPreparacion.setText("Tiempo de preparación: " + receta.getTiempoPreparacion().toString());
        textFlowPreparacion.getChildren().clear();

        text.setText(receta.getInstrucciones());
        textFlowPreparacion.getChildren().add(text);

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
}
