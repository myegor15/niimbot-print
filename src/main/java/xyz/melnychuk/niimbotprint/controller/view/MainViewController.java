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

        bindComponent(appContext, editorController);
        stickerSettingsController.setEditor(editorController);

        bindComponent(appContext, stickerSettingsController);
        printSettingsController.setEditor(editorController);

        bindComponent(appContext, printSettingsController);
        printerComponentController.setConnectionListener(this::applyConnectionState);

        bindComponent(appContext, printerComponentController);

        applyConnectionState(false);
    }

    private void bindComponent(AppContext appContext, AbstractController controller) {
        controller.setAppContext(appContext);
        controller.setMessageHandler(statusBarController::setMessage);
        controller.setErrorHandler(this::showError);
    }

    private void applyConnectionState(boolean connected) {
        statusBarController.setConnected(connected);
        printSettingsController.setConnected(connected);
    }

    private void showError(Throwable error) {
        statusBarController.setMessage(getErrorMessage(error));
    }
}
