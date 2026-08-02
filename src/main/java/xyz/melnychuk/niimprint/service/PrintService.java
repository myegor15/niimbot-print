package xyz.melnychuk.niimprint.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimblue.request.PrintRequest;
import xyz.melnychuk.niimblue.response.DevicesResponse;
import xyz.melnychuk.niimblue.response.InfoResponse;
import xyz.melnychuk.niimprint.AppException;
import xyz.melnychuk.niimprint.model.Sticker;

@Slf4j
public class PrintService {

    @Getter
    private final String apiUrl;
    private final NiimBlueApi api;

    public PrintService(String apiUrl) {
        this.apiUrl = apiUrl;
        this.api = new NiimBlueApi(apiUrl);
    }

    public boolean isConnected() {
        try {
            return api.isConnected();
        } catch (Exception e) {
            log.error("Exception in isConnected().", e);
            throw new AppException(e);
        }
    }

    public DevicesResponse scanDevices() {
        try {
            return api.scan();
        } catch (Exception e) {
            log.error("Exception in scanDevices().", e);
            throw new AppException(e);
        }
    }

    public boolean connect(DevicesResponse.Device device) {
        try {
            String target = device.address() == null || device.address().isBlank() ? device.name() : device.address();
            api.connect("ble", target);
            return api.isConnected();
        } catch (Exception e) {
            log.error("Exception in connect().", e);
            throw new AppException(e);
        }
    }

    public void disconnect() {
        try {
            api.disconnect();
        } catch (Exception e) {
            log.error("Exception in disconnect().", e);
            throw new AppException(e);
        }
    }

    public String getPrinterInfo() {
        try {
            InfoResponse info = api.info();
            InfoResponse.PrinterInfo printer = info.printerInfo();
            InfoResponse.ModelMetadata model = info.modelMetadata();
            return "Модель: " + model.model() + "\n"
                    + "DPI: " + model.dpi() + "\n"
                    + "Задача: " + info.detectedPrintTask() + "\n"
                    + "Серийник: " + printer.serial() + "\n"
                    + "MAC: " + printer.mac() + "\n"
                    + "Заряд: " + printer.charge() + "%\n"
                    + "FW: " + printer.softwareVersion();
        } catch (Exception e) {
            log.error("Exception in getPrinterInfo().", e);
            throw new AppException(e);
        }
    }

    public void print(String base64, Sticker sticker, int density, int quantity, String direction) {
        try {
            PrintRequest request = PrintRequest.of(
                    base64, sticker.getWidth(), sticker.getHeight(),
                    density, quantity, direction
            );
            api.print(request);
        } catch (Exception e) {
            log.error("Exception in print().", e);
            throw new AppException(e);
        }
    }
}
