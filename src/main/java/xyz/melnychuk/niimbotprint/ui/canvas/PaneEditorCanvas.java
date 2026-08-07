package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.ui.canvas.element.ElementView;
import xyz.melnychuk.niimbotprint.ui.canvas.element.ElementViewFactory;

import java.util.function.Consumer;

public class PaneEditorCanvas extends Pane implements EditorCanvas {

    private final Sticker sticker;

    private Element selectedElement;

    private boolean positionSnap = true;
    private boolean rotationSnap = true;
    private boolean gridVisible = true;

    private final GridDecorator grid = new GridDecorator();
    private final SelectionOverlay overlay = new SelectionOverlay();
    private final ViewRegistry views = new ViewRegistry();
    private final CanvasGestures gestures;

    private final ObjectProperty<Object> zoneVersion = new SimpleObjectProperty<>(new Object());

    private Consumer<Element> selectionListener = e -> {};
    private Consumer<Element> changeListener = e -> {};
    private Consumer<Boolean> gestureListener = b -> {};

    public PaneEditorCanvas(Sticker sticker) {
        this.sticker = sticker;
        getStyleClass().add("canvas");
        setLabelSize(sticker.getWidth(), sticker.getHeight());
        grid.install(this);
        overlay.nodes().forEach(getChildren()::add);
        gestures = new CanvasGestures(this, overlay);
        installHandlers();
        setOnMousePressed(e -> selectNone());
        refresh();
    }

    private void installHandlers() {
        overlay.handleNodes().forEach((side, node) -> gestures.installResize(node, side));
        gestures.installRotation();
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void setLabelSize(double width, double height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        grid.setSize(width, height);
    }

    @Override
    public void setGridVisible(boolean visible) {
        gridVisible = visible;
        grid.setGridVisible(visible);
    }

    @Override
    public boolean isGridVisible() {
        return gridVisible;
    }

    @Override
    public void setPositionSnap(boolean positionSnap) {
        this.positionSnap = positionSnap;
    }

    @Override
    public boolean isPositionSnap() {
        return positionSnap;
    }

    @Override
    public void setRotationSnap(boolean rotationSnap) {
        this.rotationSnap = rotationSnap;
    }

    @Override
    public boolean isRotationSnap() {
        return rotationSnap;
    }

    @Override
    public void setSelectionListener(Consumer<Element> listener) {
        selectionListener = listener;
    }

    @Override
    public void setChangeListener(Consumer<Element> listener) {
        changeListener = listener;
    }

    @Override
    public void setGestureListener(Consumer<Boolean> listener) {
        gestureListener = listener;
    }

    @Override
    public DoubleBinding contentWidth() {
        return extentBinding(true);
    }

    @Override
    public DoubleBinding contentHeight() {
        return extentBinding(false);
    }

    private DoubleBinding extentBinding(boolean horizontal) {
        return Bindings.createDoubleBinding(
                () -> {
                    double min = 0;
                    double max = horizontal ? sticker.getWidth() : sticker.getHeight();
                    for (ElementView view : views.all()) {
                        Bounds b = view.node().getBoundsInParent();
                        min = Math.min(min, horizontal ? b.getMinX() : b.getMinY());
                        max = Math.max(max, horizontal ? b.getMaxX() : b.getMaxY());
                    }
                    return max - min;
                },
                zoneVersion);
    }

    @Override
    public void refresh() {
        for (ElementView view : views.all()) {
            getChildren().remove(view.node());
        }
        views.clear();
        for (Element element : sticker.getElements()) {
            attach(ElementViewFactory.getElementView(element));
        }
        refreshSelection();
    }

    private void attach(ElementView view) {
        int index = Math.max(1, getChildren().size() - overlay.nodes().size());
        getChildren().add(index, view.node());
        views.attach(view);
        view.node().setRotate(view.element().getRotation());
        gestures.installDrag(view.node(), view);
    }

    @Override
    public Element addElement(Element element) {
        sticker.getElements().add(element);
        attach(ElementViewFactory.getElementView(element));
        select(element);
        return element;
    }

    @Override
    public void updateElement(Element element) {
        ElementView view = views.get(element);
        if (view == null) {
            return;
        }
        view.node().setRotate(element.getRotation());
        view.refresh();
        view.applyPosition();
        refreshSelection();
        changeListener.accept(element);
    }

    @Override
    public void removeElement(Element element) {
        if (element == null) {
            return;
        }
        sticker.getElements().remove(element);
        ElementView view = views.remove(element);
        if (view != null) {
            getChildren().remove(view.node());
        }
        if (selectedElement == element) {
            selectNone();
        }
    }

    @Override
    public void setSelectionVisible(boolean visible) {
        if (visible) {
            refreshSelection();
        } else {
            overlay.hideSelection();
        }
    }

    @Override
    public Element getSelectedElement() {
        return selectedElement;
    }

    @Override
    public void select(Element element) {
        selectedElement = element;
        refreshSelection();
        selectionListener.accept(element);
    }

    @Override
    public void selectNone() {
        if (selectedElement == null) {
            return;
        }
        selectedElement = null;
        overlay.hideSelection();
        selectionListener.accept(null);
    }

    @Override
    public void hideGuides() {
        overlay.hideGuides();
    }

    Element selected() {
        return selectedElement;
    }

    ElementView viewOf(Element element) {
        return views.get(element);
    }

    void gestureStarted() {
        gestureListener.accept(true);
    }

    void gestureEnded() {
        gestureListener.accept(false);
    }

    void changed(Element element) {
        changeListener.accept(element);
    }

    void refreshSelection() {
        zoneVersion.set(new Object());
        ElementView view = selectedElement == null ? null : views.get(selectedElement);
        if (view == null) {
            overlay.hideSelection();
            return;
        }
        Node node = view.node();
        Bounds bounds = node.getBoundsInParent();
        if (bounds.getWidth() == 0 && bounds.getHeight() == 0) {
            Platform.runLater(this::refreshSelection);
            return;
        }
        overlay.show(bounds, selectedElement.getRotation(), view.resizeHandles());
    }

    Point2D toLocal(double sceneX, double sceneY) {
        return sceneToLocal(sceneX, sceneY);
    }

    double stickerWidth() {
        return sticker.getWidth();
    }

    double stickerHeight() {
        return sticker.getHeight();
    }
}
