package xyz.melnychuk.niimprint.controller;

import javafx.concurrent.Task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Controller {

    public <T> void run(Supplier<T> action,
                        Consumer<T> onSuccess,
                        Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.get();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onError.accept(task.getException()));
        new Thread(task).start();
    }
}
