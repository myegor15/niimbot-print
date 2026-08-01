package xyz.melnychuk.niimprint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class NiimPrintApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = getScene();
        stage.setTitle("Niim Print");
        stage.setScene(scene);
        stage.show();
    }

    private Scene getScene() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(NiimPrintApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 700);

        String style = Objects.requireNonNull(NiimPrintApplication.class.getResource("style.css")).toExternalForm();
        scene.getStylesheets().add(style);

        return scene;
    }
}
