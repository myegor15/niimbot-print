package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.FileChooser;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.service.StickerService;

import java.io.File;

public class StickerSettingsComponentController extends AbstractController {

    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    private Sticker sticker;
    private StickerService stickerService;
    private StickerEditor editor;

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getWidth()));
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 2000, sticker.getHeight()));
    }

    public void setStickerService(StickerService stickerService) {
        this.stickerService = stickerService;
    }

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
        widthSpinner.valueProperty().addListener((o, a, b) -> resize(b, heightSpinner.getValue()));
        heightSpinner.valueProperty().addListener((o, a, b) -> resize(widthSpinner.getValue(), b));
    }

    private void resize(int width, int height) {
        sticker.setWidth(width);
        sticker.setHeight(height);
        editor.setLabelSize(width, height);
    }

    @FXML
    private void onNew() {
        Sticker s = new Sticker();
        sticker.setWidth(s.getWidth());
        sticker.setHeight(s.getHeight());
        sticker.getElements().clear();
        widthSpinner.getValueFactory().setValue(s.getWidth());
        heightSpinner.getValueFactory().setValue(s.getHeight());
        editor.setLabelSize(s.getWidth(), s.getHeight());
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
                    sticker.setWidth(loaded.getWidth());
                    sticker.setHeight(loaded.getHeight());
                    sticker.setElements(loaded.getElements());
                    widthSpinner.getValueFactory().setValue(loaded.getWidth());
                    heightSpinner.getValueFactory().setValue(loaded.getHeight());
                    editor.setLabelSize(loaded.getWidth(), loaded.getHeight());
                    editor.refresh();
                    message("Открыто: " + file.getName());
                },
                this::error
        );
    }

    @FXML
    private void onSave() {
        File file = chooseFile("Сохранить этикетку", true);
        if (file == null) {
            return;
        }
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
        var window = widthSpinner.getScene().getWindow();
        return save ? chooser.showSaveDialog(window) : chooser.showOpenDialog(window);
    }
}
