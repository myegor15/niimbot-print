package xyz.melnychuk.niimbotprint;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueApiManager;
import xyz.melnychuk.niimbotprint.controller.view.MainViewController;
import xyz.melnychuk.niimbotprint.controller.view.SplashViewController;
import xyz.melnychuk.niimbotprint.util.AsyncUtils;
import xyz.melnychuk.niimbotprint.util.FxmlLoader;

@Slf4j
public class App extends Application {

    private NiimBlueApiManager apiManager;

    @Override
    public void start(Stage stage) {
        startUi(stage);
        startNiimBlue(stage);
    }

    @Override
    public void stop() {
        if (apiManager != null) {
            apiManager.stop();
        }
    }

    private void startUi(Stage stage) {
        if (showSplash(stage)) {
            stage.show();
        }
    }

    private void startNiimBlue(Stage stage) {
        AsyncUtils.run(
                NiimBlueApiManager::start,
                apiManager -> {
                    this.apiManager = apiManager;
                    showMain(stage);
                },
                e -> {
                    log.error("Exception in startNiimBlue().", e);
                    showError(stage, "Не удалось запустить встроенный сервер печати.");
                }
        );
    }

    private boolean showSplash(Stage stage) {
        try {
            var bandle = FxmlLoader.loadView(SplashViewController.class, stage);
            showScene(stage, bandle.node());
            return true;
        } catch (Exception e) {
            log.error("Exception in showSplash().", e);
            showError(stage, "Не удалось открыть окно загрузки.");
            return false;
        }
    }

    private void showMain(Stage stage) {
        try {
            var bundle = FxmlLoader.loadView(MainViewController.class, stage);
            bundle.controller().setApiManager(apiManager);
            showScene(stage, bundle.node());
        } catch (Exception e) {
            log.error("Exception in showMain().", e);
            showError(stage, "Не удалось открыть главное окно.");
        }
    }

    private void showScene(Stage stage, Scene scene) {
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void showError(Stage stage, String message) {
        Platform.setImplicitExit(false);
        stage.close();
        new Alert(Alert.AlertType.ERROR, message, ButtonType.OK).showAndWait();
        Platform.exit();
    }
}
