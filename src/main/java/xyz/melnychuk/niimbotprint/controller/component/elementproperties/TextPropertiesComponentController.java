package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import xyz.melnychuk.niimbotprint.model.Text;
import xyz.melnychuk.niimbotprint.model.FontFamily;
import xyz.melnychuk.niimbotprint.util.Component;

import java.util.stream.Stream;

@Component(fxml = "component/elementproperties/text-properties-component.fxml")
public class TextPropertiesComponentController extends ElementPropertiesComponentController<Text> {

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
        bind(textField.textProperty(), Text::setText);

        fontBox.setItems(FXCollections.observableArrayList(Stream.of(FontFamily.values()).toList()));
        fontBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(FontFamily font) {
                return font == null ? null : font.getDisplayName();
            }

            @Override
            public FontFamily fromString(String string) {
                return FontFamily.byDisplayName(string);
            }
        });
        fontBox.setValue(element.getFontFamily());
        bind(fontBox.valueProperty(), Text::setFontFamily);

        bindIntSpinner(sizeSpinner, 6, 200, Text::setFontSize, element::getFontSize);

        boldCheck.setSelected(element.isBold());
        bind(boldCheck.selectedProperty(), Text::setBold);

        italicCheck.setSelected(element.isItalic());
        bind(italicCheck.selectedProperty(), Text::setItalic);

        underlineCheck.setSelected(element.isUnderline());
        bind(underlineCheck.selectedProperty(), Text::setUnderline);
    }

    @Override
    protected void sync() {
        textField.setText(element.getText());
        fontBox.setValue(element.getFontFamily());
        syncIntSpinner(sizeSpinner, element::getFontSize);
        boldCheck.setSelected(element.isBold());
        italicCheck.setSelected(element.isItalic());
        underlineCheck.setSelected(element.isUnderline());
    }
}
