package xyz.melnychuk.niimbotprint.ui;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
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

    private Sticker sticker;

    @Getter
    private StickerElement selectedElement;

    private Rectangle background;

    private Rectangle selectionBox;

    private final Rectangle[] handles = new Rectangle[4];

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
        refresh();
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
        buildBackground();
        setLabelSize(sticker.getWidth(), sticker.getHeight());
        refresh();
    }

    public void setLabelSize(int width, int height) {
        setPrefSize(width, height);
        setMinSize(width, height);
        setMaxSize(width, height);
        background.setWidth(width);
        background.setHeight(height);
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

    private void buildHandles() {
        for (int i = 0; i < handles.length; i++) {
            Rectangle handle = new Rectangle(HANDLE_SIZE, HANDLE_SIZE);
            handle.setFill(Color.WHITE);
            handle.setStroke(Color.BLUE);
            handle.setVisible(false);
            installResizeHandler(handle, corner(i));
            handles[i] = handle;
            getChildren().add(handle);
        }
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
        int index = Math.max(1, getChildren().size() - handles.length);
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
        if (element instanceof TextElement text) {
            return createText(text);
        }
        if (element instanceof ImageElement image) {
            return createImage(image);
        }
        if (element instanceof BarcodeElement barcode) {
            return createBarcode(barcode);
        }
        throw new IllegalArgumentException("Unknown element: " + element);
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
            nodes.put(element, newNode);
            insertBeforeHandles(newNode);
            makeDraggable(newNode, element);
        }
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
            element.setX(clamp(p.getX() - start[0], 0, sticker.getWidth()));
            element.setY(clamp(p.getY() - start[1], 0, sticker.getHeight()));
            applyPosition(node, element);
            updateSelectionBox();
            changeListener.accept(element);
            e.consume();
        });
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
        Bounds bounds = node.getLayoutBounds();
        if (bounds.getWidth() == 0 && bounds.getHeight() == 0) {
            Platform.runLater(this::updateSelectionBox);
            return;
        }
        double x = node.getLayoutX() + bounds.getMinX();
        double y = node.getLayoutY() + bounds.getMinY();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        selectionBox.setVisible(true);
        selectionBox.setX(x);
        selectionBox.setY(y);
        selectionBox.setWidth(w);
        selectionBox.setHeight(h);
        updateHandles(x, y, w, h);
        setHandlesVisible(true);
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
    }

    private static Image decodeBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(data));
    }
}
