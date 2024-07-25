package nu.ndw.nls.accessibilitymap.jobs.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.shared.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignMapperRegistry;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignData;
import nu.ndw.nls.accessibilitymap.trafficsignclient.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityLinkService {

    private final TrafficSignService trafficSignService;
    private final NwbVersionCrudService nwbVersionService;
    private final NwbRoadSectionService nwbRoadSectionService;
    private final NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;
    private final TrafficSignMapperRegistry trafficSignMapperRegistry;

    @Transactional(readOnly = true)
    public AccessibilityLinkData getLinks() {
        TrafficSignData trafficSignData = trafficSignService.getTrafficSigns(
                trafficSignMapperRegistry.getIncludedRvvCodes());
        int nwbVersionId = nwbVersionService.findLatestByReferenceDate(trafficSignData.maxNwbReferenceDate())
                .orElseThrow(() -> new IllegalStateException("NWB version " + trafficSignData.maxNwbReferenceDate()
                        + " from traffic signs response not found in database"))
                .getVersionId();
        // To reduce memory footprint, the stream is lazy and keeps an open database connection and therefore needs to
        // be closed after use.
        try (Stream<NwbRoadSectionDto> roadSections = nwbRoadSectionService.findLazyCar(nwbVersionId)) {
            List<AccessibilityLink> links = roadSections
                    .map(roadSection -> nwbRoadSectionToLinkMapper.map(roadSection,
                            trafficSignData.getTrafficSignsByRoadSectionId(roadSection.getRoadSectionId())))
                    .toList();
            return new AccessibilityLinkData(links, nwbVersionId, trafficSignData.maxEventTimestamp());
        }
    }


    public record AccessibilityLinkData(List<AccessibilityLink> links, int nwbVersionId, Instant trafficSignTimestamp) {

    }
}
