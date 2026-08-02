package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.service.StickerService;

import java.io.File;

public class ElementActionsComponentController extends AbstractController {

    @FXML
    private Button deleteButton;

    private StickerEditor editor;
    private StickerService stickerService;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void setStickerService(StickerService stickerService) {
        this.stickerService = stickerService;
    }

    public void setHasSelection(boolean has) {
        deleteButton.setDisable(!has);
    }

    @FXML
    private void onAddText() {
        editor.addElement(new TextElement("Текст", 10, 10));
    }

    @FXML
    private void onAddBarcode() {
        editor.addElement(new BarcodeElement());
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
                    editor.addElement(element);
                    message("Изображение добавлено");
                },
                this::error
        );
    }

    @FXML
    private void onDelete() {
        if (editor.getSelectedElement() != null) {
            editor.removeSelected();
            message("Элемент удалён");
        }
    }

    private File chooseFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenDialog(deleteButton.getScene().getWindow());
    }
}
