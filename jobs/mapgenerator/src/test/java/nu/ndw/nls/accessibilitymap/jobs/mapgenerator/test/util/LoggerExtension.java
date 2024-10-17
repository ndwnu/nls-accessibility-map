package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

public class LoggerExtension implements BeforeEachCallback, AfterEachCallback {

    private SynchronizedListAppender<ILoggingEvent> synchronizedListAppender = new SynchronizedListAppender<>();

    private Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Override
    public void beforeEach(ExtensionContext extensionContext) {

        ((LoggerContext) LoggerFactory.getILoggerFactory()).reset();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(synchronizedListAppender);
        synchronizedListAppender.start();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {

        synchronizedListAppender.stop();
        synchronizedListAppender.clear();
        logger.detachAppender(synchronizedListAppender);
    }

    public void isEmpty() {

        assertThat(synchronizedListAppender.list).isEmpty();
    }

    public void containsLog(Level level, String message) {

        containsLog(level, message, null, 1);
    }

    public void containsLog(Level level, String message, String causeMessage) {

        containsLog(level, message, causeMessage, 1);
    }

    public void containsLog(Level level, String message, int times) {

        containsLog(level, message, null, times);
    }

    public void containsLog(Level level, String message, String causeMessage, int times) {

        var anyMatchingTotal = synchronizedListAppender.stream()
                .filter(logEvent -> level.equals(logEvent.getLevel()))
                .filter(logEvent -> message.equals(logEvent.getFormattedMessage()))
                .filter(logEvent -> Objects.isNull(causeMessage) || causeMessage.equals(
                        logEvent.getThrowableProxy().getMessage()))
                .count();

        if (anyMatchingTotal != times) {
            List<String> messages = synchronizedListAppender.stream()
                    .map(logEvent -> String.format("%n Level: '%s', \tMessage: '%s', \tCause: '%s'",
                            logEvent.getLevel(),
                            logEvent.getFormattedMessage(),
                            Objects.isNull(logEvent.getThrowableProxy())
                                    ? null
                                    : logEvent.getThrowableProxy().getMessage()))
                    .toList();

            if (times == 1) {
                fail(String.format(
                        "No event found matching level: '%s', message: '%s', cause: '%s' exists exactly one time. %nMessages that do exists: %s",
                        level, message, causeMessage, messages));
            } else {
                fail(String.format(
                        "No event found matching level: '%s' and message: '%s', cause: '%s' exists for %s times. %nMessages that do exists: %s",
                        level, message, causeMessage, times, messages));
            }
        }
    }
}
