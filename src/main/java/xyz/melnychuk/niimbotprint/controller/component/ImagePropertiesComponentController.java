package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/image-properties-component.fxml")
public class ImagePropertiesComponentController extends ElementPropertiesComponentController<ImageElement> {

    @FXML
    private Spinner<Double> widthSpinner;
    @FXML
    private Spinner<Double> heightSpinner;

    @Override
    protected void apply() {
        bindSpinner(widthSpinner, 1, 2000, ImageElement::setWidth, element::getWidth);
        bindSpinner(heightSpinner, 1, 2000, ImageElement::setHeight, element::getHeight);
    }

    @Override
    protected void sync() {
        syncSpinner(widthSpinner, element::getWidth);
        syncSpinner(heightSpinner, element::getHeight);
    }
}