package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.PrinterDto;
import xyz.melnychuk.niimbotprint.service.PrintService;

public class PrinterInfoComponentController extends AbstractController {

    @FXML
    private TextArea printerInfoArea;

    private PrintService printService;

    public void setPrintService(PrintService printService) {
        this.printService = printService;
    }

    public void refresh() {
        run(printService::getPrinterInfo, this::setInfo, this::error);
    }

    public void setInfo(PrinterDto info) {
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

    public void clear() {
        printerInfoArea.clear();
    }
}
