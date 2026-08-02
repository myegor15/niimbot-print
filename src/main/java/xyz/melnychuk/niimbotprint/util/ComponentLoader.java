package xyz.melnychuk.niimbotprint.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.App;
import xyz.melnychuk.niimbotprint.AppException;

import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComponentLoader {

    public record Bundle<T>(T controller, Node root) {
    }

    public static <T> Bundle<T> load(Class<T> controllerClass) {
        Component component = controllerClass.getAnnotation(Component.class);
        if (component == null) {
            throw new AppException("No @Component annotation on " + controllerClass.getName());
        }
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(component.fxml()));
            Node root = loader.load();
            return new Bundle<>(loader.getController(), root);
        } catch (IOException e) {
            throw new AppException("Failed to load component " + component.fxml(), e);
        }
    }
}