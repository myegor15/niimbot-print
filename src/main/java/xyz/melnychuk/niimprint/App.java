package xyz.melnychuk.niimprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import xyz.melnychuk.niimblue.NiimBlueServer;
import xyz.melnychuk.niimprint.controller.MainController;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private NiimBlueServer server;

    @Override
    public void start(Stage stage) throws IOException {
        startNiimBlue();
        startUI(stage);
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void startNiimBlue() {
        try {
            server = NiimBlueServer.start();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Не удалось запустить встроенный сервер печати: " + e.getMessage()
                            + "\n\nВыполните `mvn generate-resources` для сборки runtime.")
                    .show();
        }
    }

    private void startUI(Stage stage) throws IOException {
        stage.setScene(getScene());
        stage.setTitle("NiimBot Print");
        stage.show();
    }

    private Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        MainController controller = fxmlLoader.getController();
        if (server != null) {
            controller.setServerBaseUrl(server.getUrl());
        }

        String style = Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm();
        scene.getStylesheets().add(style);

        return scene;
    }
}
