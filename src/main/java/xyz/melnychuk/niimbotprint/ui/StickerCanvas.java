package xyz.melnychuk.niimbotprint.ui;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.model.*;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class StickerCanvas extends Pane {

    private static final double HANDLE_SIZE = 9;
    private static final double MIN_SIZE = 10;
    private static final double SNAP_THRESHOLD = 6;
    private static final double GRID_CELLS = 10;
    private static final double ROTATION_DISTANCE = 40;
    private static final double ROTATION_SNAP_THRESHOLD = 5;
    private static final Color GRID_COLOR = Color.rgb(0, 0, 0, 0.08);
    private static final Color GUIDE_COLOR = Color.rgb(255, 60, 60, 0.9);
    private static final Color HANDLE_COLOR = Color.rgb(30, 120, 255);
    private static final Color SNAP_COLOR = Color.rgb(60, 160, 60);
    private static final double[] SNAP_OFFSETS = {0, 0.5, 1};

    private Sticker sticker;

    @Getter
    private StickerElement selectedElement;

    private Rectangle background;

    private Group grid;
    private boolean gridVisible = true;

    private boolean positionSnap = true;
    private boolean rotationSnap = true;

    private Line vGuide;
    private Line hGuide;

    private Rectangle selectionBox;

    private final Rectangle[] handles = new Rectangle[4];

    private final Group rotationHandle = new Group();
    private Line rotStem;
    private Circle rotTip;

    private final double[] rotation = new double[8];

    private final Map<StickerElement, Node> nodes = new IdentityHashMap<>();

    @Setter
    private Consumer<StickerElement> selectionListener = e -> {};

    @Setter
    private Consumer<StickerElement> changeListener = e -> {};

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

    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setPositionSnap(boolean enabled) {
        positionSnap = enabled;
    }

    public boolean isPositionSnap() {
        return positionSnap;
    }

    public void setRotationSnap(boolean enabled) {
        rotationSnap = enabled;
    }

    public boolean isRotationSnap() {
        return rotationSnap;
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
            installResizeHandler(handle, corner(i));
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

    private int[] corner(int index) {
        return new int[]{
                (index & 1) == 0 ? -1 : 1,
                (index & 2) == 0 ? -1 : 1
        };
    }

    public void refresh() {
        nodes.values().forEach(getChildren()::remove);
        nodes.clear();
        for (StickerElement element : sticker.getElements()) {
            Node node = createNode(element);
            nodes.put(element, node);
            insertBeforeHandles(node);
            makeDraggable(node, element);
        }
        updateSelectionBox();
    }

    private void insertBeforeHandles(Node node) {
        int index = Math.max(1, getChildren().size() - handles.length - 1);
        getChildren().add(index, node);
    }

    public StickerElement addElement(StickerElement element) {
        sticker.getElements().add(element);
        Node node = createNode(element);
        nodes.put(element, node);
        insertBeforeHandles(node);
        makeDraggable(node, element);
        select(element);
        return element;
    }

    public void updateElement(StickerElement element) {
        Node node = nodes.get(element);
        if (node == null) {
            return;
        }
        updateNode(node, element);
        node = nodes.get(element);
        applyPosition(node, element);
        updateSelectionBox();
        changeListener.accept(element);
    }

    public void removeSelected() {
        if (selectedElement == null) {
            return;
        }
        sticker.getElements().remove(selectedElement);
        getChildren().remove(nodes.remove(selectedElement));
        selectNone();
    }

    public void setSelectionVisible(boolean visible) {
        if (selectionBox != null) {
            selectionBox.setVisible(visible);
        }
    }

    public void select(StickerElement element) {
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

    private Node createNode(StickerElement element) {
        Node node;
        if (element instanceof TextElement text) {
            node = createText(text);
        } else if (element instanceof ImageElement image) {
            node = createImage(image);
        } else if (element instanceof BarcodeElement barcode) {
            node = createBarcode(barcode);
        } else {
            throw new IllegalArgumentException("Unknown element: " + element);
        }
        node.setRotate(element.getRotation());
        return node;
    }

    private void updateNode(Node node, StickerElement element) {
        if (element instanceof TextElement text && node instanceof Label label) {
            label.setText(text.getText());
            label.setFont(font(text));
        } else if (element instanceof ImageElement image && node instanceof ImageView view) {
            view.setFitWidth(image.getWidth());
            view.setFitHeight(image.getHeight());
        } else if (element instanceof BarcodeElement) {
            getChildren().remove(node);
            Node newNode = createBarcode((BarcodeElement) element);
            newNode.setRotate(element.getRotation());
            nodes.put(element, newNode);
            insertBeforeHandles(newNode);
            makeDraggable(newNode, element);
        }
        node.setRotate(element.getRotation());
    }

    private Node createText(TextElement element) {
        Font font = font(element);
        Label text = new Label(element.getText());
        text.setFont(font);
        text.setPadding(Insets.EMPTY);
        text.setLayoutX(element.getX());
        text.setLayoutY(element.getY());
        return text;
    }

    private Node createImage(ImageElement element) {
        ImageView view = new ImageView(decodeBase64(element.getImageBase64()));
        view.setFitWidth(element.getWidth());
        view.setFitHeight(element.getHeight());
        view.setPreserveRatio(true);
        view.setLayoutX(element.getX());
        view.setLayoutY(element.getY());
        return view;
    }

    private Node createBarcode(BarcodeElement element) {
        try {
            var image = BarcodeGenerator.generate(element.getContent(), element.getFormat(),
                    (int) element.getWidth(), (int) element.getHeight());
            ImageView view = new ImageView(SwingFXUtils.toFXImage(image, null));
            view.setFitWidth(element.getWidth());
            view.setFitHeight(element.getHeight());
            view.setPreserveRatio(false);
            view.setLayoutX(element.getX());
            view.setLayoutY(element.getY());
            return view;
        } catch (Exception e) {
            log.warn("Barcode cannot be rendered for content '{}' format '{}': {}",
                    element.getContent(), element.getFormat(), e.getMessage());
            Label error = new Label("Недопустимый штрихкод");
            error.setLayoutX(element.getX());
            error.setLayoutY(element.getY());
            return error;
        }
    }

    private static Font font(TextElement element) {
        return Font.font(element.getFontFamily(),
                element.isBold() ? FontWeight.BOLD : FontWeight.NORMAL,
                element.getFontSize());
    }

    private void makeDraggable(Node node, StickerElement element) {
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
                double[] snapped = snapPosition(x, y, node);
                x = snapped[0];
                y = snapped[1];
            } else {
                hideGuides();
            }
            element.setX(x);
            element.setY(y);
            applyPosition(node, element);
            updateSelectionBox();
            changeListener.accept(element);
            e.consume();
        });
        node.setOnMouseReleased(e -> hideGuides());
    }

    private double[] snapPosition(double x, double y, Node node) {
        Bounds bounds = node.getLayoutBounds();
        double[] sx = new double[]{0, 0};
        double[] sy = new double[]{0, 0};
        snapAxis(x, bounds.getWidth(), sticker.getWidth(), vGuide, true, sx);
        snapAxis(y, bounds.getHeight(), sticker.getHeight(), hGuide, false, sy);
        return new double[]{sx[0], sy[0]};
    }

    private void snapAxis(double coord, double size, double max, Line guide, boolean vertical, double[] out) {
        out[1] = 0;
        double best = Double.MAX_VALUE;
        double bestCoord = coord;
        double bestTarget = Double.NaN;
        double[] targets = {max / 2, 0, max};
        for (double offset : SNAP_OFFSETS) {
            double ref = coord + offset * size;
            for (double target : targets) {
                double distance = Math.abs(ref - target);
                if (distance < SNAP_THRESHOLD && distance < best) {
                    best = distance;
                    bestCoord = target - offset * size;
                    bestTarget = target;
                }
            }
        }
        if (best < SNAP_THRESHOLD) {
            out[0] = bestCoord;
            out[1] = 1;
            placeGuide(guide, vertical, bestTarget);
        } else {
            out[0] = coord;
            guide.setVisible(false);
        }
    }

    private void placeGuide(Line guide, boolean vertical, double target) {
        guide.setVisible(true);
        if (vertical) {
            guide.setStartX(target);
            guide.setEndX(target);
            guide.setStartY(0);
            guide.setEndY(sticker.getHeight());
        } else {
            guide.setStartX(0);
            guide.setEndX(sticker.getWidth());
            guide.setStartY(target);
            guide.setEndY(target);
        }
    }

    public void hideGuides() {
        ensureGuides();
        vGuide.setVisible(false);
        hGuide.setVisible(false);
    }

    private void installResizeHandler(Rectangle handle, int[] corner) {
        final double[] start = new double[8];
        handle.setOnMousePressed(e -> {
            if (selectedElement == null) {
                return;
            }
            Node sel = nodes.get(selectedElement);
            if (sel == null) {
                return;
            }
            Bounds b = sel.getLayoutBounds();
            start[2] = b.getWidth();
            start[3] = b.getHeight();
            start[4] = selectedElement instanceof TextElement text ? text.getFontSize() : 0;
            double ax = sel.getLayoutX() + b.getMinX();
            double ay = sel.getLayoutY() + b.getMinY();
            start[5] = ax + (corner[0] < 0 ? start[2] : 0);
            start[6] = ay + (corner[1] < 0 ? start[3] : 0);
            e.consume();
        });
        handle.setOnMouseDragged(e -> {
            Point2D p = sceneToLocal(e.getSceneX(), e.getSceneY());
            resize(corner, start, p.getX(), p.getY());
        });
    }

    private void resize(int[] corner, double[] start, double x, double y) {
        if (selectedElement == null) {
            return;
        }
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

        if (selectedElement instanceof TextElement text) {
            text.setX(newX);
            text.setY(newY);
            text.setFontSize(Math.max(1, start[4] * k));
        } else if (selectedElement instanceof ImageElement image) {
            image.setX(newX);
            image.setY(newY);
            image.setWidth(origW * k);
            image.setHeight(origH * k);
        } else if (selectedElement instanceof BarcodeElement barcode) {
            barcode.setX(newX);
            barcode.setY(newY);
            barcode.setWidth(origW * k);
            barcode.setHeight(origH * k);
        }
        updateElement(selectedElement);
    }

    private void installRotationHandler(Node handle) {
        handle.setCursor(Cursor.MOVE);
        handle.setOnMousePressed(e -> {
            if (selectedElement == null) {
                return;
            }
            Node sel = nodes.get(selectedElement);
            if (sel == null) {
                return;
            }
            double[] c = centerOf(sel);
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
            double snapped = rotationSnap ? snapRotation(target) : target;
            selectedElement.setRotation(snapped);
            Node sel = nodes.get(selectedElement);
            if (sel != null) {
                sel.setRotate(selectedElement.getRotation());
            }
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

    private static double snapRotation(double degrees) {
        double nearest = Math.round(degrees / 15) * 15;
        return Math.abs(degrees - nearest) <= ROTATION_SNAP_THRESHOLD
                ? normalizeDegrees(nearest)
                : degrees;
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

    private void applyPosition(Node node, StickerElement element) {
        node.setLayoutX(element.getX());
        node.setLayoutY(element.getY());
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
        Node node = selectedElement == null ? null : nodes.get(selectedElement);
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
            int[] c = corner(i);
            Rectangle handle = handles[i];
            handle.setX(x + (c[0] > 0 ? w : 0) - half);
            handle.setY(y + (c[1] > 0 ? h : 0) - half);
            handle.setCursor(c[0] == c[1]
                    ? (c[0] > 0 ? Cursor.SE_RESIZE : Cursor.NW_RESIZE)
                    : (c[0] > 0 ? Cursor.SW_RESIZE : Cursor.NE_RESIZE));
        }
    }

    private void setHandlesVisible(boolean visible) {
        for (Rectangle handle : handles) {
            handle.setVisible(visible);
            handle.setMouseTransparent(!visible);
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

    private static Image decodeBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(data));
    }
}
