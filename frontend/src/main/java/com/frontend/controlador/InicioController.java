package com.frontend.controlador;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.frontend.SessionManager;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

public class InicioController {

    private final InicioService inicioService;
    private ValidationSupport validationSupport;

    // constructor sin armumentos para javaFX e inicializacion de campos final
    public InicioController() {
        this.inicioService = new InicioService();
    }

    // constructor con argumentos para pruebas unitarias
    public InicioController(InicioService inicioService) {
        this.inicioService = inicioService;
    }

    @FXML
    private Label saludoLabel;

    @FXML
    private VBox formularioReceta;

    @FXML
    private Button crearRecetaButton;

    @FXML
    private Button misRecetasButton;

    @FXML
    private TextField nombreRecetaField;

    @FXML
    private TextField ingredientesField;

    @FXML
    private TextField instruccionesField;

    @FXML
    private TextField tiempoPreparacionField;

    @FXML
    private TextField tiempoCoccionField;

    @FXML
    private TextField porcionesField;

    @FXML
    private TextField imagenPathField;

    private File imagenSeleccionada;

    @FXML
    private ComboBox<Integer> horasPreparacionComboBox;
    @FXML
    private ComboBox<Integer> minutosPreparacionComboBox;
    @FXML
    private ComboBox<Integer> horasCoccionComboBox;
    @FXML
    private ComboBox<Integer> minutosCoccionComboBox;

    @FXML
    private Button guardarRecetaButton;

    @FXML
    private Button cerrarSesionButton;

    @FXML
    public void initialize() {
        // Inicializar ComboBox para horas (0-23)
        ObservableList<Integer> horas = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            horas.add(i);
        }
        horasPreparacionComboBox.setItems(horas);
        horasCoccionComboBox.setItems(horas);

