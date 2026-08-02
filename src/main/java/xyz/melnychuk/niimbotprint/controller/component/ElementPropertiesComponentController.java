package xyz.melnychuk.niimbotprint.controller.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.StickerElement;

import java.util.function.BiConsumer;

public abstract class ElementPropertiesComponentController<T extends StickerElement> extends AbstractController {

    private StickerEditor editor;

    protected T element;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void show(T element) {
        this.element = element;
    }

    protected <V> void bind(ObservableValue<V> property, BiConsumer<T, V> setter) {
        property.addListener(changeListener(setter));
    }

    protected <V> ChangeListener<V> changeListener(BiConsumer<T, V> setter) {
        return (o, a, b) -> {
            setter.accept(element, b);
            if (editor != null) {
                editor.updateElement(element);
            }
        };
    }

    protected Spinner<Double> doubleSpinner(double value, double min, double max) {
        Spinner<Double> spinner = new Spinner<>(min, max, value, 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        return spinner;
    }
}
