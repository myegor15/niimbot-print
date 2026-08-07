package xyz.melnychuk.niimbotprint.ui.canvas.element;

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
        return switch (element) {
            case Text text -> new TextView(text);
            case Image image -> new ImageView(image);
            case Barcode barcode -> new BarcodeView(barcode);
            case Element other -> throw new AppException("Unknown element: " + other);
        };
    }
}
