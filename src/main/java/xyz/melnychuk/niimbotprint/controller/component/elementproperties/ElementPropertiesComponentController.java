package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.model.Element;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ElementPropertiesComponentController<T extends Element> extends AbstractController {

    protected T element;
    private boolean updating;

    private Consumer<Element> elementChangeListener = e -> {};

    public void setElementChangeListener(Consumer<Element> elementChangeListener) {
        this.elementChangeListener = elementChangeListener == null ? e -> {} : elementChangeListener;
    }

    public final void show(Element element) {
        this.element = (T) element;
        updating = true;
        apply();
        updating = false;
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
        elementChangeListener.accept(element);
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
        getAppContext().getEditorHistoryService().withEdit(() -> {
            setter.accept(element, value);
            touch();
        });
    }

    protected void bindIntSpinner(Spinner<Integer> spinner, int min, int max, BiConsumer<T, Integer> setter, Supplier<Integer> value) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, value.get(), 1));
        spinner.setEditable(true);
        bind(spinner.valueProperty(), setter);
    }

    protected void syncIntSpinner(Spinner<Integer> spinner, Supplier<Integer> value) {
        if (spinner.getValueFactory() != null) {
            spinner.getValueFactory().setValue(value.get());
        }
    }
}
