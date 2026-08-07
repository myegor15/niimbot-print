module xyz.melnychuk.niimbotprint {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;

    requires java.desktop;
    requires java.logging;
    requires java.net.http;

    requires com.fasterxml.jackson.databind;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;

    requires static lombok;

    exports xyz.melnychuk.niimbotprint;

    opens xyz.melnychuk.niimbotprint.controller to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.controller.view to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.controller.component to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.controller.component.elementproperties to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.ui to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.ui.canvas to javafx.fxml;
    opens xyz.melnychuk.niimbotprint.ui.canvas.element to javafx.fxml;

    opens xyz.melnychuk.niimbotprint.model to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue.request to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimblue.response to com.fasterxml.jackson.databind;
    opens xyz.melnychuk.niimbotprint.dto to com.fasterxml.jackson.databind;

}
