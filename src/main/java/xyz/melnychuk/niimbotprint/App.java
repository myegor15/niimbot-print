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
import xyz.melnychuk.niimbotprint.i18n.I18n;
import xyz.melnychuk.niimbotprint.i18n.message.AppMessage;
import xyz.melnychuk.niimbotprint.util.AsyncUtils;
import xyz.melnychuk.niimbotprint.util.FxmlLoader;

@Slf4j
public class App extends Application {

    private NiimBlueApiManager apiManager;
    private AppContext appContext;
    private MainViewController mainController;

    @Override
    public void start(Stage stage) {
        initI18n(stage);
        startUi(stage);
        startNiimBlue(stage);
    }

    @Override
    public void stop() {
        if (apiManager != null) {
            apiManager.stop();
        }
    }

    private void initI18n(Stage stage) {
        I18n.init();
        I18n.addLanguageListener(language -> reloadMain(stage));
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
                    this.appContext = AppContext.create(apiManager);
                    showMain(stage, true);
                },
                e -> {
                    log.error("Exception in startNiimBlue().", e);
                    showError(stage, AppMessage.ERROR_SERVER_START);
                }
        );
    }

    private boolean showSplash(Stage stage) {
        try {
            var bundle = FxmlLoader.loadView(SplashViewController.class);
            showScene(stage, bundle.node(), true);
            return true;
        } catch (Exception e) {
            log.error("Exception in showSplash().", e);
            showError(stage, AppMessage.ERROR_SPLASH_OPEN);
            return false;
        }
    }

    private void showMain(Stage stage, boolean center) {
        try {
            var bundle = FxmlLoader.loadView(MainViewController.class);
            mainController = bundle.controller();
            mainController.setAppContext(appContext);
            stage.setTitle(I18n.get(AppMessage.APP_TITLE));
            showScene(stage, bundle.node(), center);
        } catch (Exception e) {
            log.error("Exception in showMain().", e);
            showError(stage, AppMessage.ERROR_MAIN_OPEN);
        }
    }

    private void reloadMain(Stage stage) {
        if (mainController == null) {
            return;
        }
        mainController.dispose();
        showMain(stage, false);
    }

    private void showScene(Stage stage, Scene scene, boolean center) {
        stage.setScene(scene);
        if (center) {
            stage.centerOnScreen();
        }
    }

    private void showError(Stage stage, AppMessage key, Object... args) {
        Platform.setImplicitExit(false);
        stage.close();
        new Alert(Alert.AlertType.ERROR, I18n.get(key, args), ButtonType.OK).showAndWait();
        Platform.exit();
    }
}
