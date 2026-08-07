package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.service.EditorHistoryService;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ElementPropertiesComponentController<T extends Element> extends AbstractController {

    protected T element;
    private boolean updating;

    @Setter
    private EditorHistoryService historyService;

    @Setter
    private Consumer<Element> elementChangeListener;

    public final void show(Element element) {
        this.element = (T) element;
        apply();
    }

    public final void sync(Element element) {
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
            commit(setter, b);
        };
    }

    private <V> void commit(BiConsumer<T, V> setter, V value) {
        historyService.withEdit(() -> {
            setter.accept(element, value);
            touch();
        });
    }

    protected void bindIntSpinner(Spinner<Integer> spinner, int min, int max, BiConsumer<T, Integer> setter, Supplier<Integer> value) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value.get(), 1));
        spinner.setEditable(true);
        bind(spinner.valueProperty(), setter);
        spinner.getEditor().textProperty().addListener((o, a, b) -> {
            Integer v = parseInt(b);
            if (v != null) {
                commit(setter, v);
            }
        });
    }

    protected void syncIntSpinner(Spinner<Integer> spinner, Supplier<Integer> value) {
        if (element != null && spinner.getValueFactory() != null) {
            spinner.getValueFactory().setValue(value.get());
        }
    }

    protected static Integer parseInt(String text) {
        try {
            return text == null ? null : Integer.valueOf(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected void bindSpinner(Spinner<Double> spinner, double min, double max, BiConsumer<T, Double> setter, Supplier<Double> value) {
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, value.get(), 1));
        spinner.setEditable(true);
        bind(spinner.valueProperty(), setter);
        spinner.getEditor().textProperty().addListener((o, a, b) -> {
            Double v = parseDouble(b);
            if (v != null) {
                commit(setter, v);
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
