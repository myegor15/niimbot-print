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
import xyz.melnychuk.niimbotprint.util.View;

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
    private CanvasComponentController canvasController;
    @FXML
    private ElementActionsComponentController elementActionsController;
    @FXML
    private ElementPropertiesComponentController elementPropertiesController;
    @FXML
    private StatusBarComponentController statusBarController;

    private PrintService printService;
    private StickerService stickerService;
    private boolean bound;

    public void setPrintService(PrintService printService) {
        this.printService = printService;
        bind();
    }

    public void setStickerService(StickerService stickerService) {
        this.stickerService = stickerService;
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
                        elementActionsController
                )
                .forEach(component -> {
                    component.setMessageHandler(statusBarController::setMessage);
                    component.setErrorHandler(this::showError);
                });

        Sticker sticker = new Sticker();
        canvasController.setSticker(sticker);
        StickerEditor editor = canvasController.getStickerEditor();

        topBarController.setPrintService(printService);
        topBarController.setServerUrl(printService.getApiUrl());
        topBarController.setConnectionListener(this::applyConnectionState);

        stickerSettingsController.setSticker(sticker);
        stickerSettingsController.setStickerService(stickerService);
        stickerSettingsController.setStickerEditor(editor);

        printSettingsController.setSticker(sticker);
        printSettingsController.setPrintService(printService);
        printSettingsController.setStickerEditor(editor);

        elementActionsController.setStickerEditor(editor);
        elementActionsController.setStickerService(stickerService);

        printerInfoController.setPrintService(printService);

        elementPropertiesController.setStickerEditor(editor);
        elementPropertiesController.setHost();

        canvasController.setSelectionListener(element -> {
            elementPropertiesController.showElement(element);
            elementActionsController.setHasSelection(element != null);
        });

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
