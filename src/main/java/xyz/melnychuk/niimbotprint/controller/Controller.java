package xyz.melnychuk.niimbotprint.controller;

import xyz.melnychuk.niimbotprint.util.AsyncUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Controller {

    public <T> void run(Supplier<T> action,
                        Consumer<T> onSuccess,
                        Consumer<Throwable> onError) {
        AsyncUtils.run(action, onSuccess, onError);
    }
}
