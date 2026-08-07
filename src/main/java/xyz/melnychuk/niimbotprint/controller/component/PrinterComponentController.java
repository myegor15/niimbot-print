package xyz.melnychuk.niimbotprint.controller.component;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import lombok.NonNull;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;
import xyz.melnychuk.niimbotprint.service.PrinterService;

import java.util.function.Consumer;

public class PrinterComponentController extends AbstractController {

    @FXML
    private ComboBox<DeviceDto> deviceComboBox;
    @FXML
    private Button refreshButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private TextArea printerInfoArea;

    private Timeline timeline;

    @Setter
    @NonNull
    private PrinterService printerService;

    @Setter
    @NonNull
    private Consumer<Boolean> connectionListener = c -> {};

    private boolean infoRequestInFlight;

    @FXML
    private void initialize() {
        initTimeline();
    }

    private void initTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> pollStatus()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pollStatus() {
        if (printerService == null) {
            return;
        }
        run(
                printerService::isConnected,
                ok -> setConnected(connected(ok)),
                this::error
        );
    }

    private boolean connected(Boolean ok) {
        return Boolean.TRUE.equals(ok);
    }

    private void setConnected(boolean connected) {
        refreshButton.setDisable(connected);
        deviceComboBox.setDisable(connected);
        disconnectButton.setVisible(connected);
        disconnectButton.setManaged(connected);
        if (connected) {
            refreshPrinterInfo();
        } else {
            clearPrinterInfo();
        }
        connectionListener.accept(connected);
    }

    @FXML
    private void onRefresh() {
        message("Поиск устройств...");
        run(
                printerService::scanDevices,
                devices -> {
                    deviceComboBox.getItems().setAll(devices);
                    message("Найдено устройств: " + devices.size());
                },
                this::error
        );
    }

    @FXML
    private void onDeviceSelected() {
        DeviceDto device = deviceComboBox.getValue();
        if (device == null) {
            return;
        }
        run(
                () -> printerService.connect(device),
                ok -> {
                    if (connected(ok)) {
                        message("Подключено к " + device);
                    } else {
                        message("Не удалось подключиться");
                    }
                    setConnected(connected(ok));
                },
                this::error
        );
    }

    @FXML
    private void onDisconnect() {
        run(
                printerService::disconnect,
                () -> {
                    message("Отключено");
                    setConnected(false);
                },
                this::error
        );
    }

    private void refreshPrinterInfo() {
        if (infoRequestInFlight) {
            return;
        }
        infoRequestInFlight = true;
        run(
                printerService::getPrinterInfo,
                info -> {
                    infoRequestInFlight = false;
                    setInfo(info);
                },
                e -> {
                    infoRequestInFlight = false;
                    error(e);
                }
        );
    }

    private void setInfo(PrinterDto info) {
        printerInfoArea.setText("""
                Модель: %s
                DPI: %s
                Задача: %s
                Серийник: %s
                MAC: %s
                Заряд: %s%%
                FW: %s
                """.formatted(
                info.getModel(),
                info.getDpi(),
                info.getDetectedPrintTask(),
                info.getSerial(),
                info.getMac(),
                info.getCharge(),
                info.getSoftwareVersion()
        ));
    }

    private void clearPrinterInfo() {
        printerInfoArea.clear();
    }
}
