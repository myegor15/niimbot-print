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
import xyz.melnychuk.niimbotprint.ui.StickerEditor;
import xyz.melnychuk.niimbotprint.util.ComponentLoader;

import java.util.Map;
import java.util.function.DoubleConsumer;

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
    private ElementPropertiesComponentController<?> elementController;
    private boolean updating;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void setHost() {
        bindLive(xSpinner, this::setX);
        bindLive(ySpinner, this::setY);
    }

    public void showElement(StickerElement element) {
        this.element = element;
        propertyBox.getChildren().clear();
        elementController = null;
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
        elementController = controller;
        propertyBox.getChildren().add(bundle.root());
    }

    public void syncFromCanvas(StickerElement changed) {
        if (element == null || element != changed) {
            return;
        }
        updating = true;
        if (elementController != null) {
            elementController.setUpdating(true);
        }
        updatePositionValues();
        if (elementController != null) {
            elementController.sync();
            elementController.setUpdating(false);
        }
        updating = false;
    }

    private Class<? extends ElementPropertiesComponentController<?>> registryController(StickerElement element) {
        Class<? extends ElementPropertiesComponentController<?>> controllerType = REGISTRY.get(element.getClass());
        if (controllerType == null) {
            throw new AppException("Unknown element: " + element);
        }
        return controllerType;
    }

    private void setX(double x) {
        if (element != null && editor != null) {
            element.setX(x);
            editor.updateElement(element);
        }
    }

    private void setY(double y) {
        if (element != null && editor != null) {
            element.setY(y);
            editor.updateElement(element);
        }
    }

    @SuppressWarnings("unchecked")
    private void show(ElementPropertiesComponentController<?> controller, StickerElement element) {
        ((ElementPropertiesComponentController<StickerElement>) controller).show(element);
    }

    private void bindLive(Spinner<Double> spinner, DoubleConsumer setter) {
        spinner.valueProperty().addListener((o, a, b) -> {
            if (updating || b == null) {
                return;
            }
            setter.accept(b);
        });
        spinner.getEditor().textProperty().addListener((o, a, b) -> {
            if (updating) {
                return;
            }
            Double v = ElementPropertiesComponentController.parseDouble(b);
            if (v != null) {
                setter.accept(v);
            }
        });
    }

    private void updatePositionValues() {
        if (xSpinner.getValueFactory() != null && ySpinner.getValueFactory() != null) {
            xSpinner.getValueFactory().setValue(element.getX());
            ySpinner.getValueFactory().setValue(element.getY());
        }
    }

    private void configurePosition(StickerElement element) {
        xSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 2000, element.getX(), 1));
        ySpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 2000, element.getY(), 1));
    }
}