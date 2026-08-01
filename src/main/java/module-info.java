module xyz.melnychuk.niimprint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires java.net.http;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    requires com.fasterxml.jackson.databind;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires static lombok;

    opens xyz.melnychuk.niimprint to javafx.fxml;
    exports xyz.melnychuk.niimprint;

    opens xyz.melnychuk.niimprint.model to com.fasterxml.jackson.databind;
}
