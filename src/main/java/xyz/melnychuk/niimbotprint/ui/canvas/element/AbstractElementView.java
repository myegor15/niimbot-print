package xyz.melnychuk.niimbotprint.ui.canvas.element;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.ui.canvas.ResizeHandle;

import java.util.List;

public abstract class AbstractElementView<T extends Element> implements ElementView {

    protected static final double MIN_SIZE = 10;

    private final T element;

    protected double baseW;
    private double baseH;
    private double baseAX;
    private double baseAY;

    protected AbstractElementView(T element) {
        this.element = element;
    }

    @Override
    public T element() {
        return element;
    }

    @Override
    public void beginResize() {
        Node n = node();
        Bounds b = n.getLayoutBounds();
        baseW = b.getWidth();
        baseH = b.getHeight();
        baseAX = n.getLayoutX() + b.getMinX();
        baseAY = n.getLayoutY() + b.getMinY();
    }

    @Override
    public List<ResizeHandle> resizeHandles() {
        return ResizeHandle.corners();
    }

    @Override
    public void resize(ResizeHandle handle, double targetX, double targetY) {
        double fixedX = baseAX + (handle.sideX() < 0 ? baseW : 0);
        double fixedY = baseAY + (handle.sideY() < 0 ? baseH : 0);
        double newX;
        double newY;
        double newW;
        double newH;
        if (handle.isCorner()) {
            double k = Math.hypot(fixedX - targetX, fixedY - targetY)
                    / Math.hypot(baseW, baseH);
            k = Math.max(k, MIN_SIZE / Math.max(1, Math.min(baseW, baseH)));
            newW = baseW * k;
            newH = baseH * k;
            newX = handle.sideX() < 0 ? fixedX - newW : fixedX;
            newY = handle.sideY() < 0 ? fixedY - newH : fixedY;
            newX = Math.min(newX, fixedX);
            newY = Math.min(newY, fixedY);
        } else {
            newW = baseW;
            newH = baseH;
            if (handle.sideX() != 0) {
                newW = Math.max(MIN_SIZE, Math.abs(targetX - fixedX));
            }
            if (handle.sideY() != 0) {
                newH = Math.max(MIN_SIZE, Math.abs(targetY - fixedY));
            }
            newX = handle.sideX() < 0 ? fixedX - newW : baseAX;
            newY = handle.sideY() < 0 ? fixedY - newH : baseAY;
        }
        applySize(newW, newH, newX, newY);
    }

    protected abstract void applySize(double newWidth, double newHeight, double newX, double newY);

    protected void refreshNode() {
        refresh();
        applyPosition();
    }
}
