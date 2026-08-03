package xyz.melnychuk.niimbotprint.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import xyz.melnychuk.niimbotprint.App;
import xyz.melnychuk.niimbotprint.AppException;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FxmlLoader {

    public record Bundle<T, V>(T controller, V node) {
    }

    public static <T> Bundle<T, Scene> loadView(Class<T> controllerClass, Stage stage) {
        View info = controllerClass.getAnnotation(View.class);
        if (info == null) {
            throw new AppException("No @View annotation on " + controllerClass.getName());
        }

        try {
            FXMLLoader loader = new FXMLLoader(getResource(info.fxml()));
            Scene scene = new Scene(loader.load(), info.width(), info.height());

            for (String stylesheet : info.stylesheets()) {
                scene.getStylesheets().add(Objects.requireNonNull(getResource(stylesheet)).toExternalForm());
            }
            if (!info.title().isEmpty()) {
                stage.setTitle(info.title());
            }

            return new Bundle<>(loader.getController(), scene);
        } catch (IOException e) {
            throw new AppException("Failed to load view " + info.fxml(), e);
        }
    }

    public static <T> Bundle<T, Node> loadComponent(Class<T> controllerClass) {
        Component component = controllerClass.getAnnotation(Component.class);
        if (component == null) {
            throw new AppException("No @Component annotation on " + controllerClass.getName());
        }

        try {
            FXMLLoader loader = new FXMLLoader(getResource(component.fxml()));
            Node node = loader.load();
            return new Bundle<>(loader.getController(), node);
        } catch (IOException e) {
            throw new AppException("Failed to load component " + component.fxml(), e);
        }
    }

    private static URL getResource(String name) {
        return App.class.getResource(name);
    }
}
