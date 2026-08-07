package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.PrintDensity;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.PrinterService;
import xyz.melnychuk.niimbotprint.ui.Editor;

import java.util.Objects;

public class PrintSettingsComponentController extends AbstractController {

    @FXML
    private ComboBox<PrintDensity> densityComboBox;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private Button printButton;

    @Setter
    private Sticker sticker;
    @Setter
    private Editor editor;

    private PrinterService printerService;

    public void setPrinterService(PrinterService printerService) {
        this.printerService = Objects.requireNonNull(printerService);
    }

    @FXML
    private void initialize() {
        densityComboBox.getItems().addAll(PrintDensity.values());
        densityComboBox.setValue(PrintDensity.NORMAL);
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
    }

    public void setConnected(boolean connected) {
        printButton.setDisable(!connected);
    }

    @FXML
    private void onPrint() {
        String base64 = editor.snapshot();
        if (base64 == null) {
            return;
        }
        run(
                () -> {
                    PrintTaskDto task = new PrintTaskDto(base64, sticker.getWidth(), sticker.getHeight(),
                            densityComboBox.getValue(), quantitySpinner.getValue());
                    printerService.print(task);
                    return "Печать отправлена (" + quantitySpinner.getValue() + " шт.)";
                },
                this::message,
                this::error
        );
    }
}
