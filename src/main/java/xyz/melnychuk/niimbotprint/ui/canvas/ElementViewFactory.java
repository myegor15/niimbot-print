package xyz.melnychuk.niimbotprint.ui.canvas;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.Barcode;
import xyz.melnychuk.niimbotprint.model.Image;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.model.Text;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementViewFactory {

    public static ElementView getElementView(Element element) {
        if (element instanceof Text text) {
            return new TextView(text);
        }
        if (element instanceof Image image) {
            return new ImageView(image);
        }
        if (element instanceof Barcode barcode) {
            return new BarcodeView(barcode);
        }
        throw new AppException("Unknown element: " + element);
    }
}
