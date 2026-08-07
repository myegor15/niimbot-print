package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.NonNull;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.PrintDensity;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.PrinterService;
import xyz.melnychuk.niimbotprint.ui.Editor;

public class PrintSettingsComponentController extends AbstractController {

    @FXML
    private ComboBox<PrintDensity> densityComboBox;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private Button printButton;

    @Setter
    @NonNull
    private Sticker sticker;
    @Setter
    @NonNull
    private Editor editor;

    @Setter
    @NonNull
    private PrinterService printerService;

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
        String snapshot = editor.snapshot();
        if (snapshot == null) {
            return;
        }
        int quantity = quantitySpinner.getValue();
        PrintTaskDto task = PrintTaskDto.builder()
                .imageBase64(snapshot)
                .width(sticker.getWidth())
                .height(sticker.getHeight())
                .density(densityComboBox.getValue())
                .quantity(quantity)
                .build();
        run(
                () -> {
                    printerService.print(task);
                    return "Печать отправлена (" + quantity + " шт.)";
                },
                this::message,
                this::error
        );
    }
}
