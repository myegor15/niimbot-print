package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.StickerEditor;

import java.util.List;

public class PropertiesComponentController extends AbstractController {

    @FXML
    private VBox elementProperties;

    @Setter
    private StickerEditor stickerEditor;

    private ElementPropertiesComponentController<?> currentElementProperties;

    public void show(StickerElement element) {
        if (element == null) {
            currentElementProperties = null;
            elementProperties.getChildren().clear();
            return;
        }
        var bundle = ElementPropertiesComponentControllerFactory.getController(element);
        currentElementProperties = bundle.controller();
        currentElementProperties.setStickerEditor(stickerEditor);
        currentElementProperties.show(element);
        elementProperties.getChildren().setAll(List.of(bundle.node()));
    }

    public void sync(StickerElement changed) {
        if (currentElementProperties != null) {
            currentElementProperties.sync(changed);
        }
    }
}
