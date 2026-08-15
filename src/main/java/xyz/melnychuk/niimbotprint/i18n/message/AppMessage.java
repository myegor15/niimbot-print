package xyz.melnychuk.niimbotprint.i18n.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.melnychuk.niimbotprint.i18n.Message;

@Getter
@RequiredArgsConstructor
public enum AppMessage implements Message {

    APP_TITLE("app.title"),
    SPLASH_STARTING("splash.starting"),
    STATUS_READY("status.ready"),
    ERROR_SYSTEM("error.system"),
    ERROR_SERVER_START("error.serverStart"),
    ERROR_SPLASH_OPEN("error.splashOpen"),
    ERROR_MAIN_OPEN("error.mainOpen");

    private final String key;
}
