package nu.ndw.nls.accessibilitymap.backend.nwb.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.stream.Message;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.NwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.backend.nwb.messaging.mapper.NwbRoadSectionUpdateMapper;
import nu.ndw.nls.accessibilitymap.backend.rabbitmq.actuators.ControllableMessageListener;
import nu.ndw.nls.db.nwb.jooq.mappers.NwbVersionIdMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
// Depends on the template to create the stream queue if it does not exist.
@DependsOn("updateRoadSectionStreamTemplate")
public class RoadSectionUpdateListener implements ControllableMessageListener {

    private static final String LISTENER_ID = "updateRoadSectionStreamListener";

    private final AtomicInteger messagesProcessed = new AtomicInteger();

    private final AtomicInteger messagesRejected = new AtomicInteger();

    private final NetworkDataService networkDataService;

    private final NwbVersionIdMapper nwbVersionIdMapper;

    private final NwbRoadSectionUpdateMapper nwbRoadSectionUpdateMapper;

    private final ObjectMapper objectMapper;

    @RabbitListener(id = LISTENER_ID,
            queues = "nls_accessibility_map_update_road_section",
            containerFactory = "updateRoadSectionStreamFactory")
    public void handleMessage(Message message) {
        NwbRoadSectionUpdate nwbRoadSectionUpdate = toRoadSectionUpdate(message);
        NwbData nwbData = networkDataService.get().getNwbData();
        int updateMapVersion = nwbVersionIdMapper.mapFromReferenceDate(nwbRoadSectionUpdate.nwbVersion());

        if (updateMapVersionIsDifferentFromActiveMapVersion(updateMapVersion, nwbData.getNwbVersionId())) {

            if (updateMapVersionIsEarlierThanActiveVersion(updateMapVersion, nwbData.getNwbVersionId())) {
                messagesRejected.incrementAndGet();
                return;
            } else {
                throw new IllegalArgumentException("Map version is newer than the one currently in use");
            }
        }

        AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate = nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate);
        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(updateMapVersion,
                List.of(accessibilityNwbRoadSectionUpdate));
        networkDataService.writeNwbDataUpdates(nwbDataUpdates);
        messagesProcessed.incrementAndGet();
    }

    private boolean updateMapVersionIsDifferentFromActiveMapVersion(int updateMapVersion, int activeMapVersion) {
        return !Objects.equals(updateMapVersion, activeMapVersion);
    }

    private boolean updateMapVersionIsEarlierThanActiveVersion(int updateMapVersion, int activeMapVersion) {
        return updateMapVersion < activeMapVersion;
    }

    private NwbRoadSectionUpdate toRoadSectionUpdate(Message message) {
        try {
            return objectMapper.readValue(message.getBodyAsBinary(), NwbRoadSectionUpdate.class);
        } catch (IOException e) {
            messagesRejected.incrementAndGet();
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getListenerId() {
        return LISTENER_ID;
    }

    @Override
    public void resetCounters() {
        messagesRejected.set(0);
        messagesProcessed.set(0);
    }

    @Override
    public int getMessagesProcessed() {
        return messagesProcessed.get();
    }

    @Override
    public int getMessagesRejected() {
        return messagesRejected.get();
    }
}
