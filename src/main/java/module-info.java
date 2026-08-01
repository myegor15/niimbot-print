module xyz.melnychuk.niimprint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires java.desktop;
    requires java.logging;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires static lombok;

    opens xyz.melnychuk.niimprint to javafx.fxml;
    exports xyz.melnychuk.niimprint;

    opens xyz.melnychuk.niimprint.model to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue.request to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue.response to com.fasterxml.jackson.databind;
}
