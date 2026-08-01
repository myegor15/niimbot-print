package xyz.melnychuk.niimprint.ui;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import xyz.melnychuk.niimprint.model.BarcodeElement;
import xyz.melnychuk.niimprint.model.ImageElement;
import xyz.melnychuk.niimprint.model.Label;
import xyz.melnychuk.niimprint.model.LabelElement;
import xyz.melnychuk.niimprint.model.TextElement;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LabelCanvas extends javafx.scene.layout.Pane {
    private Label label;
    private LabelElement selected;
    private Rectangle background;
    private Rectangle selectionBox;
    private final Map<LabelElement, Node> nodes = new HashMap<>();
    private Consumer<LabelElement> selectionListener = e -> {
    };

    public LabelCanvas(Label label) {
        this.label = label;
        setStyle("-fx-background-color: #ececec;");
        buildBackground();
        setOnMousePressed(e -> selectNone());
        refresh();
    }

    public void setLabel(Label label) {
        this.label = label;
        buildBackground();
        setLabelSize(label.getWidth(), label.getHeight());
        refresh();
    }

    public void setSelectionListener(Consumer<LabelElement> listener) {
        this.selectionListener = listener;
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
        background = new Rectangle(label.getWidth(), label.getHeight());
        background.setFill(Color.WHITE);
        getChildren().add(0, background);
    }

    public void refresh() {
        nodes.values().forEach(getChildren()::remove);
        nodes.clear();
        for (LabelElement element : label.getElements()) {
            Node node = createNode(element);
            nodes.put(element, node);
            getChildren().add(node);
            makeDraggable(node, element);
        }
        updateSelectionBox();
    }

    public LabelElement addElement(LabelElement element) {
        label.getElements().add(element);
        Node node = createNode(element);
        nodes.put(element, node);
        getChildren().add(node);
        makeDraggable(node, element);
        select(element);
        return element;
    }

    public void updateElement(LabelElement element) {
        Node old = nodes.get(element);
        if (old == null) {
            return;
        }
        getChildren().remove(old);
        Node node = createNode(element);
        nodes.put(element, node);
        getChildren().add(node);
        makeDraggable(node, element);
        if (selected == element) {
            updateSelectionBox();
        }
    }

    public void removeSelected() {
        if (selected == null) {
            return;
        }
        label.getElements().remove(selected);
        getChildren().remove(nodes.remove(selected));
        selectNone();
    }

    public LabelElement getSelected() {
        return selected;
    }

    public void setSelectionVisible(boolean visible) {
        if (selectionBox != null) {
            selectionBox.setVisible(visible);
        }
    }

    public void select(LabelElement element) {
        selected = element;
        updateSelectionBox();
        selectionListener.accept(element);
    }

    public void selectNone() {
        if (selected == null) {
            return;
        }
        selected = null;
        updateSelectionBox();
        selectionListener.accept(null);
    }

    private Node createNode(LabelElement element) {
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

    private Node createText(TextElement element) {
        Font font = font(element);
        javafx.scene.control.Label text = new javafx.scene.control.Label(element.getText());
        text.setFont(font);
        text.setPadding(javafx.geometry.Insets.EMPTY);
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
            javafx.scene.control.Label error = new javafx.scene.control.Label("Ошибка штрихкода: " + element.getContent());
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

    private void makeDraggable(Node node, LabelElement element) {
        double[] start = new double[2];
        node.setOnMousePressed(e -> {
            select(element);
            start[0] = e.getSceneX() - element.getX();
            start[1] = e.getSceneY() - element.getY();
            e.consume();
        });
        node.setOnMouseDragged(e -> {
            element.setX(clamp(e.getSceneX() - start[0], 0, label.getWidth()));
            element.setY(clamp(e.getSceneY() - start[1], 0, label.getHeight()));
            applyPosition(node, element);
            updateSelectionBox();
            e.consume();
        });
    }

    private void applyPosition(Node node, LabelElement element) {
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
            getChildren().add(selectionBox);
        }
        Node node = selected == null ? null : nodes.get(selected);
        if (node == null) {
            selectionBox.setVisible(false);
            return;
        }
        Bounds bounds = node.getLayoutBounds();
        if (bounds.getWidth() == 0 && bounds.getHeight() == 0) {
            Platform.runLater(this::updateSelectionBox);
            return;
        }
        selectionBox.setVisible(true);
        selectionBox.setX(node.getLayoutX() + bounds.getMinX());
        selectionBox.setY(node.getLayoutY() + bounds.getMinY());
        selectionBox.setWidth(bounds.getWidth());
        selectionBox.setHeight(bounds.getHeight());
    }

    private static Image decodeBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(data));
    }
}
