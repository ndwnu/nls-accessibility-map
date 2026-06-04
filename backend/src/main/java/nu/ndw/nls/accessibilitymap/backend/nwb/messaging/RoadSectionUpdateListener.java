package nu.ndw.nls.accessibilitymap.backend.nwb.messaging;

import io.micrometer.core.annotation.Timed;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.cache.CacheLoadedEvent;
import nu.ndw.nls.accessibilitymap.accessibility.network.NetworkDataService;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.AccessibilityNwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbData;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.dto.NwbDataUpdates;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.messaging.dto.NwbRoadSectionUpdate;
import nu.ndw.nls.accessibilitymap.backend.nwb.messaging.mapper.NwbRoadSectionUpdateMapper;
import nu.ndw.nls.db.nwb.jooq.mappers.NwbVersionIdMapper;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
// Depends on the template to create the stream queue if it does not exist.
@DependsOn("updateRoadSectionStreamTemplate")
public class RoadSectionUpdateListener {

    public static final String LISTENER_ID = "updateRoadSectionStreamListener";

    private final NetworkDataService networkDataService;

    private final NwbVersionIdMapper nwbVersionIdMapper;

    private final NwbRoadSectionUpdateMapper nwbRoadSectionUpdateMapper;

    private final JsonMapper jsonMapper;

    private final RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Value("${nu.ndw.nls.accessibilitymap.messaging.stream-queues.updateRoadSection.listener-auto-start:true}")
    private boolean autoStartup;

    @RabbitListener(id = LISTENER_ID,
            queues = "nls_accessibility_map_update_road_section",
            containerFactory = "updateRoadSectionStreamFactory",
            autoStartup = "${nu.ndw.nls.accessibilitymap.messaging.stream-queues.updateRoadSection.listener-auto-start:true}"
    )
    @Timed(description = "time spend processing road section changes")
    public void handleMessage(Message message) {
        NwbRoadSectionUpdate nwbRoadSectionUpdate = toRoadSectionUpdate(message);
        log.debug("Received message raw json: {}",
                new String(message.getBody(), StandardCharsets.UTF_8));
        log.debug("Received road section update: {}", nwbRoadSectionUpdate);
        NwbData nwbData = networkDataService.get().getNwbData();
        int updateMapVersion = nwbVersionIdMapper.mapFromReferenceDate(nwbRoadSectionUpdate.nwbVersion());
        log.debug("Update nwb map version: {}", updateMapVersion);
        if (updateMapVersionIsDifferentFromActiveMapVersion(updateMapVersion, nwbData.getNwbVersionId())) {

            if (updateMapVersionIsEarlierThanActiveVersion(updateMapVersion, nwbData.getNwbVersionId())) {
                log.warn("Received road section update for previous map version active: {} message: {}",
                        updateMapVersion,
                        nwbRoadSectionUpdate);
                return;
            } else {
                throw new IllegalArgumentException("Map version is newer than the one currently in use");
            }
        }

        AccessibilityNwbRoadSectionUpdate accessibilityNwbRoadSectionUpdate = nwbRoadSectionUpdateMapper.map(nwbRoadSectionUpdate);
        NwbDataUpdates nwbDataUpdates = new NwbDataUpdates(updateMapVersion, List.of(accessibilityNwbRoadSectionUpdate));
        networkDataService.writeNwbDataUpdates(nwbDataUpdates);
    }

    private boolean updateMapVersionIsDifferentFromActiveMapVersion(int updateMapVersion, int activeMapVersion) {
        return updateMapVersion != activeMapVersion;
    }

    private boolean updateMapVersionIsEarlierThanActiveVersion(int updateMapVersion, int activeMapVersion) {
        return updateMapVersion < activeMapVersion;
    }

    private NwbRoadSectionUpdate toRoadSectionUpdate(Message message) {
        try {
            return jsonMapper.readValue(message.getBody(), NwbRoadSectionUpdate.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @EventListener
    void startListener(CacheLoadedEvent cacheLoadedEvent) {
        if (cacheLoadedEvent.getType() == CacheLoadedEvent.Type.NETWORK_DATA) {
            MessageListenerContainer messageListenerContainer = rabbitListenerEndpointRegistry.getListenerContainer(LISTENER_ID);
            if (!autoStartup && !messageListenerContainer.isRunning()) {
                log.info("Initial network data loaded starting RoadSectionUpdateListener");
                messageListenerContainer.start();
            }
        }
    }
}
