package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.model.Barcode;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/elementproperties/barcode-properties-component.fxml")
public class BarcodePropertiesComponentController extends ElementPropertiesComponentController<Barcode> {

    @FXML
    private TextField contentField;
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;
    @FXML
    private CheckBox showValueCheck;

    @Override
    protected void apply() {
        contentField.setText(element.getContent());
        bind(contentField.textProperty(), Barcode::setContent);

        bindIntSpinner(widthSpinner, 1, 2000, Barcode::setWidth, element::getWidth);
        bindIntSpinner(heightSpinner, 1, 2000, Barcode::setHeight, element::getHeight);

        showValueCheck.setSelected(element.isShowValue());
        bind(showValueCheck.selectedProperty(), Barcode::setShowValue);
    }

    @Override
    protected void sync() {
        contentField.setText(element.getContent());
        syncIntSpinner(widthSpinner, element::getWidth);
        syncIntSpinner(heightSpinner, element::getHeight);
        showValueCheck.setSelected(element.isShowValue());
    }
}
