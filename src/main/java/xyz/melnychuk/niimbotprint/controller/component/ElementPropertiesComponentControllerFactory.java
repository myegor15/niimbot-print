package xyz.melnychuk.niimbotprint.controller.component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.BarcodeElement;
import xyz.melnychuk.niimbotprint.model.ImageElement;
import xyz.melnychuk.niimbotprint.model.StickerElement;
import xyz.melnychuk.niimbotprint.model.TextElement;
import xyz.melnychuk.niimbotprint.util.ComponentLoader;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementPropertiesComponentControllerFactory {

    private static final Map<Class<? extends StickerElement>, Class<? extends ElementPropertiesComponentController<?>>> REGISTRY =
            Map.of(
                    TextElement.class, TextPropertiesComponentController.class,
                    BarcodeElement.class, BarcodePropertiesComponentController.class,
                    ImageElement.class, ImagePropertiesComponentController.class
            );

    public static ComponentLoader.Bundle<? extends ElementPropertiesComponentController<?>> getController(StickerElement element) {
        Class<? extends ElementPropertiesComponentController<?>> type = REGISTRY.get(element.getClass());
        if (type == null) {
            throw new AppException("Unknown element: " + element);
        }
        return ComponentLoader.load(type);
    }
}
