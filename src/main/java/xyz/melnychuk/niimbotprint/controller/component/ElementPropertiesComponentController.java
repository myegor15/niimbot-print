package xyz.melnychuk.niimbotprint.controller.component;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.StickerElement;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ElementPropertiesComponentController<T extends StickerElement> extends AbstractController {

    protected T element;
    private boolean updating;

    @Setter
    private Consumer<StickerElement> elementChangeListener;

    public final void show(StickerElement element) {
        this.element = (T) element;
        apply();
    }

    public final void sync(StickerElement element) {
        if (this.element != element) {
            return;
        }
        updating = true;
        sync();
        updating = false;
    }

    protected abstract void apply();

    protected abstract void sync();

    protected void touch() {
        if (elementChangeListener != null) {
            elementChangeListener.accept(element);
        }
    }

    protected <V> void bind(ObservableValue<V> property, BiConsumer<T, V> setter) {
        property.addListener(changeListener(setter));
    }

    private <V> ChangeListener<V> changeListener(BiConsumer<T, V> setter) {
        return (o, a, b) -> {
            if (updating) {
                return;
            }
            setter.accept(element, b);
            touch();
        };
    }

    protected void bindSpinner(Spinner<Double> spinner, double min, double max, BiConsumer<T, Double> setter, Supplier<Double> value) {
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, value.get(), 1));
        spinner.setEditable(true);
        bind(spinner.valueProperty(), setter);
        spinner.getEditor().textProperty().addListener((o, a, b) -> {
            Double v = parseDouble(b);
            if (v != null) {
                setter.accept(element, v);
                touch();
            }
        });
    }

    protected void syncSpinner(Spinner<Double> spinner, Supplier<Double> value) {
        if (element != null && spinner.getValueFactory() != null) {
            spinner.getValueFactory().setValue(value.get());
        }
    }

    protected static Double parseDouble(String text) {
        try {
            return text == null
                    ? null
                    : Double.valueOf(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
