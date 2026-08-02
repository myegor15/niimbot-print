package xyz.melnychuk.niimbotprint.ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.StickerElement;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;

public class StickerCanvasEditor implements StickerEditor {

    private final StickerCanvas canvas;

    public StickerCanvasEditor(StickerCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public StickerElement addElement(StickerElement element) {
        return canvas.addElement(element);
    }

    @Override
    public void removeSelected() {
        canvas.removeSelected();
    }

    @Override
    public StickerElement getSelectedElement() {
        return canvas.getSelectedElement();
    }

    @Override
    public void updateElement(StickerElement element) {
        canvas.updateElement(element);
    }

    @Override
    public void refresh() {
        canvas.refresh();
    }

    @Override
    public void setLabelSize(int width, int height) {
        canvas.setLabelSize(width, height);
    }

    @Override
    public String snapshotPngBase64() {
        canvas.setSelectionVisible(false);
        try {
            WritableImage snapshot = canvas.snapshot(null, null);
            BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new AppException(e);
        } finally {
            canvas.setSelectionVisible(true);
        }
    }

    @Override
    public void setSelectionListener(Consumer<StickerElement> listener) {
        canvas.setSelectionListener(listener);
    }

    @Override
    public void setChangeListener(Consumer<StickerElement> listener) {
        canvas.setChangeListener(listener);
    }
}
