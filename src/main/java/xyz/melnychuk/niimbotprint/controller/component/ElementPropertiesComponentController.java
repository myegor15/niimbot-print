package xyz.melnychuk.niimbotprint.controller.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.StickerEditor;

import java.util.function.BiConsumer;

public abstract class ElementPropertiesComponentController<T extends StickerElement> extends AbstractController {

    private StickerEditor editor;

    private boolean updating;

    protected T element;

    public void setStickerEditor(StickerEditor editor) {
        this.editor = editor;
    }

    public void show(T element) {
        this.element = element;
    }

    public void sync() {
    }

    protected void setUpdating(boolean updating) {
        this.updating = updating;
    }

    protected void touch() {
        if (editor != null) {
            editor.updateElement(element);
        }
    }

    protected <V> void bind(ObservableValue<V> property, BiConsumer<T, V> setter) {
        property.addListener(changeListener(setter));
    }

    protected <V> ChangeListener<V> changeListener(BiConsumer<T, V> setter) {
        return (o, a, b) -> {
            if (updating) {
                return;
            }
            setter.accept(element, b);
            touch();
        };
    }

    protected void bindLive(Spinner<Double> spinner, BiConsumer<T, Double> setter) {
        bind(spinner.valueProperty(), setter);
        spinner.getEditor().textProperty().addListener((o, a, b) -> {
            Double v = parseDouble(b);
            if (v != null) {
                setter.accept(element, v);
                touch();
            }
        });
    }

    protected Spinner<Double> doubleSpinner(double value, double min, double max) {
        Spinner<Double> spinner = new Spinner<>(min, max, value, 1);
        spinner.setEditable(true);
        spinner.setPrefWidth(120);
        return spinner;
    }

    protected static Double parseDouble(String text) {
        try {
            return text == null ? null : Double.valueOf(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}