package xyz.melnychuk.niimprint.ui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimprint.model.BarcodeElement;
import xyz.melnychuk.niimprint.ui.BarcodeGenerator;

public class BarcodeElementPropertiesView extends ElementPropertiesView<BarcodeElement> {

    @Override
    protected void addSpecificProperties(BarcodeElement barcode) {
        TextField field = new TextField(barcode.getContent());
        bind(field.textProperty(), barcode::setContent, barcode);
        addRow("Содержимое", field);

        ComboBox<String> formatBox = new ComboBox<>(FXCollections.observableArrayList(BarcodeGenerator.FORMATS));
        formatBox.setValue(barcode.getFormat());
        bind(formatBox.valueProperty(), barcode::setFormat, barcode);
        addRow("Формат", formatBox);

        Spinner<Double> bw = doubleSpinner(barcode.getWidth(), 1, 2000);
        bind(bw.valueProperty(), barcode::setWidth, barcode);
        addRow("Ширина", bw);

        Spinner<Double> bh = doubleSpinner(barcode.getHeight(), 1, 2000);
        bind(bh.valueProperty(), barcode::setHeight, barcode);
        addRow("Высота", bh);
    }
}
