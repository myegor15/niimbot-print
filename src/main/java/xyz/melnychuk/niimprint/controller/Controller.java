package xyz.melnychuk.niimprint.controller;

import xyz.melnychuk.niimprint.util.AsyncUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Controller {

    public <T> void run(Supplier<T> action,
                        Consumer<T> onSuccess,
                        Consumer<Throwable> onError) {
        AsyncUtils.run(action, onSuccess, onError);
    }
}
