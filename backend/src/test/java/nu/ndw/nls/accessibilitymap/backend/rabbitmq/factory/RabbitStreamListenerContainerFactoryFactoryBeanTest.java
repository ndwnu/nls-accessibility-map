package nu.ndw.nls.accessibilitymap.backend.rabbitmq.factory;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import com.rabbitmq.stream.Message;
import com.rabbitmq.stream.Properties;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.properties.StreamQueueProperties;
import nu.ndw.nls.springboot.test.logging.LoggerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.MethodInvocationRetryListenerSupport;
import org.springframework.retry.support.Args;

@ExtendWith(MockitoExtension.class)
class RabbitStreamListenerContainerFactoryFactoryBeanTest {

    @RegisterExtension
    LoggerExtension loggerExtension = new LoggerExtension();

    @Mock
    private StreamQueueProperties streamQueueConfig;

    @Mock
    private RetryContext retryContext;

    @Mock
    private RetryCallback<Object, Throwable> retryCallback;

    @Mock
    private Message message;

    private RabbitStreamListenerContainerFactoryFactoryBean factoryBean;

    @BeforeEach
    void setup() {
        factoryBean = new RabbitStreamListenerContainerFactoryFactoryBean(streamQueueConfig, null);
    }

    @Test
    void onError_messageToSkipInProperties() {
        Properties messageProperties = mock(Properties.class);
        when(message.getProperties()).thenReturn(messageProperties);
        when(messageProperties.getMessageId()).thenReturn("messageId");

        Args args = new Args(new Object[]{message});
        when(retryContext.getAttribute(anyString())).thenReturn(args);
        when(streamQueueConfig.isMessageToSkip(anyString())).thenReturn(true);

        MethodInvocationRetryListenerSupport listener = factoryBean.skippableMessageRetryListener(streamQueueConfig);

        listener.onError(retryContext, retryCallback, new Exception("Test exception"));

        verify(retryContext).setExhaustedOnly();
        verify(messageProperties, times(3)).getMessageId();
        loggerExtension.containsLog(Level.WARN, "Failed to process stream message: 'messageId' for the 0 time");
        loggerExtension.containsLog(Level.INFO, "Skip stream message with messageId: 'messageId'");
    }

    @Test
    void onError_noMessageToSkip() {
        Properties messageProperties = mock(Properties.class);
        when(message.getProperties()).thenReturn(messageProperties);
        when(messageProperties.getMessageId()).thenReturn("messageId");
        Args args = new Args(new Object[]{message});
        when(retryContext.getAttribute(anyString())).thenReturn(args);
        when(streamQueueConfig.isMessageToSkip(anyString())).thenReturn(false);

        MethodInvocationRetryListenerSupport listener = factoryBean.skippableMessageRetryListener(streamQueueConfig);

        listener.onError(retryContext, retryCallback, new Exception("Test exception"));

        verify(retryContext, never()).setExhaustedOnly();
        verify(messageProperties, times(2)).getMessageId();
        loggerExtension.containsLog(Level.WARN, "Failed to process stream message: 'messageId' for the 0 time");
    }

    @Test
    void onError_noMessage() {
        when(streamQueueConfig.isMessageToSkip(isNull())).thenReturn(true);
        when(retryContext.getRetryCount()).thenReturn(1);
        when(retryContext.getLastThrowable()).thenReturn(new Exception("Test exception"));

        MethodInvocationRetryListenerSupport listener = factoryBean.skippableMessageRetryListener(streamQueueConfig);

        listener.onError(retryContext, retryCallback, new Exception("Test exception"));

        verify(retryContext).setExhaustedOnly();
        loggerExtension.containsLog(Level.WARN, "No message provided in message listener retry handler with context: retryContext");
        loggerExtension.containsLog(Level.WARN, "Failed to process stream message: 'null' for the 1 time");
        loggerExtension.containsLog(Level.INFO, "Skip stream message with messageId: 'null'");
    }

    @Test
    void onError_invalidArgs() {
        when(streamQueueConfig.isMessageToSkip(isNull())).thenReturn(true);
        when(retryContext.getAttribute(anyString())).thenReturn(null);

        when(retryContext.getAttribute(anyString())).thenReturn(new Object[]{message});
        MethodInvocationRetryListenerSupport listener = factoryBean.skippableMessageRetryListener(streamQueueConfig);

        listener.onError(retryContext, retryCallback, new Exception("Test exception"));

        verify(retryContext).setExhaustedOnly();
        loggerExtension.containsLog(Level.WARN, "No message provided in message listener retry handler with context: retryContext");
        loggerExtension.containsLog(Level.WARN, "Failed to process stream message: 'null' for the 0 time");
        loggerExtension.containsLog(Level.INFO, "Skip stream message with messageId: 'null'");
    }
}
