package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.model.Sticker;
import xyz.melnychuk.niimbotprint.model.Element;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class StickerCanvas extends Pane {

    private static final double HANDLE_SIZE = 9;
    private static final double MIN_SIZE = 10;
    private static final double GRID_CELLS = 10;
    private static final double ROTATION_DISTANCE = 40;
    private static final Color GRID_COLOR = Color.rgb(0, 0, 0, 0.08);
    private static final Color GUIDE_COLOR = Color.rgb(255, 60, 60, 0.9);
    private static final Color HANDLE_COLOR = Color.rgb(30, 120, 255);
    private static final Color SNAP_COLOR = Color.rgb(60, 160, 60);
    private static final int[][] SIDES = {
            {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}
    };

    private Sticker sticker;

    @Getter
    private Element selectedElement;

    private Rectangle background;

    private Group grid;

    @Getter
    private boolean gridVisible = true;
    @Getter
    @Setter
    private boolean positionSnap = true;
    @Getter
    @Setter
    private boolean rotationSnap = true;

    private Line vGuide;
    private Line hGuide;

    private Rectangle selectionBox;
    private final Rectangle[] handles = new Rectangle[8];

    private final Group rotationHandle = new Group();
    private Line rotStem;
    private Circle rotTip;

    private final double[] rotation = new double[8];

    private final Map<Element, ElementView> views = new IdentityHashMap<>();

    @Setter
    private Consumer<Element> selectionListener = e -> {};

    @Setter
    private Consumer<Element> changeListener = e -> {};

    public StickerCanvas(Sticker sticker) {
        this.sticker = sticker;
        getStyleClass().add("canvas");
        buildBackground();
        buildHandles();
        setOnMousePressed(e -> selectNone());
        buildGrid();
        ensureGuides();
        refresh();
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        buildBackground();
        buildGrid();
        setLabelSize(sticker.getWidth(), sticker.getHeight());
        refresh();
    }

    public void setLabelSize(int width, int height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        background.setWidth(width);
        background.setHeight(height);
        buildGrid();
        updateSelectionBox();
    }

    private void buildBackground() {
        if (background != null) {
            getChildren().remove(background);
        }
        background = new Rectangle(sticker.getWidth(), sticker.getHeight());
        background.setFill(Color.WHITE);
        getChildren().add(0, background);
    }

    private void buildGrid() {
        if (grid != null) {
            getChildren().remove(grid);
        }
        grid = new Group();
        double width = sticker.getWidth();
        double height = sticker.getHeight();
        double stepX = adaptiveStep(width);
        double stepY = adaptiveStep(height);
        for (double x = stepX; x < width; x += stepX) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(GRID_COLOR);
            grid.getChildren().add(line);
        }
        for (double y = stepY; y < height; y += stepY) {
            Line line = new Line(0, y, width, y);
            line.setStroke(GRID_COLOR);
            grid.getChildren().add(line);
        }
        grid.setMouseTransparent(true);
        grid.setVisible(gridVisible);
        getChildren().add(1, grid);
    }

    private static double adaptiveStep(double size) {
        int cells = (int) Math.max(1, Math.round(size / GRID_CELLS));
        return size / cells;
    }

    public void setGridVisible(boolean visible) {
        gridVisible = visible;
        if (grid != null) {
            grid.setVisible(visible);
        }
    }

    private void ensureGuides() {
        if (vGuide == null) {
            vGuide = new Line();
            vGuide.setStroke(GUIDE_COLOR);
            vGuide.setStrokeWidth(1);
            vGuide.setMouseTransparent(true);
            vGuide.setVisible(false);
            insertBeforeHandles(vGuide);
        }
        if (hGuide == null) {
            hGuide = new Line();
            hGuide.setStroke(GUIDE_COLOR);
            hGuide.setStrokeWidth(1);
            hGuide.setMouseTransparent(true);
            hGuide.setVisible(false);
            insertBeforeHandles(hGuide);
        }
    }

    private void buildHandles() {
        for (int i = 0; i < handles.length; i++) {
            Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
            handle.setFill(Color.WHITE);
            handle.setStroke(HANDLE_COLOR);
            handle.setVisible(false);
            installResizeHandler(handle, side(i));
            handles[i] = handle;
            getChildren().add(handle);
        }
        rotStem = new Line();
        rotStem.setStroke(HANDLE_COLOR);
        rotStem.getStrokeDashArray().addAll(3.0, 3.0);
        rotTip = new Circle(HANDLE_SIZE / 2 + 2);
        rotTip.setFill(Color.WHITE);
        rotTip.setStroke(HANDLE_COLOR);
        rotationHandle.getChildren().addAll(rotStem, rotTip);
        rotationHandle.setVisible(false);
        installRotationHandler(rotationHandle);
        getChildren().add(rotationHandle);
    }

    private int[] side(int index) {
        return SIDES[index];
    }

    public void refresh() {
        views.values().forEach(v -> getChildren().remove(v.node()));
        views.clear();
        for (Element element : sticker.getElements()) {
            attach(ElementViewFactory.getElementView(element));
        }
        updateSelectionBox();
    }

    private void attach(ElementView view) {
        Element element = view.element();
        Node node = view.node();
        node.setRotate(element.getRotation());
        views.put(element, view);
        insertBeforeHandles(node);
        makeDraggable(node, element);
    }

    private void insertBeforeHandles(Node node) {
        int index = Math.max(1, getChildren().size() - handles.length - 1);
        getChildren().add(index, node);
    }

    public Element addElement(Element element) {
        sticker.getElements().add(element);
        attach(ElementViewFactory.getElementView(element));
        select(element);
        return element;
    }

    public void updateElement(Element element) {
        ElementView view = views.get(element);
        if (view == null) {
            return;
        }
        view.node().setRotate(element.getRotation());
        view.refresh();
        view.applyPosition();
        updateSelectionBox();
        changeListener.accept(element);
    }

    public void removeSelected() {
        if (selectedElement == null) {
            return;
        }
        sticker.getElements().remove(selectedElement);
        ElementView view = views.remove(selectedElement);
        getChildren().remove(view.node());
        selectNone();
    }

    public void setSelectionVisible(boolean visible) {
        if (selectionBox != null) {
            selectionBox.setVisible(visible);
        }
    }

    public void select(Element element) {
        selectedElement = element;
        updateSelectionBox();
        selectionListener.accept(element);
    }

    public void selectNone() {
        if (selectedElement == null) {
            return;
        }
        selectedElement = null;
        updateSelectionBox();
        selectionListener.accept(null);
    }

    private void makeDraggable(Node node, Element element) {
        double[] start = new double[2];
        node.setOnMousePressed(e -> {
            select(element);
            Point2D p = sceneToLocal(e.getSceneX(), e.getSceneY());
            start[0] = p.getX() - element.getX();
            start[1] = p.getY() - element.getY();
            e.consume();
        });
        node.setOnMouseDragged(e -> {
            Point2D p = sceneToLocal(e.getSceneX(), e.getSceneY());
            double x = clamp(p.getX() - start[0], 0, sticker.getWidth());
            double y = clamp(p.getY() - start[1], 0, sticker.getHeight());
            if (positionSnap) {
                Bounds bounds = views.get(element).node().getLayoutBounds();
                double[] snapped = SnapEngine.snapPosition(x, y,
                        bounds.getWidth(), bounds.getHeight(),
                        sticker.getWidth(), sticker.getHeight(), vGuide, hGuide);
                x = snapped[0];
                y = snapped[1];
            } else {
                hideGuides();
            }
            element.setX(x);
            element.setY(y);
            views.get(element).applyPosition();
            updateSelectionBox();
            changeListener.accept(element);
            e.consume();
        });
        node.setOnMouseReleased(e -> hideGuides());
    }

    public void hideGuides() {
        ensureGuides();
        vGuide.setVisible(false);
        hGuide.setVisible(false);
    }

    private void installResizeHandler(Rectangle handle, int[] side) {
        final double[] start = new double[8];
        handle.setOnMousePressed(e -> {
            if (selectedElement == null) {
                return;
            }
            ElementView view = views.get(selectedElement);
            if (view == null) {
                return;
            }
            Node sel = view.node();
            Bounds b = sel.getLayoutBounds();
            start[2] = b.getWidth();
            start[3] = b.getHeight();
            view.beginResize();
            double ax = sel.getLayoutX() + b.getMinX();
            double ay = sel.getLayoutY() + b.getMinY();
            start[0] = ax;
            start[1] = ay;
            start[5] = ax + (side[0] < 0 ? start[2] : 0);
            start[6] = ay + (side[1] < 0 ? start[3] : 0);
            e.consume();
        });
        handle.setOnMouseDragged(e -> {
            Point2D p = sceneToLocal(e.getSceneX(), e.getSceneY());
            resize(side, start, p.getX(), p.getY());
        });
    }

    private void resize(int[] side, double[] start, double x, double y) {
        if (selectedElement == null) {
            return;
        }
        boolean corner = side[0] != 0 && side[1] != 0;
        if (corner) {
            resizeCorner(side, start, x, y);
        } else {
            resizeAxis(side, start, x, y);
        }
    }

    private void resizeCorner(int[] corner, double[] start, double x, double y) {
        double origW = start[2] > 0 ? start[2] : 1;
        double origH = start[3] > 0 ? start[3] : 1;
        double sx = clamp(x, 0, sticker.getWidth());
        double sy = clamp(y, 0, sticker.getHeight());
        double k = Math.hypot(start[5] - sx, start[6] - sy) / Math.hypot(origW, origH);
        k = Math.max(k, MIN_SIZE / Math.max(1, Math.min(origW, origH)));

        double newX = corner[0] < 0 ? start[5] - origW * k : start[5];
        double newY = corner[1] < 0 ? start[6] - origH * k : start[6];
        newX = clamp(newX, 0, sticker.getWidth() - MIN_SIZE);
        newY = clamp(newY, 0, sticker.getHeight() - MIN_SIZE);
        newX = Math.min(newX, start[5]);
        newY = Math.min(newY, start[6]);

        views.get(selectedElement).resize(k, newX, newY);
        updateElement(selectedElement);
    }

    private void resizeAxis(int[] side, double[] start, double x, double y) {
        double newW = start[2];
        double newH = start[3];
        if (side[0] != 0) {
            newW = Math.max(MIN_SIZE, Math.abs(clamp(x, 0, sticker.getWidth()) - start[5]));
        }
        if (side[1] != 0) {
            newH = Math.max(MIN_SIZE, Math.abs(clamp(y, 0, sticker.getHeight()) - start[6]));
        }
        double newX = side[0] != 0
                ? clamp(side[0] < 0 ? start[5] - newW : start[5], 0, sticker.getWidth() - MIN_SIZE)
                : start[0];
        double newY = side[1] != 0
                ? clamp(side[1] < 0 ? start[6] - newH : start[6], 0, sticker.getHeight() - MIN_SIZE)
                : start[1];
        views.get(selectedElement).resizeAxis(newW, newH, newX, newY);
        updateElement(selectedElement);
    }

    private void installRotationHandler(Node handle) {
        handle.setCursor(Cursor.MOVE);
        handle.setOnMousePressed(e -> {
            if (selectedElement == null) {
                return;
            }
            ElementView view = views.get(selectedElement);
            if (view == null) {
                return;
            }
            double[] c = centerOf(view.node());
            rotation[0] = c[0];
            rotation[1] = c[1];
            rotation[2] = selectedElement.getRotation();
            rotation[3] = pointerAngle(e.getSceneX(), e.getSceneY(), c[0], c[1]);
            e.consume();
        });
        handle.setOnMouseDragged(e -> {
            if (selectedElement == null) {
                return;
            }
            double angle = pointerAngle(e.getSceneX(), e.getSceneY(), rotation[0], rotation[1]);
            double delta = normalizeDegrees(rotation[3] - angle);
            double target = normalizeDegrees(rotation[2] + delta);
            double snapped = rotationSnap ? SnapEngine.snapRotation(target) : target;
            selectedElement.setRotation(snapped);
            Node sel = views.get(selectedElement).node();
            sel.setRotate(selectedElement.getRotation());
            updateSelectionBox();
            setRotationHighlight(rotationSnap && snapped != target);
            changeListener.accept(selectedElement);
            e.consume();
        });
    }

    private void setRotationHighlight(boolean snapped) {
        Color color = snapped ? SNAP_COLOR : HANDLE_COLOR;
        rotStem.setStroke(color);
        rotTip.setStroke(color);
    }

    private double[] centerOf(Node node) {
        Bounds bounds = node.getLayoutBounds();
        return new double[]{
                node.getLayoutX() + bounds.getMinX() + bounds.getWidth() / 2,
                node.getLayoutY() + bounds.getMinY() + bounds.getHeight() / 2
        };
    }

    private static double pointerAngle(double sceneX, double sceneY, double cx, double cy) {
        return Math.toDegrees(Math.atan2(cy - sceneY, sceneX - cx));
    }

    private static double normalizeDegrees(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateSelectionBox() {
        if (selectionBox == null) {
            selectionBox = new Rectangle();
            selectionBox.setFill(Color.TRANSPARENT);
            selectionBox.setStroke(Color.BLUE);
            selectionBox.getStrokeDashArray().addAll(4.0, 4.0);
            selectionBox.setMouseTransparent(true);
            insertBeforeHandles(selectionBox);
        }
        ElementView view = selectedElement == null ? null : views.get(selectedElement);
        Node node = view == null ? null : view.node();
        if (node == null) {
            selectionBox.setVisible(false);
            setHandlesVisible(false);
            return;
        }
        Bounds bounds = node.getBoundsInParent();
        if (bounds.getWidth() == 0 && bounds.getHeight() == 0) {
            Platform.runLater(this::updateSelectionBox);
            return;
        }
        double x = bounds.getMinX();
        double y = bounds.getMinY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        selectionBox.setVisible(true);
        selectionBox.setX(x);
        selectionBox.setY(y);
        selectionBox.setWidth(w);
        selectionBox.setHeight(h);
        updateHandles(x, y, w, h);
        updateRotationHandle(x, y, w, h);
        setHandlesVisible(true);
    }

    private void updateRotationHandle(double x, double y, double w, double h) {
        double cx = x + w / 2;
        double cy = y + h / 2;
        double theta = Math.toRadians(selectedElement.getRotation());
        double tx = cx + ROTATION_DISTANCE * Math.sin(theta);
        double ty = cy - ROTATION_DISTANCE * Math.cos(theta);
        rotStem.setStartX(cx);
        rotStem.setStartY(cy);
        rotStem.setEndX(tx);
        rotStem.setEndY(ty);
        rotTip.setCenterX(tx);
        rotTip.setCenterY(ty);
        rotationHandle.setVisible(true);
    }

    private void updateHandles(double x, double y, double w, double h) {
        double half = HANDLE_SIZE / 2;
        for (int i = 0; i < handles.length; i++) {
            int[] side = SIDES[i];
            Rectangle handle = handles[i];
            handle.setX(x + (side[0] < 0 ? 0 : side[0] > 0 ? w : w / 2) - half);
            handle.setY(y + (side[1] < 0 ? 0 : side[1] > 0 ? h : h / 2) - half);
            handle.setCursor(cursorFor(side));
        }
    }

    private Cursor cursorFor(int[] side) {
        boolean corner = side[0] != 0 && side[1] != 0;
        if (corner) {
            return side[0] == side[1]
                    ? (side[0] > 0 ? Cursor.SE_RESIZE : Cursor.NW_RESIZE)
                    : (side[0] > 0 ? Cursor.SW_RESIZE : Cursor.NE_RESIZE);
        }
        return side[0] != 0 ? Cursor.H_RESIZE : Cursor.V_RESIZE;
    }

    private void setHandlesVisible(boolean visible) {
        ElementView view = selectedElement == null ? null : views.get(selectedElement);
        boolean axis = view != null && view.supportsAxisResize();
        for (int i = 0; i < handles.length; i++) {
            boolean isCorner = SIDES[i][0] != 0 && SIDES[i][1] != 0;
            boolean show = visible && (axis || isCorner);
            handles[i].setVisible(show);
            handles[i].setMouseTransparent(!show);
        }
        rotationHandle.setVisible(visible);
        rotationHandle.setMouseTransparent(!visible);
        if (!visible) {
            rotTip.setCenterX(0);
            rotTip.setCenterY(0);
            rotStem.setStartX(0);
            rotStem.setStartY(0);
            rotStem.setEndX(0);
            rotStem.setEndY(0);
        }
    }
}
