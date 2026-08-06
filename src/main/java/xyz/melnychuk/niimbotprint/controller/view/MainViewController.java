package xyz.melnychuk.niimbotprint.controller.view;

import javafx.fxml.FXML;
import xyz.melnychuk.niimblue.NiimBlueApiManager;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.controller.component.*;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.EditorService;
import xyz.melnychuk.niimbotprint.service.EditorHistoryService;
import xyz.melnychuk.niimbotprint.service.PrinterService;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.util.View;

import java.util.Objects;
import java.util.stream.Stream;

@View(
        fxml = "view/main-view.fxml",
        title = "NiimBot Print",
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

    private NiimBlueApiManager apiManager;
    private boolean bound;

    public void setApiManager(NiimBlueApiManager apiManager) {
        this.apiManager = Objects.requireNonNull(apiManager);
        bind();
    }

    private void bind() {
        if (bound || apiManager == null) {
            return;
        }
        bound = true;

        StickerService stickerService = new StickerService();
        Sticker sticker = stickerService.createSticker();

        EditorService editorService = new EditorService();
        EditorHistoryService editorHistoryService = new EditorHistoryService(sticker);

        PrinterService printerService = new PrinterService(apiManager.getApi());

        Stream.of(
                        printerComponentController,
                        stickerSettingsController,
                        printSettingsController,
                        editorController
                )
                .forEach(component -> {
                    component.setMessageHandler(statusBarController::setMessage);
                    component.setErrorHandler(this::showError);
                });

        printerComponentController.setPrinterService(printerService);
        printerComponentController.setConnectionListener(this::applyConnectionState);

        statusBarController.setMessage("Готово");
        statusBarController.setApiUrl(printerService.getApiUrl());

        editorController.setHistoryService(editorHistoryService);
        editorController.setSticker(sticker);
        editorController.setEditorService(editorService);

        stickerSettingsController.setSticker(sticker);
        stickerSettingsController.setEditor(editorController);
        stickerSettingsController.setStickerService(stickerService);
        stickerSettingsController.setHistoryService(editorHistoryService);

        printSettingsController.setSticker(sticker);
        printSettingsController.setEditor(editorController);
        printSettingsController.setPrinterService(printerService);

        applyConnectionState(false);
    }

    private void applyConnectionState(boolean connected) {
        statusBarController.setConnected(connected);
        printSettingsController.setConnected(connected);
    }

    private void showError(Throwable error) {
        statusBarController.setMessage("Ошибка: " + (error != null ? error.getMessage() : "неизвестна"));
    }
}
