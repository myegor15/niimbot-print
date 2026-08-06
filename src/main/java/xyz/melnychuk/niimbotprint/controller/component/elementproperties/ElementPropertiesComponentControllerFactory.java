package xyz.melnychuk.niimbotprint.controller.component.elementproperties;

import javafx.scene.Node;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.AppException;
import xyz.melnychuk.niimbotprint.model.Barcode;
import xyz.melnychuk.niimbotprint.model.Image;
import xyz.melnychuk.niimbotprint.model.Element;
import xyz.melnychuk.niimbotprint.model.Text;
import xyz.melnychuk.niimbotprint.util.FxmlLoader;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElementPropertiesComponentControllerFactory {

    private static final Map<Class<? extends Element>, Class<? extends ElementPropertiesComponentController<?>>> REGISTRY =
            Map.of(
                    Text.class, TextPropertiesComponentController.class,
                    Barcode.class, BarcodePropertiesComponentController.class,
                    Image.class, ImagePropertiesComponentController.class
            );

    public static FxmlLoader.Bundle<? extends ElementPropertiesComponentController<?>, Node> getController(Element element) {
        Class<? extends ElementPropertiesComponentController<?>> type = REGISTRY.get(element.getClass());
        if (type == null) {
            throw new AppException("Unknown element: " + element);
        }
        return FxmlLoader.loadComponent(type);
    }
}
