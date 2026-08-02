package xyz.melnychuk.niimbotprint;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueApiManager;
import xyz.melnychuk.niimbotprint.controller.MainController;
import xyz.melnychuk.niimbotprint.controller.SplashController;
import xyz.melnychuk.niimbotprint.service.PrintService;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.util.AsyncUtils;
import xyz.melnychuk.niimbotprint.util.ViewLoader;

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
        showSplash(stage);
        stage.show();
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
                    showError("Не удалось запустить встроенный сервер печати.");
                }
        );
    }

    private void showSplash(Stage stage) {
        try {
            stage.setScene(ViewLoader.load(SplashController.class, stage).scene());
        } catch (Exception e) {
            log.error("Exception in showSplash().", e);
            showError("Не удалось открыть окно загрузки.");
        }
    }

    private void showMain(Stage stage) {
        try {
            ViewLoader.Result<MainController> result = ViewLoader.load(MainController.class, stage);
            result.controller().setPrintService(new PrintService(apiManager.getApi()));
            result.controller().setStickerService(new StickerService());
            stage.setScene(result.scene());
        } catch (Exception e) {
            log.error("Exception in showMain().", e);
            showError("Не удалось открыть главное окно.");
        }
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }
}
