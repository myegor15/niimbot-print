package xyz.melnychuk.niimprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import xyz.melnychuk.niimblue.NiimBlueServer;
import xyz.melnychuk.niimprint.controller.MainController;
import xyz.melnychuk.niimprint.service.PrintService;
import xyz.melnychuk.niimprint.service.StickerService;
import xyz.melnychuk.niimprint.util.AsyncUtils;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private NiimBlueServer server;

    @Override
    public void start(Stage stage) {
        startUi(stage);
        startNiimBlue(stage);
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void startUi(Stage stage) {
        stage.setTitle("NiimBot Print");
        stage.setScene(createSplashScene());
        stage.show();
    }

    private Scene createSplashScene() {
        VBox box = new VBox(
                16,
                new Label("Запуск приложения..."),
                new ProgressIndicator()
        );
        box.setAlignment(Pos.CENTER);
        return new Scene(box, 400, 200);
    }

    private void startNiimBlue(Stage stage) {
        AsyncUtils.run(
                NiimBlueServer::start,
                e -> {
                    server = e;
                    showMain(stage);
                },
                e -> showErrorAlert("Не удалось запустить встроенный сервер печати: " + e.getMessage())
        );
    }

    private void showMain(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

            MainController controller = fxmlLoader.getController();
            controller.setPrintService(new PrintService(server.getUrl()));
            controller.setStickerService(new StickerService());

            String style = Objects.requireNonNull(App.class.getResource("style.css")).toExternalForm();
            scene.getStylesheets().add(style);

            stage.setScene(scene);
        } catch (IOException e) {
            showErrorAlert("Не удалось открыть главное окно: " + e.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }
}
