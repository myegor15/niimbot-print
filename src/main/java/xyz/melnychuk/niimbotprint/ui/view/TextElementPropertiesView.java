package xyz.melnychuk.niimbotprint.ui.view;

import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import xyz.melnychuk.niimbotprint.model.TextElement;

import java.util.List;

public class TextElementPropertiesView extends ElementPropertiesView<TextElement> {

    private static final List<String> FONTS = List.of(
            "Arial", "Arial Black", "Courier New", "Helvetica",
            "Segoe UI", "Tahoma", "Times New Roman", "Verdana"
    );

    @Override
    protected void addSpecificProperties(TextElement text) {
        TextField field = new TextField(text.getText());
        bind(field.textProperty(), text::setText, text);
        addRow("Текст", field);

        ComboBox<String> fontBox = new ComboBox<>(FXCollections.observableArrayList(FONTS));
        fontBox.setValue(FONTS.contains(text.getFontFamily()) ? text.getFontFamily() : "Arial");
        bind(fontBox.valueProperty(), text::setFontFamily, text);
        addRow("Шрифт", fontBox);

        Spinner<Double> size = doubleSpinner(text.getFontSize(), 6, 200);
        bind(size.valueProperty(), text::setFontSize, text);
        addRow("Размер", size);

        CheckBox bold = new CheckBox();
        bold.setSelected(text.isBold());
        bind(bold.selectedProperty(), text::setBold, text);
        addRow("Жирный", bold);
    }
}
