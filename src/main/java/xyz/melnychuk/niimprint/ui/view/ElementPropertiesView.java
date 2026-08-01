package xyz.melnychuk.niimprint.ui.view;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Setter;
import xyz.melnychuk.niimprint.model.StickerElement;

import java.util.function.Consumer;

public abstract class ElementPropertiesView<T extends StickerElement> extends VBox {

    @Setter
    private Consumer<StickerElement> changeListener = e -> {};

    protected ElementPropertiesView() {
        super(8);
    }

    public final void show(T element) {
        getChildren().clear();
        addSpecificProperties(element);
        addPositionProperties(element);
    }

    protected abstract void addSpecificProperties(T element);

    protected void addPositionProperties(StickerElement element) {
        Spinner<Double> x = doubleSpinner(element.getX(), 0, 2000);
        bind(x.valueProperty(), element::setX, element);
        addRow("X", x);

        Spinner<Double> y = doubleSpinner(element.getY(), 0, 2000);
        bind(y.valueProperty(), element::setY, element);
        addRow("Y", y);
    }

    protected <V> void bind(ObservableValue<V> property, Consumer<V> setter, StickerElement element) {
        property.addListener((o, a, b) -> {
            setter.accept(b);
            changeListener.accept(element);
        });
    }

    protected Spinner<Double> doubleSpinner(double value, double min, double max) {
        Spinner<Double> spinner = new Spinner<>(min, max, value, 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        return spinner;
    }

    protected void addRow(String name, Node control) {
        HBox row = new HBox(8, new Label(name), control);
        row.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(row);
    }
}
