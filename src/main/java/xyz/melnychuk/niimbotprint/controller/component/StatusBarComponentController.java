package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import xyz.melnychuk.niimbotprint.controller.AbstractController;

public class StatusBarComponentController extends AbstractController {

    @FXML
    private Label apiLabel;
    @FXML
    private Label connectionLabel;
    @FXML
    private Label messageLabel;

    public void setApiUrl(String url) {
        apiLabel.setText(url);
    }

    public void setConnected(boolean connected) {
        connectionLabel.setText(connected ? "Подключено" : "Не подключено");
        connectionLabel.setStyle(connected ? "-fx-text-fill: green;" : "-fx-text-fill: gray;");
    }

    public void setMessage(String text) {
        messageLabel.setText(text);
    }
}
