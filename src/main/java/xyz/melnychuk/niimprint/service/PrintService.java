package xyz.melnychuk.niimprint.service;

import lombok.Getter;
import xyz.melnychuk.niimblue.NiimBlueApi;
import xyz.melnychuk.niimblue.request.PrintRequest;
import xyz.melnychuk.niimblue.response.DevicesResponse;
import xyz.melnychuk.niimblue.response.InfoResponse;
import xyz.melnychuk.niimprint.model.Sticker;

public class PrintService {

    @Getter
    private final String apiUrl;
    private final NiimBlueApi api;

    public PrintService(String apiUrl) {
        this.apiUrl = apiUrl;
        this.api = new NiimBlueApi(apiUrl);
    }

    public boolean isConnected() {
        return api.isConnected();
    }

    public DevicesResponse scanDevices() {
        return api.scan();
    }

    public boolean connect(DevicesResponse.Device device) {
        String target = device.address() == null || device.address().isBlank() ? device.name() : device.address();
        api.connect("ble", target);
        return api.isConnected();
    }

    public void disconnect() {
        api.disconnect();
    }

    public String getPrinterInfo() {
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
    }

    public void print(String base64, Sticker sticker, int density, int quantity, String direction) {
        PrintRequest request = PrintRequest.of(
                base64, sticker.getWidth(), sticker.getHeight(),
                density, quantity, direction
        );
        api.print(request);
    }
}
