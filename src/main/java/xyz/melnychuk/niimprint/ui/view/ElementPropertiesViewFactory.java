package xyz.melnychuk.niimprint.ui.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimprint.AppException;
import xyz.melnychuk.niimprint.model.BarcodeElement;
import xyz.melnychuk.niimprint.model.ImageElement;
import xyz.melnychuk.niimprint.model.StickerElement;
import xyz.melnychuk.niimprint.model.TextElement;

import java.util.Map;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementPropertiesViewFactory {

    private static final Map<Class<?>, Supplier<ElementPropertiesView<?>>> REGISTRY = Map.of(
            TextElement.class, TextElementPropertiesView::new,
            BarcodeElement.class, BarcodeElementPropertiesView::new,
            ImageElement.class, ImageElementPropertiesView::new
    );

    public static <T extends StickerElement> ElementPropertiesView<T> create(T element) {
        Supplier<ElementPropertiesView<?>> supplier = REGISTRY.get(element.getClass());
        if (supplier == null) {
            throw new AppException("Unknown element: " + element);
        }

        @SuppressWarnings("unchecked")
        ElementPropertiesView<T> view = (ElementPropertiesView<T>) supplier.get();
        return view;
    }
}
