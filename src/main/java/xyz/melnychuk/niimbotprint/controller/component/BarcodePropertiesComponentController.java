package xyz.melnychuk.niimbotprint.controller.component;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.ui.BarcodeGenerator;
import xyz.melnychuk.niimbotprint.util.Component;

@Component(fxml = "component/barcode-properties-component.fxml")
public class BarcodePropertiesComponentController extends ElementPropertiesComponentController<BarcodeElement> {

    @FXML
    private TextField contentField;
    @FXML
    private ComboBox<String> formatBox;
    @FXML
    private Spinner<Double> widthSpinner;
    @FXML
    private Spinner<Double> heightSpinner;

    @Override
    public void show(BarcodeElement barcode) {
        super.show(barcode);

        contentField.setText(barcode.getContent());
        bind(contentField.textProperty(), BarcodeElement::setContent);

        formatBox.setItems(FXCollections.observableArrayList(BarcodeGenerator.FORMATS));
        formatBox.setValue(barcode.getFormat());
        bind(formatBox.valueProperty(), BarcodeElement::setFormat);

        widthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 2000, barcode.getWidth(), 1));
        widthSpinner.setEditable(true);
        bind(widthSpinner.valueProperty(), BarcodeElement::setWidth);

        heightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 2000, barcode.getHeight(), 1));
        heightSpinner.setEditable(true);
        bind(heightSpinner.valueProperty(), BarcodeElement::setHeight);
    }
}
