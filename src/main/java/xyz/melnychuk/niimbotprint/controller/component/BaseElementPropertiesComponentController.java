package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.util.ComponentLoader;

import java.util.Map;

public class BaseElementPropertiesComponentController extends AbstractController {

    private static final Map<Class<? extends StickerElement>, Class<? extends ElementPropertiesComponentController<?>>> REGISTRY =
            Map.of(
                    TextElement.class, TextPropertiesComponentController.class,
                    BarcodeElement.class, BarcodePropertiesComponentController.class,
                    ImageElement.class, ImagePropertiesComponentController.class
            );

    @FXML
    private Spinner<Double> xSpinner;
    @FXML
    private Spinner<Double> ySpinner;
    @FXML
    private VBox propertyBox;

    private StickerEditor editor;
    private StickerElement element;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void setHost() {
        bindPosition();
    }

    public void showElement(StickerElement element) {
        this.element = element;
        propertyBox.getChildren().clear();
        if (element == null) {
            propertyBox.getChildren().add(new Label("Выберите элемент на этикетке"));
            return;
        }
        configurePosition(element);
        ComponentLoader.Bundle<? extends ElementPropertiesComponentController<?>> bundle =
                ComponentLoader.load(registryController(element));
        ElementPropertiesComponentController<?> controller = bundle.controller();
        controller.setStickerEditor(editor);
        show(controller, element);
        propertyBox.getChildren().add(bundle.root());
    }

    private Class<? extends ElementPropertiesComponentController<?>> registryController(StickerElement element) {
        Class<? extends ElementPropertiesComponentController<?>> controllerType = REGISTRY.get(element.getClass());
        if (controllerType == null) {
            throw new AppException("Unknown element: " + element);
        }
        return controllerType;
    }

    private void bindPosition() {
        xSpinner.valueProperty().addListener((o, a, b) -> onPositionChanged(b, ySpinner.getValue()));
        ySpinner.valueProperty().addListener((o, a, b) -> onPositionChanged(xSpinner.getValue(), b));
    }

    @SuppressWarnings("unchecked")
    private void show(ElementPropertiesComponentController<?> controller, StickerElement element) {
        ((ElementPropertiesComponentController<StickerElement>) controller).show(element);
    }

    private void onPositionChanged(Double x, Double y) {
        if (element == null || editor == null) {
            return;
        }
        double newX = x != null ? x : element.getX();
        double newY = y != null ? y : element.getY();
        element.setX(newX);
        element.setY(newY);
        editor.updateElement(element);
    }

    private void configurePosition(StickerElement element) {
        xSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 2000, element.getX(), 1));
        ySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 2000, element.getY(), 1));
    }
}
