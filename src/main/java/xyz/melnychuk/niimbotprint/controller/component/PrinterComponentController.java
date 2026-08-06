package xyz.melnychuk.niimbotprint.controller.component;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;
import xyz.melnychuk.niimbotprint.service.PrinterService;

import java.util.Objects;
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

    private PrinterService printerService;

    @Setter
    private Consumer<Boolean> connectionListener = c -> {};

    public void setPrinterService(PrinterService printerService) {
        this.printerService = Objects.requireNonNull(printerService);
    }

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
                ok -> applyConnectionState(Boolean.TRUE.equals(ok)),
                this::error
        );
    }

    private void applyConnectionState(boolean connected) {
        setConnected(connected);
        connectionListener.accept(connected);
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
                    if (Boolean.TRUE.equals(ok)) {
                        message("Подключено к " + device);
                    } else {
                        message("Не удалось подключиться");
                    }
                    applyConnectionState(Boolean.TRUE.equals(ok));
                },
                this::error
        );
    }

    @FXML
    private void onDisconnect() {
        run(
                () -> {
                    printerService.disconnect();
                    return true;
                },
                ok -> {
                    message("Отключено");
                    applyConnectionState(false);
                },
                this::error
        );
    }

    private void refreshPrinterInfo() {
        run(printerService::getPrinterInfo, this::setInfo, this::error);
    }

    private void setInfo(PrinterDto info) {
        printerInfoArea.setText(
                "Модель: " + info.getModel() + "\n"
                        + "DPI: " + info.getDpi() + "\n"
                        + "Задача: " + info.getDetectedPrintTask() + "\n"
                        + "Серийник: " + info.getSerial() + "\n"
                        + "MAC: " + info.getMac() + "\n"
                        + "Заряд: " + info.getCharge() + "%\n"
                        + "FW: " + info.getSoftwareVersion()
        );
    }

    private void clearPrinterInfo() {
        printerInfoArea.clear();
    }
}
