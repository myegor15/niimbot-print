package xyz.melnychuk.niimbotprint.controller.view;

import javafx.fxml.FXML;
import xyz.melnychuk.niimbotprint.AppContext;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.controller.component.*;
import xyz.melnychuk.niimbotprint.util.View;

@View(
        fxml = "view/main-view.fxml",
        width = 1200,
        height = 700,
        stylesheets = "style.css"
)
public class MainViewController extends AbstractController {

    @FXML
    private StickerSettingsComponentController stickerSettingsController;
    @FXML
    private PrintSettingsComponentController printSettingsController;
    @FXML
    private PrinterComponentController printerComponentController;
    @FXML
    private EditorComponentController editorController;
    @FXML
    private StatusBarComponentController statusBarController;

    @Override
    public void dispose() {
        stickerSettingsController.dispose();
        printSettingsController.dispose();
        printerComponentController.dispose();
        editorController.dispose();
        statusBarController.dispose();
    }

    @Override
    protected void bind(AppContext appContext) {
        statusBarController.setAppContext(appContext);

        editorController.setAppContext(appContext);
        editorController.setMessageHandler(statusBarController::setMessage);
        editorController.setErrorHandler(this::showError);

        stickerSettingsController.setAppContext(appContext);
        stickerSettingsController.setMessageHandler(statusBarController::setMessage);
        stickerSettingsController.setErrorHandler(this::showError);
        stickerSettingsController.setEditor(editorController);

        printSettingsController.setAppContext(appContext);
        printSettingsController.setMessageHandler(statusBarController::setMessage);
        printSettingsController.setErrorHandler(this::showError);
        printSettingsController.setEditor(editorController);

        printerComponentController.setAppContext(appContext);
        printerComponentController.setMessageHandler(statusBarController::setMessage);
        printerComponentController.setErrorHandler(this::showError);
        printerComponentController.setConnectionListener(this::applyConnectionState);

        applyConnectionState(false);
    }

    private void applyConnectionState(boolean connected) {
        statusBarController.setConnected(connected);
        printSettingsController.setConnected(connected);
    }

    private void showError(Throwable error) {
        statusBarController.setMessage(getErrorMessage(error));
    }
}
