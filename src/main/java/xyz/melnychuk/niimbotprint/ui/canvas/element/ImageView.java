package xyz.melnychuk.niimbotprint.ui.canvas.element;

import javafx.scene.Node;
import xyz.melnychuk.niimbotprint.model.Image;
import xyz.melnychuk.niimbotprint.ui.ImageDecoder;

import java.util.Objects;

public class ImageView extends AbstractElementView<Image> {

    private final javafx.scene.image.ImageView view = new javafx.scene.image.ImageView();
    private String lastBase64;
    private javafx.scene.image.Image image;

    public ImageView(Image element) {
        super(element);
        view.setPreserveRatio(true);
        refreshNode();
    }

    @Override
    public Node node() {
        return view;
    }

    @Override
    public void applyPosition() {
        view.setLayoutX(element().getX());
        view.setLayoutY(element().getY());
    }

    @Override
    public void refresh() {
        view.setImage(cachedImage());
        view.setFitWidth(element().getWidth());
        view.setFitHeight(element().getHeight());
    }

    @Override
    protected void applySize(double newWidth, double newHeight, double newX, double newY) {
        element().setX(newX);
        element().setY(newY);
        element().setWidth((int) Math.round(newWidth));
        element().setHeight((int) Math.round(newHeight));
        refreshNode();
    }

    private javafx.scene.image.Image cachedImage() {
        String base64 = element().getImageBase64();
        if (image == null || !Objects.equals(base64, lastBase64)) {
            lastBase64 = base64;
            image = ImageDecoder.decodeBase64(base64);
        }
        return image;
    }
}
