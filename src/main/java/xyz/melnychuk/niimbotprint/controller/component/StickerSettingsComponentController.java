package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.editor.StickerEditor;

import java.io.File;

public class StickerSettingsComponentController extends AbstractController {

    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    private Sticker sticker;
    private StickerEditor stickerEditor;
    @Setter
    private StickerService stickerService;
    private File currentFile;

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getWidth()));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getHeight()));
    }

    public void setStickerEditor(StickerEditor editor) {
        this.stickerEditor = editor;
        widthSpinner.valueProperty().addListener((o, a, b) -> resize(b, heightSpinner.getValue()));
        heightSpinner.valueProperty().addListener((o, a, b) -> resize(widthSpinner.getValue(), b));
    }

    private void resize(int width, int height) {
        sticker.setWidth(width);
        sticker.setHeight(height);
        stickerEditor.setLabelSize(width, height);
    }

    @FXML
    private void onNew() {
        currentFile = null;
        Sticker s = new Sticker();
        sticker.setWidth(s.getWidth());
        sticker.setHeight(s.getHeight());
        sticker.getElements().clear();
        widthSpinner.getValueFactory().setValue(s.getWidth());
        heightSpinner.getValueFactory().setValue(s.getHeight());
        stickerEditor.setLabelSize(s.getWidth(), s.getHeight());
        stickerEditor.refresh();
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
                    sticker.setWidth(loaded.getWidth());
                    sticker.setHeight(loaded.getHeight());
                    sticker.setElements(loaded.getElements());
                    widthSpinner.getValueFactory().setValue(loaded.getWidth());
                    heightSpinner.getValueFactory().setValue(loaded.getHeight());
                    stickerEditor.setLabelSize(loaded.getWidth(), loaded.getHeight());
                    stickerEditor.refresh();
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
