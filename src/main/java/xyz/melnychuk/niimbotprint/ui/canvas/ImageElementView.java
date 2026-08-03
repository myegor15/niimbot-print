package xyz.melnychuk.niimbotprint.ui.canvas;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class ImageElementView implements ElementView {

    private final ImageElement element;
    private final ImageView view = new ImageView();
    private String lastBase64;
    private Image image;
    private double baseW;
    private double baseH;

    public ImageElementView(ImageElement element) {
        this.element = element;
        view.setPreserveRatio(true);
        refresh();
        applyPosition();
    }

    @Override
    public StickerElement element() {
        return element;
    }

    @Override
    public Node node() {
        return view;
    }

    @Override
    public void applyPosition() {
        view.setLayoutX(element.getX());
        view.setLayoutY(element.getY());
    }

    @Override
    public void refresh() {
        view.setImage(cachedImage());
        view.setFitWidth(element.getWidth());
        view.setFitHeight(element.getHeight());
    }

    @Override
    public void beginResize() {
        baseW = element.getWidth();
        baseH = element.getHeight();
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

    private Image cachedImage() {
        String base64 = element.getImageBase64();
        if (image == null || !java.util.Objects.equals(base64, lastBase64)) {
            lastBase64 = base64;
            image = decodeBase64(base64);
        }
        return image;
    }

    private static Image decodeBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(base64);
        return new Image(new ByteArrayInputStream(data));
    }
}
