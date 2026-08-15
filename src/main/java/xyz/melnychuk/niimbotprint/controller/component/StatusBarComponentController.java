package xyz.melnychuk.niimbotprint.controller.component;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import org.kordamp.ikonli.javafx.FontIcon;
import xyz.melnychuk.niimbotprint.AppContext;
import xyz.melnychuk.niimbotprint.controller.AbstractController;
import xyz.melnychuk.niimbotprint.i18n.I18n;
import xyz.melnychuk.niimbotprint.i18n.Language;
import xyz.melnychuk.niimbotprint.i18n.message.AppMessage;
import xyz.melnychuk.niimbotprint.i18n.message.PrinterMessage;

public class StatusBarComponentController extends AbstractController {

    @FXML
    private Label apiLabel;
    @FXML
    private Label connectionLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private Button languageButton;

    private ContextMenu languageMenu;

    @FXML
    private void initialize() {
        Language currentLanguage = I18n.getLanguage();
        languageButton.setText(currentLanguage.getCode());
        languageMenu = new ContextMenu();
        for (Language language : Language.values()) {
            MenuItem item = new MenuItem(language.getDisplayName());
            boolean selected = language == currentLanguage;
            if (selected) {
                item.setGraphic(new FontIcon("mdi2c-check"));
            }
            item.setOnAction(e -> {
                if (!selected) {
                    I18n.setLanguage(language);
                }
            });
            languageMenu.getItems().add(item);
        }
    }

    @FXML
    private void onLanguagePressed() {
        if (languageMenu.isShowing()) {
            languageMenu.hide();
        } else {
            languageMenu.show(languageButton, Side.BOTTOM, 0, 0);
        }
    }

    @Override
    protected void bind(AppContext appContext) {
        apiLabel.setText(appContext.getPrinterService().getApiUrl());
        setMessage(I18n.get(AppMessage.STATUS_READY));
    }

    public void setConnected(boolean connected) {
        connectionLabel.setText(I18n.get(connected ? PrinterMessage.STATUS_CONNECTED : PrinterMessage.STATUS_DISCONNECTED));
        connectionLabel.getStyleClass().setAll(connected ? "status-online" : "status-offline");
    }

    public void setMessage(String text) {
        messageLabel.setText(text);
    }
}
