package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.DeviceDto;
import xyz.melnychuk.niimbotprint.service.PrintService;

import java.util.function.Consumer;

public class TopBarComponentController extends AbstractController {

    @FXML
    private TextField serverField;
    @FXML
    private ComboBox<DeviceDto> deviceCombo;
    @FXML
    private Button scanButton;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;

    private PrintService printService;
    private Consumer<Boolean> connectionListener = c -> {
    };

    public void setPrintService(PrintService printService) {
        this.printService = printService;
    }

    public void setServerUrl(String url) {
        serverField.setText(url);
    }

    public void setConnectionListener(Consumer<Boolean> listener) {
        this.connectionListener = listener;
    }

    public void updateConnected(boolean connected) {
        connectButton.setDisable(connected);
        disconnectButton.setDisable(!connected);
        scanButton.setDisable(connected);
    }

    @FXML
    private void onScan() {
        message("Поиск устройств...");
        run(
                printService::scanDevices,
                devices -> {
                    deviceCombo.getItems().setAll(devices);
                    message("Найдено устройств: " + devices.size());
                },
                this::error
        );
    }

    @FXML
    private void onConnect() {
        DeviceDto device = deviceCombo.getValue();
        if (device == null) {
            message("Сначала выполните поиск и выберите устройство");
            return;
        }
        run(
                () -> printService.connect(device),
                ok -> {
                    message(ok ? "Подключено к " + device : "Не удалось подключиться");
                    connectionListener.accept(Boolean.TRUE.equals(ok));
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
                    connectionListener.accept(false);
                },
                this::error
        );
    }
}
