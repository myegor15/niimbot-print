package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.service.StickerService;
import xyz.melnychuk.niimbotprint.ui.canvas.StickerCanvas;
import xyz.melnychuk.niimbotprint.ui.editor.CanvasStickerEditor;
import xyz.melnychuk.niimbotprint.ui.editor.StickerEditor;

import java.io.File;
import java.util.List;

public class EditorComponentController extends AbstractController {

    @FXML
    private StackPane canvasHost;
    @FXML
    private VBox elementProperties;
    @FXML
    private Button deleteButton;
    @FXML
    private ToggleButton gridToggle;
    @FXML
    private ToggleButton snapToggle;
    @FXML
    private ToggleButton angleSnapToggle;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Label zoomLabel;

    @Setter
    private StickerService stickerService;

    private StickerEditor editor;
    private Group zoomGroup;
    private ElementPropertiesComponentController<?> currentElementProperties;

    public void setSticker(Sticker sticker) {
        StickerCanvas canvas = new StickerCanvas(sticker);
        zoomGroup = new Group(canvas);
        canvasHost.getChildren().add(zoomGroup);
        editor = new CanvasStickerEditor(canvas);
        editor.setSelectionListener(this::onSelectionChanged);
        editor.setChangeListener(this::sync);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> onZoom());
        onZoom();
    }

    public StickerEditor getStickerEditor() {
        return editor;
    }

    private void onSelectionChanged(StickerElement element) {
        deleteButton.setDisable(element == null);
        show(element);
    }

    private void show(StickerElement element) {
        if (element == null) {
            currentElementProperties = null;
            elementProperties.getChildren().clear();
            return;
        }
        var bundle = ElementPropertiesComponentControllerFactory.getController(element);
        currentElementProperties = bundle.controller();
        currentElementProperties.setStickerEditor(editor);
        currentElementProperties.show(element);
        elementProperties.getChildren().setAll(List.of(bundle.node()));
    }

    private void sync(StickerElement changed) {
        if (currentElementProperties != null) {
            currentElementProperties.sync(changed);
        }
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

    @FXML
    private void onToggleGrid() {
        if (editor != null) {
            editor.setGridVisible(gridToggle.isSelected());
        }
    }

    @FXML
    private void onTogglePositionSnap() {
        if (editor != null) {
            editor.setPositionSnap(snapToggle.isSelected());
        }
    }

    @FXML
    private void onToggleRotationSnap() {
        if (editor != null) {
            editor.setRotationSnap(angleSnapToggle.isSelected());
        }
    }

    @FXML
    private void onZoom() {
        if (zoomGroup == null) {
            return;
        }
        double percent = Math.round(zoomSlider.getValue() / 25) * 25;
        zoomSlider.setValue(percent);
        zoomGroup.setScaleX(percent / 100);
        zoomGroup.setScaleY(percent / 100);
        zoomLabel.setText((int) Math.round(percent) + "%");
    }

    private File chooseFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenDialog(canvasHost.getScene().getWindow());
    }
}