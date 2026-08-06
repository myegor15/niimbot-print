package xyz.melnychuk.niimbotprint.controller.component;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.controller.component.elementproperties.ElementPropertiesComponentController;
import xyz.melnychuk.niimbotprint.controller.component.elementproperties.ElementPropertiesComponentControllerFactory;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.service.EditorService;
import xyz.melnychuk.niimbotprint.ui.Editor;
import xyz.melnychuk.niimbotprint.ui.canvas.StickerCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class EditorComponentController extends AbstractController implements Editor {

    @FXML
    private StackPane canvasHost;
    @FXML
    private VBox elementProperties;
    @FXML
    private Button deleteButton;
    @FXML
    private Button rotateLeftButton;
    @FXML
    private Button rotateRightButton;
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
    private Group zoomGroup;

    private ElementPropertiesComponentController<?> currentElementPropertiesController;

    private Sticker sticker;
    private StickerCanvas canvas;

    private EditorService editorService;

    public void setEditorService(EditorService editorService) {
        this.editorService = Objects.requireNonNull(editorService);
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        canvas = new StickerCanvas(sticker);
        zoomGroup = new Group(canvas);
        canvasHost.getChildren().add(zoomGroup);
        canvas.setSelectionListener(this::onSelectionChanged);
        canvas.setChangeListener(this::sync);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> onZoom());
        onZoom();
    }

    @Override
    public void refresh() {
        canvas.setLabelSize(sticker.getWidth(), sticker.getHeight());
        canvas.refresh();
    }

    @Override
    public String snapshot() {
        boolean gridVisible = canvas.isGridVisible();
        canvas.setGridVisible(false);
        canvas.hideGuides();
        canvas.setSelectionVisible(false);
        try {
            WritableImage image = canvas.snapshot(null, null);
            BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(buffered, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new AppException(e);
        } finally {
            canvas.setSelectionVisible(true);
            canvas.setGridVisible(gridVisible);
        }
    }

    private void onSelectionChanged(Element element) {
        deleteButton.setDisable(element == null);
        rotateLeftButton.setDisable(element == null);
        rotateRightButton.setDisable(element == null);
        show(element);
    }

    private void show(Element element) {
        if (element == null) {
            currentElementPropertiesController = null;
            elementProperties.getChildren().clear();
            return;
        }
        var bundle = ElementPropertiesComponentControllerFactory.getController(element);
        currentElementPropertiesController = bundle.controller();
        currentElementPropertiesController.setElementChangeListener(canvas::updateElement);
        currentElementPropertiesController.show(element);
        elementProperties.getChildren().setAll(List.of(bundle.node()));
    }

    private void sync(Element changed) {
        if (currentElementPropertiesController != null) {
            currentElementPropertiesController.sync(changed);
        }
    }

    @FXML
    private void onAddText() {
        canvas.addElement(editorService.getTextElement());
    }

    @FXML
    private void onAddBarcode() {
        canvas.addElement(editorService.getBarcodeElement());
    }

    @FXML
    private void onAddImage() {
        File file = chooseFile("Выбрать изображение");
        if (file == null) {
            return;
        }
        run(
                () -> editorService.getImageElement(file),
                element -> {
                    canvas.addElement(element);
                    message("Изображение добавлено");
                },
                this::error
        );
    }

    @FXML
    private void onDelete() {
        if (canvas.getSelectedElement() != null) {
            canvas.removeSelected();
            message("Элемент удалён");
        }
    }

    @FXML
    private void onRotateLeft() {
        rotateSelected(-45);
    }

    @FXML
    private void onRotateRight() {
        rotateSelected(45);
    }

    private void rotateSelected(double delta) {
        Element el = canvas.getSelectedElement();
        if (el == null) {
            return;
        }
        el.setRotation(((el.getRotation() + delta) % 360 + 360) % 360);
        canvas.updateElement(el);
    }

    @FXML
    private void onToggleGrid() {
        canvas.setGridVisible(gridToggle.isSelected());
    }

    @FXML
    private void onTogglePositionSnap() {
        canvas.setPositionSnap(snapToggle.isSelected());
    }

    @FXML
    private void onToggleRotationSnap() {
        canvas.setRotationSnap(angleSnapToggle.isSelected());
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
