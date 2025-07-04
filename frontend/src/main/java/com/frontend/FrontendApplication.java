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
        Scene scene = new Scene(fxmlLoader.load(), 600, 600);
        stage.setTitle("Recetas - login");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Throwable t) {
            System.err.println("Error en la aplicación:");
            t.printStackTrace();
            esperarEnter(); // Pausar si hay error
        }
    }

    private static void esperarEnter() {
        System.out.println("Presioná ENTER para cerrar...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
