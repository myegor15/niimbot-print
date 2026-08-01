package xyz.melnychuk.niimprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import xyz.melnychuk.niimprint.rest.NiimServer;

import java.io.IOException;
import java.util.Objects;

public class NiimPrintApplication extends Application {
    private NiimServer server;

    @Override
    public void start(Stage stage) throws IOException {
        startServer();
        Scene scene = getScene();
        stage.setTitle("Niim Print");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void startServer() {
        try {
            server = NiimServer.start();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось запустить встроенный сервер печати: " + e.getMessage()
                            + "\n\nВыполните `mvn generate-resources` для сборки runtime.")
                    .show();
        }
    }

    private Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NiimPrintApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        MainController controller = fxmlLoader.getController();
        if (server != null) {
            controller.setServerBaseUrl(server.getBaseUrl());
        }

        String style = Objects.requireNonNull(NiimPrintApplication.class.getResource("style.css")).toExternalForm();
        scene.getStylesheets().add(style);

        return scene;
    }
}
