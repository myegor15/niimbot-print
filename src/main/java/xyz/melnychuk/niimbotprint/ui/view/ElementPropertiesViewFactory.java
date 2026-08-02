package xyz.melnychuk.niimbotprint.ui.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;

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
