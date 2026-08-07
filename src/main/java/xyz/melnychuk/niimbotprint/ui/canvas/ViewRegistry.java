package xyz.melnychuk.niimbotprint.ui.canvas;

import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.ui.canvas.element.ElementView;

import java.util.IdentityHashMap;
import java.util.Map;

public class ViewRegistry {

    private final Map<Element, ElementView> views = new IdentityHashMap<>();

    public ElementView attach(ElementView view) {
        views.put(view.element(), view);
        return view;
    }

    public ElementView get(Element element) {
        return views.get(element);
    }

    public ElementView remove(Element element) {
        return views.remove(element);
    }

    public void clear() {
        views.clear();
    }

    public Iterable<ElementView> all() {
        return views.values();
    }
}
