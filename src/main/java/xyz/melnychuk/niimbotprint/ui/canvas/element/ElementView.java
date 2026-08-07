package xyz.melnychuk.niimbotprint.ui.canvas.element;

import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.ui.canvas.ResizeHandle;

import java.util.List;

public interface ElementView {

    Element element();

    Node node();

    void applyPosition();

    void refresh();

    void beginResize();

    List<ResizeHandle> resizeHandles();

    void resize(ResizeHandle handle, double targetX, double targetY);
}
