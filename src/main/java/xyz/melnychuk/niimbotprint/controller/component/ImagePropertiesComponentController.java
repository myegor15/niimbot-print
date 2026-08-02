package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/image-properties-component.fxml")
public class ImagePropertiesComponentController extends ElementPropertiesComponentController<ImageElement> {

    @FXML
    private Spinner<Double> widthSpinner;
    @FXML
    private Spinner<Double> heightSpinner;

    @Override
    public void show(ImageElement image) {
        super.show(image);

        widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 2000, image.getWidth(), 1));
        widthSpinner.setEditable(true);
        bind(widthSpinner.valueProperty(), ImageElement::setWidth);

        heightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 2000, image.getHeight(), 1));
        heightSpinner.setEditable(true);
        bind(heightSpinner.valueProperty(), ImageElement::setHeight);
    }
}
