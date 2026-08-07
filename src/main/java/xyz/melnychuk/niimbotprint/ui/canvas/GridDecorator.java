package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class GridDecorator {

    private static final double GRID_CELLS = 10;
    private static final Color GRID_COLOR = Color.rgb(0, 0, 0, 0.08);
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private final Rectangle background = new Rectangle();
    private final Group grid = new Group();

    private double width;
    private double height;
    private boolean visible = true;

    public void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        background.setWidth(width);
        background.setHeight(height);
        rebuildGrid();
    }

    public void setGridVisible(boolean visible) {
        this.visible = visible;
        grid.setVisible(visible);
    }

    public boolean isVisible() {
        return visible;
    }

    private void rebuildGrid() {
        if (width <= 0 || height <= 0) {
            return;
        }
        grid.getChildren().clear();
        double stepX = adaptiveStep(width);
        double stepY = adaptiveStep(height);
        for (double x = stepX; x < width; x += stepX) {
            grid.getChildren().add(gridLine(x, 0, x, height));
        }
        for (double y = stepY; y < height; y += stepY) {
            grid.getChildren().add(gridLine(0, y, width, y));
        }
        grid.setMouseTransparent(true);
        grid.setVisible(visible);
    }

    private static Line gridLine(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(GRID_COLOR);
        return line;
    }

    private static double adaptiveStep(double size) {
        int cells = (int) Math.max(1, Math.round(size / GRID_CELLS));
        return size / cells;
    }

    public void install(Pane parent) {
        background.setFill(BACKGROUND_COLOR);
        parent.getChildren().add(background);
        if (!grid.getChildren().isEmpty()) {
            parent.getChildren().add(grid);
        }
    }
}