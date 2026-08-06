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
import xyz.melnychuk.niimbotprint.service.PrintService;

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

    private PrintService printService;

    @Setter
    private Consumer<Boolean> connectionListener = c -> {};

    public void setPrintService(PrintService printService) {
        this.printService = Objects.requireNonNull(printService);
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
        if (printService == null) {
            return;
        }
        run(
                printService::isConnected,
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
                printService::scanDevices,
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
                () -> printService.connect(device),
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
                    printService.disconnect();
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
        run(printService::getPrinterInfo, this::setInfo, this::error);
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
