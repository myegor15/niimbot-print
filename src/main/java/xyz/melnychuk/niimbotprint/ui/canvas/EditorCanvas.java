package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.model.Element;

import java.util.function.Consumer;

public interface EditorCanvas {

    Node getNode();

    void setLabelSize(double width, double height);

    void refresh();

    DoubleBinding contentWidth();

    DoubleBinding contentHeight();

    Element addElement(Element element);

    void updateElement(Element element);

    void removeElement(Element element);

    Element getSelectedElement();

    void select(Element element);

    void selectNone();

    void setSelectionVisible(boolean visible);

    void setGridVisible(boolean visible);

    boolean isGridVisible();

    void setPositionSnap(boolean positionSnap);

    boolean isPositionSnap();

    void setRotationSnap(boolean rotationSnap);

    boolean isRotationSnap();

    void hideGuides();

    void setSelectionListener(Consumer<Element> listener);

    void setChangeListener(Consumer<Element> listener);

    void setGestureListener(Consumer<Boolean> listener);
}
