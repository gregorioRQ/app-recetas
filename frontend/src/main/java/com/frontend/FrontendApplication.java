package com.frontend;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FrontendApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(FrontendApplication.class.getResource("/vistas/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600); // Dimensiones iniciales
        stage.setTitle("Recetas");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
