package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.stage.FileChooser;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.editor.StickerEditor;

import java.io.File;

public class CanvasActionsComponentController extends AbstractController {

    @FXML
    private Button deleteButton;
    @FXML
    private ToggleButton gridToggle;
    @FXML
    private ToggleButton snapToggle;
    @FXML
    private ToggleButton angleSnapToggle;

    @Setter
    private StickerEditor stickerEditor;
    @Setter
    private StickerService stickerService;

    public void setHasSelection(boolean has) {
        deleteButton.setDisable(!has);
    }

    @FXML
    private void onAddText() {
        stickerEditor.addElement(new TextElement("Текст", 10, 10));
    }

    @FXML
    private void onAddBarcode() {
        stickerEditor.addElement(new BarcodeElement());
    }

    @FXML
    private void onAddImage() {
        File file = chooseFile("Выбрать изображение");
        if (file == null) {
            return;
        }
        run(
                () -> stickerService.loadImageElement(file),
                element -> {
                    stickerEditor.addElement(element);
                    message("Изображение добавлено");
                },
                this::error
        );
    }

    @FXML
    private void onDelete() {
        if (stickerEditor.getSelectedElement() != null) {
            stickerEditor.removeSelected();
            message("Элемент удалён");
        }
    }

    @FXML
    private void onToggleGrid() {
        if (stickerEditor != null) {
            stickerEditor.setGridVisible(gridToggle.isSelected());
        }
    }

    @FXML
    private void onTogglePositionSnap() {
        if (stickerEditor != null) {
            stickerEditor.setPositionSnap(snapToggle.isSelected());
        }
    }

    @FXML
    private void onToggleRotationSnap() {
        if (stickerEditor != null) {
            stickerEditor.setRotationSnap(angleSnapToggle.isSelected());
        }
    }

    private File chooseFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenDialog(deleteButton.getScene().getWindow());
    }
}
