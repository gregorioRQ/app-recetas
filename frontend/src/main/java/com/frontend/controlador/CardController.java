package com.frontend.controlador;

import com.shared.modelos.Receta;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CardController {
    @FXML
    private Button btnVerMas;

    @FXML
    private ImageView imgView;

    @FXML
    private Label lblNombre;

    @FXML
    private VBox vboxContainer;

    @FXML
    void onVerMas(ActionEvent event) {

    }

    public void setDatos(Receta receta) {
        lblNombre.setText(receta.getNombre());
        establecerImagen(receta.getPathImg());
    }

    public void establecerImagen(String pathImg) {
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
