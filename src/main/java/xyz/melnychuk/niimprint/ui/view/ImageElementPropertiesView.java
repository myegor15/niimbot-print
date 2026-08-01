package xyz.melnychuk.niimprint.ui.view;

import javafx.scene.control.Spinner;
import xyz.melnychuk.niimprint.model.ImageElement;

public class ImageElementPropertiesView extends ElementPropertiesView<ImageElement> {

    @Override
    protected void addSpecificProperties(ImageElement image) {
        Spinner<Double> iw = doubleSpinner(image.getWidth(), 1, 2000);
        bind(iw.valueProperty(), image::setWidth, image);
        addRow("Ширина", iw);

        Spinner<Double> ih = doubleSpinner(image.getHeight(), 1, 2000);
        bind(ih.valueProperty(), image::setHeight, image);
        addRow("Высота", ih);
    }
}
