package xyz.melnychuk.niimbotprint;

import lombok.Getter;
import xyz.melnychuk.niimbotprint.i18n.Message;

@Getter
public class AppReadableException extends AppException {

    private final Message key;
    private final Object[] args;

    public AppReadableException(Message key, Object... args) {
        super(key.getKey());
        this.key = key;
        this.args = args;
    }

    public AppReadableException(Message key, Object[] args, Throwable cause) {
        super(cause);
        this.key = key;
        this.args = args;
    }
}
