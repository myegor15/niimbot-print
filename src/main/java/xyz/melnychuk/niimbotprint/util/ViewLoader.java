package xyz.melnychuk.niimbotprint.util;

import javafx.fxml.FXMLLoader;
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
public final class ViewLoader {

    public record Bundle<T>(T controller, Scene scene) {
    }

    public static <T> Bundle<T> load(Class<T> controllerClass, Stage stage) {
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

    private static URL getResource(String name) {
        return App.class.getResource(name);
    }
}
