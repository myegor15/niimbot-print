package xyz.melnychuk.niimprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueServer;
import xyz.melnychuk.niimprint.controller.MainController;
import xyz.melnychuk.niimprint.service.PrintService;
import xyz.melnychuk.niimprint.service.StickerService;
import xyz.melnychuk.niimprint.util.AsyncUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@Slf4j
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
        showSplash(stage);
        stage.show();
    }

    private void startNiimBlue(Stage stage) {
        AsyncUtils.run(
                NiimBlueServer::start,
                e -> {
                    server = e;
                    showMain(stage);
                },
                e -> showError("Exception in startNiimBlue().", "Не удалось запустить встроенный сервер печати: ", e)
        );
    }

    private void showSplash(Stage stage) {
        try {
            FXMLLoader loader = getLoader("splash-view.fxml");
            stage.setScene(new Scene(loader.load(), 400, 200));
        } catch (IOException e) {
            showError("Exception in showSplash().", "Не удалось открыть окно загрузки: ", e);
        }
    }

    private void showMain(Stage stage) {
        try {
            FXMLLoader fxmlLoader = getLoader("main-view.fxml");
            Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

            MainController controller = fxmlLoader.getController();
            controller.setPrintService(new PrintService(server.getUrl()));
            controller.setStickerService(new StickerService());

            String style = Objects.requireNonNull(getResource("style.css")).toExternalForm();
            scene.getStylesheets().add(style);

            stage.setScene(scene);
        } catch (IOException e) {
            showError("Exception in showMain().", "Не удалось открыть главное окно: ", e);
        }
    }

    private FXMLLoader getLoader(String name) {
        return new FXMLLoader(getResource(name));
    }

    private static URL getResource(String name) {
        return App.class.getResource(name);
    }

    private void showError(String logMessage, String userMessage, Throwable throwable) {
        log.error(logMessage, throwable);
        new Alert(Alert.AlertType.ERROR, userMessage + throwable.getMessage()).show();
    }
}
