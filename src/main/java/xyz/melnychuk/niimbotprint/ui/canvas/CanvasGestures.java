package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.ui.canvas.element.ElementView;

public class CanvasGestures {

    private final PaneEditorCanvas canvas;
    private final SelectionOverlay overlay;
    private final ResizeRotationState rotation = new ResizeRotationState();

    public CanvasGestures(PaneEditorCanvas canvas, SelectionOverlay overlay) {
        this.canvas = canvas;
        this.overlay = overlay;
    }

    public void installDrag(Node node, ElementView view) {
        var element = view.element();
        final double[] offset = new double[2];

        node.setOnMousePressed(e -> {
            canvas.gestureStarted();
            canvas.select(element);
            var p = canvas.toLocal(e.getSceneX(), e.getSceneY());
            offset[0] = p.getX() - element.getX();
            offset[1] = p.getY() - element.getY();
            e.consume();
        });
        node.setOnMouseDragged(e -> {
            var p = canvas.toLocal(e.getSceneX(), e.getSceneY());
            double x = p.getX() - offset[0];
            double y = p.getY() - offset[1];
            if (canvas.isPositionSnap()) {
                Bounds b = view.node().getLayoutBounds();
                SnapEngine.SnapResult r = SnapEngine.snapPosition(x, y,
                        b.getWidth(), b.getHeight(), canvas.stickerWidth(), canvas.stickerHeight());
                x = r.x();
                y = r.y();
                overlay.showGuides(
                        r.vGuide() == null ? Double.NaN : r.vGuide(),
                        r.hGuide() == null ? Double.NaN : r.hGuide(),
                        canvas.stickerWidth(), canvas.stickerHeight());
            } else {
                overlay.hideGuides();
            }
            element.setX(x);
            element.setY(y);
            view.applyPosition();
            canvas.refreshSelection();
            canvas.changed(element);
            e.consume();
        });
        node.setOnMouseReleased(e -> {
            canvas.gestureEnded();
            overlay.hideGuides();
        });
    }

    public void installResize(Node handleNode, ResizeHandle handle) {
        handleNode.setOnMousePressed(e -> {
            var element = canvas.selected();
            if (element == null) {
                return;
            }
            ElementView view = canvas.viewOf(element);
            if (view == null) {
                return;
            }
            canvas.gestureStarted();
            view.beginResize();
            e.consume();
        });
        handleNode.setOnMouseDragged(e -> {
            var element = canvas.selected();
            ElementView view = element == null ? null : canvas.viewOf(element);
            if (view == null) {
                return;
            }
            var p = canvas.toLocal(e.getSceneX(), e.getSceneY());
            view.resize(handle, p.getX(), p.getY());
            canvas.refreshSelection();
            canvas.changed(element);
            e.consume();
        });
        handleNode.setOnMouseReleased(e -> canvas.gestureEnded());
    }

    public void installRotation() {
        Node node = overlay.rotationNode();
        node.setCursor(Cursor.MOVE);
        node.setOnMousePressed(e -> {
            var element = canvas.selected();
            ElementView view = element == null ? null : canvas.viewOf(element);
            if (view == null) {
                return;
            }
            canvas.gestureStarted();
            double[] c = centerOf(view.node());
            rotation.centerX = c[0];
            rotation.centerY = c[1];
            rotation.baseAngle = element.getRotation();
            rotation.basePointer = pointerAngle(e.getSceneX(), e.getSceneY(), c[0], c[1]);
            e.consume();
        });
        node.setOnMouseDragged(e -> {
            var element = canvas.selected();
            ElementView view = element == null ? null : canvas.viewOf(element);
            if (view == null) {
                return;
            }
            double angle = pointerAngle(e.getSceneX(), e.getSceneY(), rotation.centerX, rotation.centerY);
            double delta = normalizeDegrees(rotation.basePointer - angle);
            double target = normalizeDegrees(rotation.baseAngle + delta);
            double snapped = canvas.isRotationSnap() ? SnapEngine.snapRotation(target) : target;
            boolean isSnapped = snapped != target;
            element.setRotation(snapped);
            view.node().setRotate(element.getRotation());
            canvas.refreshSelection();
            overlay.setRotationHighlight(isSnapped);
            canvas.changed(element);
            e.consume();
        });
        node.setOnMouseReleased(e -> {
            canvas.gestureEnded();
            overlay.setRotationHighlight(false);
        });
    }

    private static double[] centerOf(Node node) {
        return new double[]{
                node.getLayoutX() + node.getLayoutBounds().getMinX() + node.getLayoutBounds().getWidth() / 2,
                node.getLayoutY() + node.getLayoutBounds().getMinY() + node.getLayoutBounds().getHeight() / 2
        };
    }

    private static double pointerAngle(double sceneX, double sceneY, double cx, double cy) {
        return Math.toDegrees(Math.atan2(cy - sceneY, sceneX - cx));
    }

    private static double normalizeDegrees(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private static final class ResizeRotationState {
        double centerX;
        double centerY;
        double baseAngle;
        double basePointer;
    }
}
