package nu.ndw.nls.accessibilitymap.backend.nwb.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.rabbitmq.stream.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionUpdateListenerTest {

    @Mock
    private Message message;

    @InjectMocks
    private RoadSectionUpdateListener roadSectionUpdateListener;

    @Test
    void handleMessage_ok_processMessage() {
        when(message.getBodyAsBinary()).thenReturn("valid content".getBytes());

        roadSectionUpdateListener.handleMessage(message);

        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isEqualTo(1);
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isZero();
    }

    @Test
    void handleMessage_exception_rejectMessage() {
        when(message.getBodyAsBinary()).thenReturn("this message is broken".getBytes());

        assertThatThrownBy(() -> roadSectionUpdateListener.handleMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed processing message");

        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isEqualTo(1);
    }

    @Test
    void resetCounters_ok() {
        when(message.getBodyAsBinary()).thenReturn("valid content".getBytes());
        roadSectionUpdateListener.handleMessage(message); // Increment counters

        roadSectionUpdateListener.resetCounters();

        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isZero();
    }
}
