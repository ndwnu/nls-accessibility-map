package nu.ndw.nls.accessibilitymap.backend.nwb.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Message;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.SneakyThrows;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.network.dto.NetworkData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.NwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.backend.nwb.messaging.mapper.NwbRoadSectionUpdateMapper;
import nu.ndw.nls.db.nwb.jooq.mappers.NwbVersionIdMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoadSectionUpdateListenerTest {

    private static final byte[] CONTENT_BYTES = "valid content".getBytes();

    private static final LocalDate NEW_VERSION_DATE = LocalDate.of(2026, 1, 1);

    private static final int NWB_VERSION_INT = 20260101;

    private static final LocalDate EARLIER_NWB_VERSION_DATE = LocalDate.of(2025, 1, 1);

    private static final int EARLIER_NWB_VERSION_INT = 20250101;

    private static final LocalDate LATER_NWB_VERSION_DATE = LocalDate.of(2026, 4, 1);

    private static final int LATER_NWB_VERSION_INT = 26040101;

    @Mock
    private Message message;

    @Mock
    private NetworkDataService networkDataService;

    @Mock
    private NwbVersionIdMapper nwbVersionIdMapper;

    @Mock
    private NwbRoadSectionUpdateMapper nwbRoadSectionUpdateMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NwbRoadSectionUpdate nwbRoadSectionUpdate;

    @Mock
    private AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate;

    @Mock
    private NetworkData networkData;

    @Mock
    private NwbData nwbData;

    @Captor
    private ArgumentCaptor<NwbDataUpdates> nwbDataUpdatesCaptor;

    private RoadSectionUpdateListener roadSectionUpdateListener;

    @BeforeEach
    void setUp() {
        roadSectionUpdateListener = new RoadSectionUpdateListener(networkDataService,
                nwbVersionIdMapper,
                nwbRoadSectionUpdateMapper,
                objectMapper);
    }

    @SneakyThrows
    @Test
    void handleMessage() {

        when(message.getBodyAsBinary()).thenReturn(CONTENT_BYTES);
        when(objectMapper.readValue(CONTENT_BYTES, NwbRoadSectionUpdate.class)).thenReturn(nwbRoadSectionUpdate);
        when(nwbRoadSectionUpdate.nwbVersion()).thenReturn(NEW_VERSION_DATE);
        when(nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate)).thenReturn(accessibilityNwbRoadSectionUpdate);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.getNwbVersionId()).thenReturn(NWB_VERSION_INT);
        when(nwbVersionIdMapper.mapFromReferenceDate(NEW_VERSION_DATE)).thenReturn(NWB_VERSION_INT);

        roadSectionUpdateListener.handleMessage(message);

        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(NWB_VERSION_INT, List.of(accessibilityNwbRoadSectionUpdate));
        verify(networkDataService).writeNwbDataUpdates(nwbDataUpdatesCaptor.capture());
        assertThat(nwbDataUpdatesCaptor.getValue()).usingRecursiveComparison().isEqualTo(nwbDataUpdates);
        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isEqualTo(1);
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isZero();
    }

    @SneakyThrows
    @Test
    void handleMessage_earlier_nwbVersion_in_message_than_current_nwbVersion() {

        when(message.getBodyAsBinary()).thenReturn(CONTENT_BYTES);
        when(objectMapper.readValue(CONTENT_BYTES, NwbRoadSectionUpdate.class)).thenReturn(nwbRoadSectionUpdate);
        when(nwbRoadSectionUpdate.nwbVersion()).thenReturn(EARLIER_NWB_VERSION_DATE);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.getNwbVersionId()).thenReturn(NWB_VERSION_INT);
        when(nwbVersionIdMapper.mapFromReferenceDate(EARLIER_NWB_VERSION_DATE)).thenReturn(EARLIER_NWB_VERSION_INT);

        roadSectionUpdateListener.handleMessage(message);

        verify(networkDataService, times(0)).writeNwbDataUpdates(nwbDataUpdatesCaptor.capture());
        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isZero();
    }

    @Test
    @SneakyThrows
    void handleMessage_later_nwbVersion_in_message_than_current_nwbVersion() {
        when(message.getBodyAsBinary()).thenReturn(CONTENT_BYTES);
        when(objectMapper.readValue(CONTENT_BYTES, NwbRoadSectionUpdate.class)).thenReturn(nwbRoadSectionUpdate);
        when(nwbRoadSectionUpdate.nwbVersion()).thenReturn(LATER_NWB_VERSION_DATE);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.getNwbVersionId()).thenReturn(NWB_VERSION_INT);
        when(nwbVersionIdMapper.mapFromReferenceDate(LATER_NWB_VERSION_DATE)).thenReturn(LATER_NWB_VERSION_INT);

        assertThatThrownBy(() -> roadSectionUpdateListener.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map version is newer than the one currently in use");
        verify(networkDataService, times(0)).writeNwbDataUpdates(nwbDataUpdatesCaptor.capture());
        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isEqualTo(1);
    }

    @Test
    @SneakyThrows
    void handleMessage_invalid_message_content_throws_exception() {
        when(message.getBodyAsBinary()).thenReturn(CONTENT_BYTES);
        when(objectMapper.readValue(CONTENT_BYTES, NwbRoadSectionUpdate.class)).thenThrow(IOException.class);

        assertThatThrownBy(() -> roadSectionUpdateListener.handleMessage(message)).isInstanceOf(IllegalArgumentException.class);
        verify(networkDataService, times(0)).writeNwbDataUpdates(nwbDataUpdatesCaptor.capture());
        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isEqualTo(1);
    }

    @SneakyThrows
    @Test
    void resetCounters_ok() {
        when(message.getBodyAsBinary()).thenReturn(CONTENT_BYTES);
        when(objectMapper.readValue(CONTENT_BYTES, NwbRoadSectionUpdate.class)).thenReturn(nwbRoadSectionUpdate);
        when(nwbRoadSectionUpdate.nwbVersion()).thenReturn(LATER_NWB_VERSION_DATE);
        when(networkDataService.get()).thenReturn(networkData);
        when(networkData.getNwbData()).thenReturn(nwbData);
        when(nwbData.getNwbVersionId()).thenReturn(NWB_VERSION_INT);
        when(nwbVersionIdMapper.mapFromReferenceDate(LATER_NWB_VERSION_DATE)).thenReturn(LATER_NWB_VERSION_INT);

        assertThatThrownBy(() -> roadSectionUpdateListener.handleMessage(message)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Map version is newer than the one currently in use");

        roadSectionUpdateListener.resetCounters();

        assertThat(roadSectionUpdateListener.getMessagesProcessed()).isZero();
        assertThat(roadSectionUpdateListener.getMessagesRejected()).isZero();
    }

    @Test
    void getListenerId() {
        assertThat(roadSectionUpdateListener.getListenerId()).isEqualTo("updateRoadSectionStreamListener");
    }
}