        // Inicializar ComboBox para minutos (0-59)
        ObservableList<Integer> minutos = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutos.add(i);
        }
        minutosPreparacionComboBox.setItems(minutos);
        minutosCoccionComboBox.setItems(minutos);

        // Establecer valores predeterminados
        horasPreparacionComboBox.setValue(0);
        minutosPreparacionComboBox.setValue(0);
        horasCoccionComboBox.setValue(0);
        minutosCoccionComboBox.setValue(0);

        mostrarSaludo();
        configurarValidaciones();
    }

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());

        // validacion para el nombre
        validationSupport.registerValidator(nombreRecetaField, true,
                Validator.createEmptyValidator("El 'nombre de la receta' es obligatorio"));

        validationSupport.registerValidator(nombreRecetaField, (control, value) -> {
            String nombre = (String) value;
            if (nombre != null && nombre.length() > 50) {
                return ValidationResult.fromError(nombreRecetaField,
                        "El campo 'nombre de la receta' no puede exceder los 50 caracteres.");
            }
            if (nombre != null && !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                return ValidationResult.fromError(nombreRecetaField,
                        "El campo 'nombre de la receta' solo puede contener letras y espacios.");
            }
            return ValidationResult.fromResults();
        });

        // valida los ingredientes
        validationSupport.registerValidator(ingredientesField, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'ingredientes' es obligatorio"),
                Validator.createPredicateValidator(texto -> texto.toString().trim().length() <= 200,
                        "El campo 'ingredientes' debe tener menos de 200 caracteres")));

        // valida las instrucciones
        validationSupport.registerValidator(instruccionesField, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'instrucciones' es obligatorio"),
                Validator.createPredicateValidator(texto -> texto.toString().length() <= 399,
                        "El campo 'instrucciones' debe tener menos de 400 caracteres")));

        // Validación porciones
        validationSupport.registerValidator(porcionesField, true,
                Validator.createEmptyValidator("El campo 'porciones' es obligatorio"));
        validationSupport.registerValidator(porcionesField, true, Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.toString().trim().isEmpty())
                        return false;
                    String porciones = texto.toString();
                    return porciones.matches("^\\d+$") &&
                            porciones.equals(porciones.trim());
                },
                "El campo 'porciones' debe ser un número válido sin espacios"));

        // Validación de imagen
        validationSupport.registerValidator(imagenPathField, false,
                Validator.createPredicateValidator(this::validarFormatoImagen,
                        "La imagen es obligatoria y solo se permiten imágenes en formato JPG, JPEG, PNG"));
    }

    private void mostrarSaludo() {
        if (SessionManager.getInstance().getAuthToken() != null) {
            DecodedJWT jwt = JWT.decode(SessionManager.getInstance().getAuthToken());
            String username = jwt.getClaim("nombre").asString();
            saludoLabel.setText("Hola, " + username);
        } else {
            saludoLabel.setText("Hola, Usuario");
        }
    }

    @FXML
    private void mostrarFormulario() {
        // Mostrar el formulario
        formularioReceta.setVisible(true);
        formularioReceta.setManaged(true);

        // Ocultar los botones "Crear receta" y "Mis recetas"
        crearRecetaButton.setVisible(false);
        crearRecetaButton.setManaged(false);
        misRecetasButton.setVisible(false);
        misRecetasButton.setManaged(false);
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");

        // Configurar filtros para archivos de imagen
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de imagen", "*.png", "*.jpg",
                "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        // Mostrar el diálogo de selección de archivo
        File file = fileChooser.showOpenDialog(imagenPathField.getScene().getWindow());

        if (file != null) {
            imagenSeleccionada = file;
            imagenPathField.setText(file.getName());
        }
    }

    @FXML
    private void guardarReceta() {
        if (validationSupport.isInvalid()) {
            // Mostrar mensaje de alerta con los errores
            StringBuilder errores = new StringBuilder();
            validationSupport.getValidationResult().getErrors().forEach(validationMessage -> {
                errores.append("- ").append(validationMessage.getText()).append("\n");
            });
            mostrarAlerta("Error de validación", "Por favor, corrige los siguientes errores:\n\n" + errores.toString());
            return;
        }

        try {
            // Obtener datos del formulario
            String nombre = nombreRecetaField.getText();
            String ingredientes = ingredientesField.getText();
            String instrucciones = instruccionesField.getText();

            // Construir LocalTime para tiempo de preparación
            LocalTime tiempoPreparacion = LocalTime.of(
                    horasPreparacionComboBox.getValue(),
                    minutosPreparacionComboBox.getValue());

            // Construir LocalTime para tiempo de cocción
            LocalTime tiempoCoccion = LocalTime.of(
                    horasCoccionComboBox.getValue(),
                    minutosCoccionComboBox.getValue());

            Integer porciones = Integer.parseInt(porcionesField.getText());

            Long creadorId = null;

            if (SessionManager.getInstance().getAuthToken() != null) {
                DecodedJWT jwt = JWT.decode(SessionManager.getInstance().getAuthToken());

                try {
                    creadorId = jwt.getClaim("id").asLong();
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear el ID del usuario desde el token: " + e.getMessage());
                    // mensaje para la ui
                    mostrarAlerta("Error", "No se pudo identificar al usuario. Por favor, inténtalo de nuevo.");
                    return;
                }
            } else {
                System.err.println("No hay token de autenticacion disponible.");
                // mensaje para la ui
                mostrarAlerta("Error", "No estás autenticado. Por favor, inicia sesión.");

                return;
            }

            // Crear la receta con los datos recopilados del formulario
            Receta recetaGuardar = Receta.builder()
                    .nombre(nombre)
                    .ingredientes(ingredientes)
                    .instrucciones(instrucciones)
                    .tiempoPreparacion(tiempoPreparacion)
                    .tiempoCoccion(tiempoCoccion)
                    .porciones(porciones)
                    .creadorId(creadorId)
                    .build();

            if (creadorId != null) {
                inicioService.enviarRecetaAlBackend(recetaGuardar, imagenSeleccionada).thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 201) {
                            mostrarDialogoExito("Tu receta se ha creado exitosamente!");
                            limpiarFormulario();
                        } else {
                            // Mostrar el error del backend en la consola
                            System.err.println("Error del backend: " + response.body());
                            // Mostrar un mensaje amigable al usuario
                            mostrarAlerta("Error", "No se pudo crear la receta. Por favor, inténtalo de nuevo.");
                        }
                    });
                }).exceptionally(e -> {
                    Platform.runLater(() -> {
                        System.err.println("Error inesperado: " + e.getMessage());
                        mostrarAlerta("Error", "Ocurrió un error inesperado. Por favor, inténtalo más tarde.");
                    });
                    return null;
                });
            }
        } catch (Exception e) {
            System.err.println("Error al guardar la receta: " + e.getMessage());
            mostrarAlerta("Error", "Por favor, verifica los datos ingresados.");
        }

    }

    private void limpiarFormulario() {
        // Limpiar campos y ocultar formulario
        nombreRecetaField.clear();
        ingredientesField.clear();
        instruccionesField.clear();
        horasPreparacionComboBox.setValue(0);
        minutosPreparacionComboBox.setValue(0);
        horasCoccionComboBox.setValue(0);
        minutosCoccionComboBox.setValue(0);
        porcionesField.clear();
        imagenPathField.clear();
        imagenSeleccionada = null;

        // Ocultar el formulario y volver a mostrar los botones.
        formularioReceta.setVisible(false);
        formularioReceta.setManaged(false);
        crearRecetaButton.setVisible(true);
        crearRecetaButton.setManaged(true);
        misRecetasButton.setVisible(true);
        misRecetasButton.setManaged(true);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarDialogoExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("¡Éxito!");
        alert.setHeaderText(null);

        // Crear un contenedor personalizado
        VBox contenido = new VBox(10);
        contenido.setAlignment(Pos.CENTER);

        Label iconoLabel = new Label("✓");
        iconoLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #2ecc71;");

        // Mensaje
        Label mensajeLabel = new Label(mensaje);
        mensajeLabel.setStyle("-fx-font-size: 14px;");

        // Botón personalizado
        Button okButton = new Button("Aceptar");
        okButton.setStyle(
                "-fx-background-color: #2ecc71;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;");

        okButton.setOnAction(e -> {
            alert.setResult(ButtonType.OK);
            alert.close();
        });

        contenido.getChildren().addAll(iconoLabel, mensajeLabel, okButton);
        contenido.setStyle("-fx-padding: 20;");

        alert.getDialogPane().setContent(contenido);
        alert.showAndWait();
    }

    // Redirige a la pantalla "recetas.fxml"
    @FXML
    private void mostrarRecetas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/recetas.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) saludoLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mis Recetas");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la pantalla de recetas.");
            e.printStackTrace();
        }
    }

    @FXML
    private void volverAtras() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/inicio.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) saludoLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Inicio");
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo cargar la pantalla de login.");
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion() {
        inicioService.cerrarSesion().thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.statusCode() == 200) {
                    // Redirigir al usuario a la pantalla de inicio de sesión
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vistas/login.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) saludoLabel.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Inicio de Sesión");
                        stage.show();
                    } catch (IOException e) {
                        mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio de sesión.");
                        e.printStackTrace();
                    }
                } else {
                    mostrarAlerta("Error", "No se pudo cerrar la sesión. Por favor, inténtalo de nuevo.");
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> {
                mostrarAlerta("Error", "Ocurrió un error inesperado al cerrar la sesión.");
            });
            return null;
        });
    }

    private boolean validarFormatoImagen(Object archivo) {
        if (archivo == null)
            return false;
        String nombreArchivo = archivo.toString().toLowerCase();
        return nombreArchivo.endsWith(".jpg") ||
                nombreArchivo.endsWith(".jpeg") ||
                nombreArchivo.endsWith(".png");
    }

}
