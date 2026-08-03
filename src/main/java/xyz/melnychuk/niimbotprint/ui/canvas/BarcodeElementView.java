package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.BarcodeGenerator;

@Slf4j
public class BarcodeElementView implements ElementView {

    private static final String RENDER_ERROR_TEXT = "Недопустимый штрихкод";

    private final BarcodeElement element;
    private final Group root = new Group();
    private double baseW;
    private double baseH;

    public BarcodeElementView(BarcodeElement element) {
        this.element = element;
        refresh();
        applyPosition();
    }

    @Override
    public StickerElement element() {
        return element;
    }

    @Override
    public Node node() {
        return root;
    }

    @Override
    public void applyPosition() {
        root.setLayoutX(element.getX());
        root.setLayoutY(element.getY());
    }

    @Override
    public void beginResize() {
        baseW = element.getWidth();
        baseH = element.getHeight();
    }

    @Override
    public void refresh() {
        root.getChildren().clear();
        try {
            var image = BarcodeGenerator.generate(element.getContent(), element.getFormat(),
                    (int) element.getWidth(), (int) element.getHeight());
            ImageView view = new ImageView(SwingFXUtils.toFXImage(image, null));
            view.setFitWidth(element.getWidth());
            view.setFitHeight(element.getHeight());
            view.setPreserveRatio(false);
            root.getChildren().add(view);
        } catch (Exception e) {
            log.warn("Barcode cannot be rendered for content '{}' format '{}': {}",
                    element.getContent(), element.getFormat(), e.getMessage());
            Label error = new Label(RENDER_ERROR_TEXT);
            root.getChildren().add(error);
        }
    }

    @Override
    public void resize(double scale, double newX, double newY) {
        element.setX(newX);
        element.setY(newY);
        element.setWidth(baseW * scale);
        element.setHeight(baseH * scale);
        refresh();
        applyPosition();
    }
}
