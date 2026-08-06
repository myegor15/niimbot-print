package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.model.Element;

public interface ElementView {

    Element element();

    Node node();

    void applyPosition();

    void refresh();

    void beginResize();

    void resize(double scale, double newX, double newY);

    default void resizeAxis(double newWidth, double newHeight, double newX, double newY) {
        // Uses only for supportsAxisResize() == true,
    }

    default boolean supportsAxisResize() {
        return false;
    }
}
