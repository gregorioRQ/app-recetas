package com.frontend.controlador;

import java.io.IOError;
import java.io.IOException;

import com.shared.modelos.Receta;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
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

    private Receta receta;

    private Runnable onRecetaEliminada;

    public void setOnRecetaEliminada(Runnable callback) {
        this.onRecetaEliminada = callback;
    }

    @FXML
    void onVerMas(ActionEvent event) {
        abrirReceta(receta);
    }

    public void setDatos(Receta receta) {
        this.receta = receta;
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

    private void abrirReceta(Receta receta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/receta.fxml"));
            VBox nodoRoot = loader.load();
            RecetaController controller = loader.getController();
            controller.setDatos(receta);
            controller.setOnRecetaEliminada(onRecetaEliminada);

            Scene scene = new Scene(nodoRoot);
            Stage stage = new Stage();
            stage.setTitle("Detalle receta");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
