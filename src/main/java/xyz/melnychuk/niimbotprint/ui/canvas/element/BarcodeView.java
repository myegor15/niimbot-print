package xyz.melnychuk.niimbotprint.ui.canvas.element;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import xyz.melnychuk.niimbotprint.i18n.message.EditorMessage;
import xyz.melnychuk.niimbotprint.i18n.I18n;
import xyz.melnychuk.niimbotprint.model.Barcode;
import xyz.melnychuk.niimbotprint.ui.BarcodeGenerator;
import xyz.melnychuk.niimbotprint.ui.canvas.ResizeHandle;

import java.util.List;

@Slf4j
public class BarcodeView extends AbstractElementView<Barcode> {

    private static final double VALUE_FONT_SIZE = 18;

    private final Group root = new Group();

    public BarcodeView(Barcode element) {
        super(element);
        refreshNode();
    }

    @Override
    public Node node() {
        return root;
    }

    @Override
    public void applyPosition() {
        root.setLayoutX(element().getX());
        root.setLayoutY(element().getY());
    }

    @Override
    public List<ResizeHandle> resizeHandles() {
        return ResizeHandle.cornersAndEdges();
    }

    @Override
    public void refresh() {
        root.getChildren().clear();
        int width = element().getWidth();
        int height = element().getHeight();
        try {
            Text value = new Text(element().getContent());
            value.setFont(Font.font("Arial", VALUE_FONT_SIZE));
            var valueBounds = value.getLayoutBounds();
            double labelH = element().isShowValue() ? valueBounds.getHeight() : 0;
            int barH = Math.max(1, height - (int) Math.round(labelH));

            var image = BarcodeGenerator.generate(element().getContent(), element().getFormat(), width, barH);
            ImageView view = new ImageView(SwingFXUtils.toFXImage(image, null));
            view.setFitWidth(width);
            view.setFitHeight(barH);
            view.setPreserveRatio(false);
            root.getChildren().add(view);

            if (element().isShowValue()) {
                if (valueBounds.getWidth() > width) {
                    value.setScaleX(width / valueBounds.getWidth());
                }
                double scaledWidth = valueBounds.getWidth() * value.getScaleX();
                value.setX(Math.max(0, (width - scaledWidth) / 2));
                double top = barH + (labelH - valueBounds.getHeight()) / 2;
                value.setY(top - valueBounds.getMinY());
                root.getChildren().add(value);
            }
        } catch (Exception e) {
            log.warn("Barcode cannot be rendered for content '{}' format '{}': {}",
                    element().getContent(), element().getFormat(), e.getMessage());
            root.getChildren().add(new Text(I18n.get(EditorMessage.BARCODE_INVALID)));
        }
    }

    @Override
    protected void applySize(double newWidth, double newHeight, double newX, double newY) {
        element().setX(newX);
        element().setY(newY);
        element().setWidth(Math.max(1, (int) Math.round(newWidth)));
        element().setHeight(Math.max(1, (int) Math.round(newHeight)));
        refreshNode();
    }
}
