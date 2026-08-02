package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.view.ElementPropertiesView;
import xyz.melnychuk.niimbotprint.ui.view.ElementPropertiesViewFactory;

public class ElementPropertiesComponentController extends AbstractController {

    @FXML
    private VBox propertiesBox;

    private final VBox body = new VBox(8);
    private StickerEditor editor;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void setHost() {
        propertiesBox.getChildren().add(body);
    }

    public void showElement(StickerElement element) {
        body.getChildren().clear();
        if (element == null) {
            body.getChildren().add(new Label("Выберите элемент на этикетке"));
            return;
        }
        ElementPropertiesView<StickerElement> view = ElementPropertiesViewFactory.create(element);
        view.setChangeListener(editor::updateElement);
        view.show(element);
        body.getChildren().add(view);
    }
}
