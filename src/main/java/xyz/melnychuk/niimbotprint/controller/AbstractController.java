package xyz.melnychuk.niimbotprint.controller;

import xyz.melnychuk.niimbotprint.util.AsyncUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractController {

    private Consumer<String> messageHandler = text -> {};
    private Consumer<Throwable> errorHandler = t -> {};

    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    public void setErrorHandler(Consumer<Throwable> handler) {
        this.errorHandler = handler;
    }

    protected void message(String text) {
        messageHandler.accept(text);
    }

    protected void error(Throwable t) {
        errorHandler.accept(t);
    }

    protected <T> void run(Supplier<T> action,
                           Consumer<T> onSuccess,
                           Consumer<Throwable> onError) {
        AsyncUtils.run(action, onSuccess, onError);
    }
}
