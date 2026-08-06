package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import xyz.melnychuk.niimbotprint.model.Image;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/elementproperties/image-properties-component.fxml")
public class ImagePropertiesComponentController extends ElementPropertiesComponentController<Image> {

    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    @Override
    protected void apply() {
        double ratio = element.getWidth() / (double) Math.max(1, element.getHeight());
        bindIntSpinner(widthSpinner, 1, 2000,
                (e, w) -> {
                    e.setWidth(w);
                    e.setHeight((int) Math.round(w / ratio));
                },
                element::getWidth);
        bindIntSpinner(heightSpinner, 1, 2000,
                (e, h) -> {
                    e.setHeight(h);
                    e.setWidth((int) Math.round(h * ratio));
                },
                element::getHeight);
    }

    @Override
    protected void sync() {
        syncIntSpinner(widthSpinner, element::getWidth);
        syncIntSpinner(heightSpinner, element::getHeight);
    }
}
