package xyz.melnychuk.niimbotprint.controller.view;

import javafx.fxml.FXML;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.controller.component.*;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.EditorService;
import xyz.melnychuk.niimbotprint.service.PrintService;
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

    private Sticker sticker;

    private PrintService printService;
    private StickerService stickerService;
    private EditorService editorService;
    private boolean bound;

    public void setServices(PrintService printService, StickerService stickerService, EditorService editorService) {
        this.printService = Objects.requireNonNull(printService);
        this.stickerService = Objects.requireNonNull(stickerService);
        this.editorService = Objects.requireNonNull(editorService);
        bind();
    }

    private void bind() {
        if (bound || printService == null || stickerService == null) {
            return;
        }
        bound = true;

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

        printerComponentController.setPrintService(printService);
        printerComponentController.setConnectionListener(this::applyConnectionState);

        statusBarController.setMessage("Готово");
        statusBarController.setApiUrl(printService.getApiUrl());

        sticker = new Sticker();
        editorController.setSticker(sticker);
        editorController.setEditorService(editorService);

        stickerSettingsController.setSticker(sticker);
        stickerSettingsController.setEditor(editorController);
        stickerSettingsController.setStickerService(stickerService);

        printSettingsController.setSticker(sticker);
        printSettingsController.setEditor(editorController);
        printSettingsController.setPrintService(printService);

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
