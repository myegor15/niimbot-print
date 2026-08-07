package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SelectionOverlay {

    private static final double HANDLE_SIZE = 9;
    private static final double FRAME_HANDLE_OFFSET = 20;
    private static final Color GUIDE_COLOR = Color.rgb(255, 60, 60, 0.9);
    private static final Color HANDLE_COLOR = Color.rgb(30, 120, 255);
    private static final Color SNAP_COLOR = Color.rgb(60, 160, 60);

    private final Rectangle selectionBox = new Rectangle();
    private final Map<ResizeHandle, Rectangle> handles =
            Map.copyOf(ResizeHandle.cornersAndEdges().stream().collect(
                    Collectors.toMap(h -> h, h -> createHandle())));
    private final Group rotationHandle = new Group();
    private final Line rotStem = new Line();
    private final Circle rotTip = new Circle(HANDLE_SIZE / 2 + 2);
    private final Line vGuide = createGuide();
    private final Line hGuide = createGuide();

    public SelectionOverlay() {
        selectionBox.setFill(Color.TRANSPARENT);
        selectionBox.setStroke(Color.BLUE);
        selectionBox.getStrokeDashArray().addAll(4.0, 4.0);
        selectionBox.setMouseTransparent(true);
        selectionBox.setVisible(false);

        rotStem.setStroke(HANDLE_COLOR);
        rotStem.getStrokeDashArray().addAll(3.0, 3.0);
        rotTip.setFill(Color.WHITE);
        rotTip.setStroke(HANDLE_COLOR);
        rotationHandle.getChildren().addAll(rotStem, rotTip);
        rotationHandle.setVisible(false);
    }

    private static Rectangle createHandle() {
        Rectangle rect = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
        rect.setFill(Color.WHITE);
        rect.setStroke(HANDLE_COLOR);
        rect.setVisible(false);
        return rect;
    }

    private static Line createGuide() {
        Line line = new Line();
        line.setStroke(GUIDE_COLOR);
        line.setStrokeWidth(1);
        line.setMouseTransparent(true);
        line.setVisible(false);
        return line;
    }

    public List<Node> nodes() {
        List<Node> nodes = new ArrayList<>();
        nodes.add(vGuide);
        nodes.add(hGuide);
        nodes.add(selectionBox);
        ResizeHandle.cornersAndEdges().forEach(h -> nodes.add(handles.get(h)));
        nodes.add(rotationHandle);
        return nodes;
    }

    public Node rotationNode() {
        return rotationHandle;
    }

    public Map<ResizeHandle, Rectangle> handleNodes() {
        return handles;
    }

    public void show(Bounds bounds, double rotation, List<ResizeHandle> allowed) {
        boolean empty = bounds.getWidth() == 0 && bounds.getHeight() == 0;
        if (empty) {
            hideSelection();
            return;
        }
        selectionBox.setVisible(true);
        selectionBox.setX(bounds.getMinX());
        selectionBox.setY(bounds.getMinY());
        selectionBox.setWidth(bounds.getWidth());
        selectionBox.setHeight(bounds.getHeight());

        double half = HANDLE_SIZE / 2;
        handles.forEach((h, rect) -> {
            boolean show = allowed.contains(h);
            rect.setVisible(show);
            rect.setMouseTransparent(!show);
            if (show) {
                rect.setX(bounds.getMinX() + (h.sideX() < 0 ? 0 : h.sideX() > 0 ? bounds.getWidth() : bounds.getWidth() / 2) - half);
                rect.setY(bounds.getMinY() + (h.sideY() < 0 ? 0 : h.sideY() > 0 ? bounds.getHeight() : bounds.getHeight() / 2) - half);
                rect.setCursor(cursorFor(h));
            }
        });
        showRotation(bounds, rotation);
    }

    private void showRotation(Bounds bounds, double rotation) {
        double cx = bounds.getMinX() + bounds.getWidth() / 2;
        double cy = bounds.getMinY() + bounds.getHeight() / 2;
        double theta = Math.toRadians(rotation);
        double dx = Math.sin(theta);
        double dy = -Math.cos(theta);
        double txBound = dx > 0 ? (bounds.getMaxX() - cx) / dx
                : dx < 0 ? (bounds.getMinX() - cx) / dx : Double.POSITIVE_INFINITY;
        double tyBound = dy > 0 ? (bounds.getMaxY() - cy) / dy
                : dy < 0 ? (bounds.getMinY() - cy) / dy : Double.POSITIVE_INFINITY;
        double distance = Math.min(txBound, tyBound) + FRAME_HANDLE_OFFSET;
        rotStem.setStartX(cx);
        rotStem.setStartY(cy);
        rotStem.setEndX(cx + dx * distance);
        rotStem.setEndY(cy + dy * distance);
        rotTip.setCenterX(cx + dx * distance);
        rotTip.setCenterY(cy + dy * distance);
        rotationHandle.setVisible(true);
    }

    public void setRotationHighlight(boolean snapped) {
        Color color = snapped ? SNAP_COLOR : HANDLE_COLOR;
        rotStem.setStroke(color);
        rotTip.setStroke(color);
    }

    public void hideSelection() {
        selectionBox.setVisible(false);
        handles.values().forEach(r -> {
            r.setVisible(false);
            r.setMouseTransparent(true);
        });
        rotationHandle.setVisible(false);
        rotTip.setCenterX(0);
        rotTip.setCenterY(0);
        rotStem.setStartX(0);
        rotStem.setStartY(0);
        rotStem.setEndX(0);
        rotStem.setEndY(0);
    }

    public void showGuides(double v, double h, double w, double height) {
        if (Double.isNaN(v)) {
            vGuide.setVisible(false);
        } else {
            vGuide.setVisible(true);
            vGuide.setStartX(v);
            vGuide.setEndX(v);
            vGuide.setStartY(0);
            vGuide.setEndY(height);
        }
        if (Double.isNaN(h)) {
            hGuide.setVisible(false);
        } else {
            hGuide.setVisible(true);
            hGuide.setStartX(0);
            hGuide.setEndX(w);
            hGuide.setStartY(h);
            hGuide.setEndY(h);
        }
    }

    public void hideGuides() {
        vGuide.setVisible(false);
        hGuide.setVisible(false);
    }

    private static Cursor cursorFor(ResizeHandle h) {
        if (h.isCorner()) {
            if (h.sideX() == h.sideY()) {
                return h.sideX() > 0 ? Cursor.SE_RESIZE : Cursor.NW_RESIZE;
            }
            return h.sideX() > 0 ? Cursor.SW_RESIZE : Cursor.NE_RESIZE;
        }
        return h.sideX() != 0 ? Cursor.H_RESIZE : Cursor.V_RESIZE;
    }
}
