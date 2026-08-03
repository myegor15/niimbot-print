package xyz.melnychuk.niimbotprint.ui.editor;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.ui.canvas.StickerCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class CanvasStickerEditor implements StickerEditor {

    @NonNull
    private final StickerCanvas stickerCanvas;

    @Override
    public StickerElement addElement(StickerElement element) {
        return stickerCanvas.addElement(element);
    }

    @Override
    public void removeSelected() {
        stickerCanvas.removeSelected();
    }

    @Override
    public StickerElement getSelectedElement() {
        return stickerCanvas.getSelectedElement();
    }

    @Override
    public void updateElement(StickerElement element) {
        stickerCanvas.updateElement(element);
    }

    @Override
    public void refresh() {
        stickerCanvas.refresh();
    }

    @Override
    public void setLabelSize(int width, int height) {
        stickerCanvas.setLabelSize(width, height);
    }

    @Override
    public void setGridVisible(boolean visible) {
        stickerCanvas.setGridVisible(visible);
    }

    @Override
    public boolean isGridVisible() {
        return stickerCanvas.isGridVisible();
    }

    @Override
    public void setPositionSnap(boolean enabled) {
        stickerCanvas.setPositionSnap(enabled);
    }

    @Override
    public boolean isPositionSnap() {
        return stickerCanvas.isPositionSnap();
    }

    @Override
    public void setRotationSnap(boolean enabled) {
        stickerCanvas.setRotationSnap(enabled);
    }

    @Override
    public boolean isRotationSnap() {
        return stickerCanvas.isRotationSnap();
    }

    @Override
    public String snapshotPngBase64() {
        boolean gridVisible = stickerCanvas.isGridVisible();
        stickerCanvas.setGridVisible(false);
        stickerCanvas.hideGuides();
        stickerCanvas.setSelectionVisible(false);
        try {
            WritableImage snapshot = stickerCanvas.snapshot(null, null);
            BufferedImage image = SwingFXUtils.fromFXImage(snapshot, null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new AppException(e);
        } finally {
            stickerCanvas.setSelectionVisible(true);
            stickerCanvas.setGridVisible(gridVisible);
        }
    }

    @Override
    public void setSelectionListener(Consumer<StickerElement> listener) {
        stickerCanvas.setSelectionListener(listener);
    }

    @Override
    public void setChangeListener(Consumer<StickerElement> listener) {
        stickerCanvas.setChangeListener(listener);
    }
}
