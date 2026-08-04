package xyz.melnychuk.niimbotprint.controller.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.controller.component.*;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.PrintService;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.editor.StickerEditor;
import xyz.melnychuk.niimbotprint.util.View;

import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@View(
        fxml = "view/main-view.fxml",
        title = "NiimBot Print",
        width = 1200,
        height = 700,
        stylesheets = "style.css"
)
public class MainViewController extends AbstractController {

    @FXML
    private TopBarComponentController topBarController;
    @FXML
    private StickerSettingsComponentController stickerSettingsController;
    @FXML
    private PrintSettingsComponentController printSettingsController;
    @FXML
    private PrinterInfoComponentController printerInfoController;
    @FXML
    private EditorComponentController editorController;
    @FXML
    private StatusBarComponentController statusBarController;

    private PrintService printService;
    private StickerService stickerService;
    private StickerEditor editor;
    private boolean bound;

    public void setServices(PrintService printService, StickerService stickerService) {
        this.printService = Objects.requireNonNull(printService);
        this.stickerService = Objects.requireNonNull(stickerService);
        bind();
    }

    @FXML
    private void initialize() {
        initTimeline();
    }

    private void bind() {
        if (bound || printService == null || stickerService == null) {
            return;
        }
        bound = true;

        statusBarController.setMessage("Готово");

        Stream.of(
                        topBarController,
                        stickerSettingsController,
                        printSettingsController,
                        printerInfoController,
                        editorController
                )
                .forEach(component -> {
                    component.setMessageHandler(statusBarController::setMessage);
                    component.setErrorHandler(this::showError);
                });

        Sticker sticker = new Sticker();
        editorController.setStickerService(stickerService);
        editorController.setSticker(sticker);
        editor = editorController.getStickerEditor();

        topBarController.setPrintService(printService);
        topBarController.setConnectionListener(this::applyConnectionState);

        statusBarController.setApiUrl(printService.getApiUrl());

        stickerSettingsController.setSticker(sticker);
        stickerSettingsController.setStickerService(stickerService);
        stickerSettingsController.setStickerEditor(editor);

        printSettingsController.setSticker(sticker);
        printSettingsController.setPrintService(printService);
        printSettingsController.setStickerEditor(editor);

        printerInfoController.setPrintService(printService);

        applyConnectionState(false);
    }

    private void initTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> pollStatus()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pollStatus() {
        run(
                printService::isConnected,
                ok -> applyConnectionState(Boolean.TRUE.equals(ok)),
                this::error
        );
    }

    private void applyConnectionState(boolean connected) {
        topBarController.updateConnected(connected);
        statusBarController.setConnected(connected);
        printSettingsController.setConnected(connected);
        if (connected) {
            printerInfoController.refresh();
        } else {
            printerInfoController.clear();
        }
    }

    private void showError(Throwable error) {
        log.error("Exception in showError().", error);
        statusBarController.setMessage("Ошибка: " + (error != null ? error.getMessage() : "неизвестна"));
    }
}
