package xyz.melnychuk.niimbotprint.ui.canvas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementViewFactory {

    public static ElementView getElementView(StickerElement element) {
        if (element instanceof TextElement text) {
            return new TextElementView(text);
        }
        if (element instanceof ImageElement image) {
            return new ImageElementView(image);
        }
        if (element instanceof BarcodeElement barcode) {
            return new BarcodeElementView(barcode);
        }
        throw new AppException("Unknown element: " + element);
    }
}
