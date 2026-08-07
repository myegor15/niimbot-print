package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.NonNull;
import lombok.Setter;
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

    @Setter
    @NonNull
    private StickerService stickerService;
    @Setter
    @NonNull
    private EditorHistoryService historyService;

    private Sticker sticker;
    private Editor editor;
    private File currentFile;
    private boolean updating;

    public void setSticker(Sticker sticker) {
        this.sticker = Objects.requireNonNull(sticker);
        historyService.setChangeListener(this::syncControls);
        syncControls();
    }

    public void setEditor(Editor editor) {
        this.editor = Objects.requireNonNull(editor);
        syncControls();
    }

    @FXML
    private void initialize() {
        modelComboBox.getItems().setAll(PrinterModel.values());
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, 8));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, 8));
        modelComboBox.valueProperty().addListener((o, a, b) -> applyModel(b));
        widthSpinner.valueProperty().addListener((o, a, b) -> resize(b, heightSpinner.getValue()));
        heightSpinner.valueProperty().addListener((o, a, b) -> resize(widthSpinner.getValue(), b));
    }

    private void applyModel(PrinterModel model) {
        if (updating || model == null) {
            return;
        }
        historyService.withEdit(() -> {
            sticker.setPrinterModel(model);
            sticker.setWidth(model.getDefaultWidth());
            sticker.setHeight(model.getDefaultHeight());
            syncControls();
            editor.refresh();
        });
    }

    private void resize(int width, int height) {
        if (updating) {
            return;
        }
        historyService.withEdit(() -> {
            sticker.setWidth(width);
            sticker.setHeight(height);
            editor.refresh();
        });
    }

    private void syncControls() {
        if (sticker == null) {
            return;
        }
        updating = true;
        try {
            modelComboBox.setValue(sticker.getPrinterModel() != null ? sticker.getPrinterModel() : PrinterModel.B1);
            widthSpinner.getValueFactory().setValue(sticker.getWidth());
            heightSpinner.getValueFactory().setValue(sticker.getHeight());
        } finally {
            updating = false;
        }
    }

    private void applySticker(Sticker source) {
        sticker.setPrinterModel(source.getPrinterModel());
        sticker.setWidth(source.getWidth());
        sticker.setHeight(source.getHeight());
        sticker.getElements().clear();
        sticker.getElements().addAll(source.getElements());
    }

    @FXML
    private void onNew() {
        currentFile = null;
        historyService.clearHistory();
        applySticker(stickerService.createSticker());
        syncControls();
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
                    applySticker(loaded);
                    syncControls();
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
                () -> stickerService.saveSticker(sticker, file),
                () -> message("Сохранено: " + file.getName()),
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
