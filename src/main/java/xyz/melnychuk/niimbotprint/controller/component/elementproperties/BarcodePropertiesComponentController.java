package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.ui.BarcodeGenerator;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/elementproperties/barcode-properties-component.fxml")
public class BarcodePropertiesComponentController extends ElementPropertiesComponentController<BarcodeElement> {

    @FXML
    private TextField contentField;
    @FXML
    private ComboBox<String> formatBox;
    @FXML
    private Spinner<Integer> widthSpinner;
    @FXML
    private Spinner<Integer> heightSpinner;

    @Override
    protected void apply() {
        contentField.setText(element.getContent());
        bind(contentField.textProperty(), BarcodeElement::setContent);

        formatBox.setItems(FXCollections.observableArrayList(BarcodeGenerator.FORMATS));
        formatBox.setValue(element.getFormat());
        bind(formatBox.valueProperty(), BarcodeElement::setFormat);

        bindIntSpinner(widthSpinner, 1, 2000, BarcodeElement::setWidth, element::getWidth);
        bindIntSpinner(heightSpinner, 1, 2000, BarcodeElement::setHeight, element::getHeight);
    }

    @Override
    protected void sync() {
        syncIntSpinner(widthSpinner, element::getWidth);
        syncIntSpinner(heightSpinner, element::getHeight);
    }
}
