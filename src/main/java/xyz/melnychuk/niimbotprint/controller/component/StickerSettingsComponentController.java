package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.PrinterModel;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.EditorHistoryService;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.Editor;

import java.io.File;
import java.util.Objects;

public class StickerSettingsComponentController extends AbstractController {

    @FXML
    private ComboBox<PrinterModel> modelComboBox;
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    private Sticker sticker;
    private Editor editor;
    private File currentFile;

    private StickerService stickerService;

    private EditorHistoryService historyService;

    public void setHistoryService(EditorHistoryService historyService) {
        this.historyService = Objects.requireNonNull(historyService);
    }

    public void setStickerService(StickerService stickerService) {
        this.stickerService = Objects.requireNonNull(stickerService);
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        modelComboBox.getItems().setAll(PrinterModel.values());
        modelComboBox.setValue(sticker.getPrinterModel() != null ? sticker.getPrinterModel() : PrinterModel.B1);
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getWidth()));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getHeight()));
    }

    public void setEditor(Editor editor) {
        this.editor = editor;
        modelComboBox.valueProperty().addListener((o, a, b) -> applyModel(b));
        widthSpinner.valueProperty().addListener((o, a, b) -> resize(b, heightSpinner.getValue()));
        heightSpinner.valueProperty().addListener((o, a, b) -> resize(widthSpinner.getValue(), b));
    }

    private void applyModel(PrinterModel model) {
        if (!PrinterModel.B1.equals(model) && !PrinterModel.D11.equals(model)) {
            return;
        }
        historyService.withEdit(() -> {
            sticker.setPrinterModel(model);
            sticker.setWidth(model.getDefaultWidth());
            sticker.setHeight(model.getDefaultHeight());
            widthSpinner.getValueFactory().setValue(model.getDefaultWidth());
            heightSpinner.getValueFactory().setValue(model.getDefaultHeight());
            editor.refresh();
        });
    }

    private void resize(int width, int height) {
        historyService.withEdit(() -> {
            sticker.setWidth(width);
            sticker.setHeight(height);
            editor.refresh();
        });
    }

    @FXML
    private void onNew() {
        currentFile = null;
        historyService.clearHistory();
        Sticker s = new Sticker();
        sticker.setPrinterModel(s.getPrinterModel());
        sticker.setWidth(s.getWidth());
        sticker.setHeight(s.getHeight());
        sticker.getElements().clear();
        modelComboBox.setValue(s.getPrinterModel());
        widthSpinner.getValueFactory().setValue(s.getWidth());
        heightSpinner.getValueFactory().setValue(s.getHeight());
        editor.refresh();
        message("Новая этикетка");
    }

    @FXML
    private void onOpen() {
        File file = chooseFile("Открыть этикетку", false);
        if (file == null) {
            return;
        }
        run(
                () -> stickerService.loadSticker(file),
                loaded -> {
                    currentFile = file;
                    historyService.clearHistory();
                    sticker.setPrinterModel(loaded.getPrinterModel());
                    sticker.setWidth(loaded.getWidth());
                    sticker.setHeight(loaded.getHeight());
                    sticker.setElements(loaded.getElements());
                    modelComboBox.setValue(loaded.getPrinterModel() != null ? loaded.getPrinterModel() : PrinterModel.B1);
                    widthSpinner.getValueFactory().setValue(loaded.getWidth());
                    heightSpinner.getValueFactory().setValue(loaded.getHeight());
                    editor.refresh();
                    message("Открыто: " + file.getName());
                },
                this::error
        );
    }

    @FXML
    private void onSave() {
        if (currentFile != null) {
            saveTo(currentFile);
        } else {
            onSaveAs();
        }
    }

    @FXML
    private void onSaveAs() {
        File file = chooseFile("Сохранить этикетку", true);
        if (file == null) {
            return;
        }
        saveTo(withJsonExtension(file));
    }

    private void saveTo(File file) {
        currentFile = file;
        run(
                () -> {
                    stickerService.saveSticker(sticker, file);
                    return true;
                },
                ok -> message("Сохранено: " + file.getName()),
                this::error
        );
    }

    private File chooseFile(String title, boolean save) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new ExtensionFilter("Этикетка (*.json)", "*.json"));
        if (save) {
            chooser.setInitialFileName("sticker.json");
        }
        var window = widthSpinner.getScene().getWindow();
        return save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
    }

    private File withJsonExtension(File file) {
        if (file.getName().toLowerCase().endsWith(".json")) {
            return file;
        }
        return new File(file.getParent(), file.getName() + ".json");
    }

}
