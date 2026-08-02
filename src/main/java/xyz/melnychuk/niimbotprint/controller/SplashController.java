package xyz.melnychuk.niimbotprint.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

public class SplashController extends Controller {

    @FXML
    private Label messageLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    public void setMessage(String message) {
        messageLabel.setText(message);
    }
}
