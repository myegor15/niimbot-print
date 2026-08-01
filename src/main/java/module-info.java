module xyz.melnychuk.niimprint {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens xyz.melnychuk.niimprint to javafx.fxml;
    exports xyz.melnychuk.niimprint;
}