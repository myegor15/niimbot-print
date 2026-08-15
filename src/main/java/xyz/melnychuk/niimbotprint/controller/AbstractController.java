package xyz.melnychuk.niimbotprint.controller;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import xyz.melnychuk.niimbotprint.AppContext;
import xyz.melnychuk.niimbotprint.AppUiException;
import xyz.melnychuk.niimbotprint.i18n.message.AppMessage;
import xyz.melnychuk.niimbotprint.i18n.I18n;
import xyz.melnychuk.niimbotprint.util.AsyncUtils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractController {

    @Getter
    private AppContext appContext;

    private boolean bound;

    @Setter
    @NonNull
    private Consumer<String> messageHandler = text -> {};

    @Setter
    @NonNull
    private Consumer<Throwable> errorHandler = t -> {};

    public void setAppContext(AppContext appContext) {
        this.appContext = Objects.requireNonNull(appContext);
        if (bound) {
            return;
        }
        bound = true;
        bind(appContext);
    }

    public void dispose() {
    }

    protected void bind(AppContext appContext) {
    }

    protected void message(String text) {
        messageHandler.accept(text);
    }

    protected void error(Throwable t) {
        errorHandler.accept(t);
    }

    protected void run(Runnable action,
                       Runnable onSuccess,
                       Consumer<Throwable> onError) {
        AsyncUtils.run(action, onSuccess, onError);
    }

    protected <T> void run(Supplier<T> action,
                           Consumer<T> onSuccess,
                           Consumer<Throwable> onError) {
        AsyncUtils.run(action, onSuccess, onError);
    }

    protected String getErrorMessage(Throwable error) {
        return switch (error) {
            case AppUiException readable -> I18n.get(readable.getKey(), readable.getArgs());
            case null, default -> I18n.get(AppMessage.ERROR_SYSTEM);
        };
    }
}
