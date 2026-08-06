package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.model.FontFamily;
import xyz.melnychuk.niimbotprint.util.Component;

import java.util.stream.Stream;

@Component(fxml = "component/elementproperties/text-properties-component.fxml")
public class TextPropertiesComponentController extends ElementPropertiesComponentController<TextElement> {

    @FXML
    private TextField textField;
    @FXML
    private ComboBox<FontFamily> fontBox;
    @FXML
    private Spinner<Integer> sizeSpinner;
    @FXML
    private CheckBox boldCheck;
    @FXML
    private CheckBox italicCheck;
    @FXML
    private CheckBox underlineCheck;

    @Override
    protected void apply() {
        textField.setText(element.getText());
        bind(textField.textProperty(), TextElement::setText);

        fontBox.setItems(FXCollections.observableArrayList(Stream.of(FontFamily.values()).toList()));
        fontBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FontFamily object) {
                return object.getDisplayName();
            }

            @Override
            public FontFamily fromString(String string) {
                return Stream.of(FontFamily.values())
                        .filter(font -> font.getDisplayName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        fontBox.setValue(element.getFontFamily());
        bind(fontBox.valueProperty(), TextElement::setFontFamily);

        bindIntSpinner(sizeSpinner, 6, 200, TextElement::setFontSize, element::getFontSize);

        boldCheck.setSelected(element.isBold());
        bind(boldCheck.selectedProperty(), TextElement::setBold);

        italicCheck.setSelected(element.isItalic());
        bind(italicCheck.selectedProperty(), TextElement::setItalic);

        underlineCheck.setSelected(element.isUnderline());
        bind(underlineCheck.selectedProperty(), TextElement::setUnderline);
    }

    @Override
    protected void sync() {
        syncIntSpinner(sizeSpinner, element::getFontSize);
    }
}
