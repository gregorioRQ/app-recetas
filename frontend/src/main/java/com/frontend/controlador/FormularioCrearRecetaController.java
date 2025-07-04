package com.frontend.controlador;

import java.io.File;
import java.time.LocalTime;
import java.util.ResourceBundle;

import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.frontend.SessionManager;
import com.frontend.servicio.InicioService;
import com.shared.modelos.Receta;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FormularioCrearRecetaController {

    private final InicioService inicioService;
    private ValidationSupport validationSupport;
    private InicioController inicioController = new InicioController();

    // constructor sin armumentos para javaFX e inicializacion de campos final
    public FormularioCrearRecetaController() {
        this.inicioService = new InicioService();
    }

    @FXML
    private VBox VboxFormularioRecetaContainer;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnSelecImg;

    @FXML
    private ComboBox<Integer> comboBoxHorasCoccion;

    @FXML
    private ComboBox<Integer> comboBoxHorasPreparacion;

    @FXML
    private ComboBox<Integer> comboBoxMinutosCoccion;

    @FXML
    private ComboBox<Integer> comboBoxMinutosPreparacion;

    @FXML
    private GridPane gridPaneContainer;

    @FXML
    private Label lblImagenPlatillo;

    @FXML
    private Label lblIngredientes;

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblPorciones;

    @FXML
    private Label lblPreparacion;

    @FXML
    private Label lblTiempoCoccion;

    @FXML
    private Label lblTiempoPreparacion;

    @FXML
    private TextArea textAreaIngredientes;

    @FXML
    private TextArea textAreaPreparacion;

    @FXML
    private TextField textFielNombre;

    @FXML
    private TextField textFieldImgSelec;

    @FXML
    private TextField textFieldPorciones;

    private File imagenSeleccionada;

    // esta funcion callback se ejecuta cuando la receta se crea con exito
    // viene de InicioController
    private Runnable onRecetaCreada;

    public void setOnRecetaCreada(Runnable callback) {
        this.onRecetaCreada = callback;
    }

    @FXML
    private void initialize() {
        // Inicializar ComboBox para horas (0-23)
        ObservableList<Integer> horas = FXCollections.observableArrayList();
        for (int i = 0; i < 24; i++) {
            horas.add(i);
        }
        comboBoxHorasPreparacion.setItems(horas);
        comboBoxHorasCoccion.setItems(horas);

        // Inicializar ComboBox para minutos (0-59)
        ObservableList<Integer> minutos = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i++) {
            minutos.add(i);
        }
        comboBoxMinutosPreparacion.setItems(minutos);
        comboBoxMinutosCoccion.setItems(minutos);

        // Establecer valores predeterminados
        comboBoxHorasPreparacion.setValue(0);
        comboBoxMinutosPreparacion.setValue(0);
        comboBoxHorasCoccion.setValue(0);
        comboBoxMinutosCoccion.setValue(0);

        configurarValidaciones();
    }

    @FXML
    void onGuardarReceta(ActionEvent event) {

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
            String nombre = textFielNombre.getText();
            String ingredientes = textAreaIngredientes.getText();
            String instrucciones = textAreaPreparacion.getText();

            // Construir LocalTime para tiempo de preparación
            LocalTime tiempoPreparacion = LocalTime.of(
                    comboBoxHorasPreparacion.getValue(),
                    comboBoxMinutosPreparacion.getValue());

            // Construir LocalTime para tiempo de cocción
            LocalTime tiempoCoccion = LocalTime.of(
                    comboBoxHorasCoccion.getValue(),
                    comboBoxMinutosCoccion.getValue());

            Integer porciones = Integer.parseInt(textFieldPorciones.getText());

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
                            mostrarDialogoExito("Receta creada!");

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

    @FXML
    void onIrAtras(ActionEvent event) {

    }

    @FXML
    void onSeleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen");

        // Configurar filtros para archivos de imagen
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de imagen", "*.png", "*.jpg",
                "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        // Mostrar el diálogo de selección de archivo
        File file = fileChooser.showOpenDialog(textFieldImgSelec.getScene().getWindow());

        if (file != null) {

            // Validar tamaño (ejemplo: mínimo 500x500)
            javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString());
            if (img.getWidth() < 500 || img.getHeight() < 500) {
                mostrarAlerta("Imagen pequeña",
                        "La imagen seleccionada es muy pequeña. Elige una de al menos 500x500 píxeles y en formato JPG, JPEG, PNG.");
                return;
            }

            imagenSeleccionada = file;
            textFieldImgSelec.setText(file.getName());
        }
    }

    private void configurarValidaciones() {
        validationSupport = new ValidationSupport();
        validationSupport.setValidationDecorator(new StyleClassValidationDecoration());

        // validacion para el nombre
        validationSupport.registerValidator(textFielNombre, true,
                Validator.createEmptyValidator("El 'nombre de la receta' es obligatorio"));

        validationSupport.registerValidator(textFielNombre, (control, value) -> {
            String nombre = (String) value;
            if (nombre != null && nombre.length() > 100) {
                return ValidationResult.fromError(textFielNombre,
                        "El campo 'nombre de la receta' no puede exceder los 100 caracteres.");
            }
            if (nombre != null && !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                return ValidationResult.fromError(textFielNombre,
                        "El campo 'nombre de la receta' solo puede contener letras y espacios.");
            }
            return ValidationResult.fromResults();
        });

        // valida los ingredientes
        validationSupport.registerValidator(textAreaIngredientes, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'ingredientes' es obligatorio"),
                Validator.createPredicateValidator(texto -> texto.toString().trim().length() <= 390,
                        "El campo 'ingredientes' debe tener menos de 390 caracteres")));

        // valida las instrucciones
        validationSupport.registerValidator(textAreaPreparacion, true, Validator.combine(
                Validator.createEmptyValidator("El campo 'instrucciones' es obligatorio"),
                Validator.createPredicateValidator(texto -> texto.toString().length() <= 4000,
                        "El campo 'instrucciones' debe tener menos de 4000 caracteres")));

        // Validación porciones
        validationSupport.registerValidator(textFieldPorciones, true,
                Validator.createEmptyValidator("El campo 'porciones' es obligatorio"));
        validationSupport.registerValidator(textFieldPorciones, true,
                Validator.createPredicateValidator(texto -> texto.toString().length() < 4,
                        "El campo 'porciones' no debe exceder numeros de 4 cifras"));
        validationSupport.registerValidator(textFieldPorciones, true, Validator.createPredicateValidator(
                texto -> {
                    if (texto == null || texto.toString().trim().isEmpty())
                        return false;
                    String porciones = texto.toString();
                    return porciones.matches("^\\d+$") &&
                            porciones.equals(porciones.trim());
                },
                "El campo 'porciones' debe ser un número válido sin espacios"));

        // Validación de imagen
        validationSupport.registerValidator(textFieldImgSelec, false,
                Validator.createPredicateValidator(this::validarFormatoImagen,
                        "La imagen es obligatoria y solo se permiten imágenes en formato JPG, JPEG, PNG"));
    }

    private boolean validarFormatoImagen(Object archivo) {
        if (archivo == null)
            return false;
        String nombreArchivo = archivo.toString().toLowerCase();
        return nombreArchivo.endsWith(".jpg") ||
                nombreArchivo.endsWith(".jpeg") ||
                nombreArchivo.endsWith(".png");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
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
            ((Stage) btnGuardar.getScene().getWindow()).close();
            onRecetaCreada.run();
        });
        delay.play();
    }

}
