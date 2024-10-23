package nu.ndw.nls.accessibilitymap.jobs.test.component.core.process;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class InputStreamConsumer implements Runnable {

    private final InputStream inputStream;

    private final Consumer<String> consumer;

    public InputStreamConsumer(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
    }
}