package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.dto.PrintTaskDto;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.PrintService;
import xyz.melnychuk.niimbotprint.ui.StickerEditor;

public class PrintSettingsComponentController extends AbstractController {

    @FXML
    private Spinner<Integer> densitySpinner;
    @FXML
    private Spinner<Integer> quantitySpinner;
    @FXML
    private ComboBox<String> directionCombo;
    @FXML
    private Button printButton;

    private Sticker sticker;
    private PrintService printService;
    private StickerEditor editor;

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
    }

    public void setPrintService(PrintService printService) {
        this.printService = printService;
    }

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
        densitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 3));
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        directionCombo.getItems().addAll("top", "left");
        directionCombo.setValue("top");
    }

    public void setConnected(boolean connected) {
        printButton.setDisable(!connected);
    }

    @FXML
    private void onPrint() {
        String base64 = editor.snapshotPngBase64();
        if (base64 == null) {
            return;
        }
        run(
                () -> {
                    PrintTaskDto task = new PrintTaskDto(base64, sticker.getWidth(), sticker.getHeight(),
                            densitySpinner.getValue(), quantitySpinner.getValue(), directionCombo.getValue());
                    printService.print(task);
                    return "Печать отправлена (" + quantitySpinner.getValue() + " шт.)";
                },
                this::message,
                this::error
        );
    }
}
