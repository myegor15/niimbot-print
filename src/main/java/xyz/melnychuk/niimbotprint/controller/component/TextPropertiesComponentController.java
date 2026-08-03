package xyz.melnychuk.niimbotprint.controller.component;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.util.Component;

import java.util.List;

@Component(fxml = "component/text-properties-component.fxml")
public class TextPropertiesComponentController extends ElementPropertiesComponentController<TextElement> {

    private static final List<String> FONTS = List.of(
            "Arial", "Arial Black", "Courier New", "Helvetica",
            "Segoe UI", "Tahoma", "Times New Roman", "Verdana"
    );

    @FXML
    private TextField textField;
    @FXML
    private ComboBox<String> fontBox;
    @FXML
    private Spinner<Double> sizeSpinner;
    @FXML
    private CheckBox boldCheck;

    @Override
    protected void apply() {
        textField.setText(element.getText());
        bind(textField.textProperty(), TextElement::setText);

        fontBox.setItems(FXCollections.observableArrayList(FONTS));
        fontBox.setValue(FONTS.contains(element.getFontFamily()) ? element.getFontFamily() : "Arial");
        bind(fontBox.valueProperty(), TextElement::setFontFamily);

        bindSpinner(sizeSpinner, 6, 200, TextElement::setFontSize, element::getFontSize);

        boldCheck.setSelected(element.isBold());
        bind(boldCheck.selectedProperty(), TextElement::setBold);
    }

    @Override
    protected void sync() {
        syncSpinner(sizeSpinner, element::getFontSize);
    }
}