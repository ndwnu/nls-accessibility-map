package nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.ControllableMessageListener;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.MessagingActuator;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.dtos.MessageCounts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessagingActuatorTest {

    private static final String MESSAGE_LISTENER_ID_1 = "listener_1";

    private static final String MESSAGE_LISTENER_ID_2 = "listener_2";

    @Mock
    private ControllableMessageListener controllableMessageListener1;

    @Mock
    private ControllableMessageListener controllableMessageListener2;

    private MessagingActuator messagingActuator;

    @BeforeEach
    void setUp() {
        messagingActuator = new MessagingActuator(List.of(controllableMessageListener1, controllableMessageListener2));
    }

    @Test
    void getMessageCounts_ok() {
        when(controllableMessageListener1.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_1);
        when(controllableMessageListener1.getMessagesProcessed()).thenReturn(1);
        when(controllableMessageListener1.getMessagesRejected()).thenReturn(2);

        MessageCounts messagingStatus = messagingActuator.getMessageCounts(MESSAGE_LISTENER_ID_1);

        assertThat(messagingStatus.messagesProcessed()).isEqualTo(1);
        assertThat(messagingStatus.messagesRejected()).isEqualTo(2);
    }

    @Test
    void resetCounters_ok() {
        when(controllableMessageListener1.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_1);
        when(controllableMessageListener1.getMessagesProcessed()).thenReturn(1);
        when(controllableMessageListener1.getMessagesRejected()).thenReturn(2);

        MessageCounts messagingStatus = messagingActuator.resetCounters(MESSAGE_LISTENER_ID_1);

        verify(controllableMessageListener1).resetCounters();

        assertThat(messagingStatus.messagesProcessed()).isEqualTo(1);
        assertThat(messagingStatus.messagesRejected()).isEqualTo(2);
    }

    @Test
    void resetCounters_ok_otherListenerId() {
        when(controllableMessageListener1.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_1);
        when(controllableMessageListener2.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_2);

        messagingActuator.resetCounters(MESSAGE_LISTENER_ID_2);

        verify(controllableMessageListener1, never()).resetCounters();
    }

    @Test
    void resetCounters_nok_listenerNotFound() {
        when(controllableMessageListener1.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_1);
        when(controllableMessageListener2.getListenerId()).thenReturn(MESSAGE_LISTENER_ID_2);

        assertThatThrownBy(() -> messagingActuator.resetCounters("unknown_listener"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Listener with id: unknown_listener not found");
    }
}
