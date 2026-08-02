package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.StickerCanvas;

import java.util.function.Consumer;

public class CanvasComponentController extends AbstractController {

    @FXML
    private StackPane canvasHost;

    private StickerEditor editor;

    public void setSticker(Sticker sticker) {
        StickerCanvas canvas = new StickerCanvas(sticker);
        canvasHost.getChildren().add(canvas);
        editor = new StickerCanvasEditor(canvas);
    }

    public StickerEditor getStickerEditor() {
        return editor;
    }

    public void setSelectionListener(Consumer<StickerElement> listener) {
        editor.setSelectionListener(listener);
    }
}
