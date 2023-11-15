package nu.ndw.nls.accessibilitymap.jobs.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.jobs.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.TrafficSignToLinkTagMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService.TrafficSignData;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.db.nwb.jooq.services.NwbVersionCrudService;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityLinkService {

    private final TrafficSignService trafficSignService;
    private final NwbVersionCrudService nwbVersionService;
    private final NwbRoadSectionService nwbRoadSectionService;
    private final NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;
    private final TrafficSignToLinkTagMapper trafficSignToLinkTagMapper;

    @Transactional(readOnly = true)
    public AccessibilityLinkData getLinks() {
        TrafficSignData trafficSignData = trafficSignService.getTrafficSigns();
        int nwbVersionId = nwbVersionService.findLatestByReferenceDate(trafficSignData.maxNwbReferenceDate())
                .orElseThrow(() -> new IllegalStateException("NWB version " + trafficSignData.maxNwbReferenceDate()
                        + " from traffic signs response not found in database"))
                .getVersionId();
        // To reduce memory footprint, the stream is lazy and keeps an open database connection and therefore needs to
        // be closed after use.
        try (Stream<NwbRoadSectionDto> roadSections = nwbRoadSectionService.findLazyCar(nwbVersionId)) {
            List<Link> links = roadSections
                    .map(r -> mapToLink(r, trafficSignData.trafficSignsByRoadSectionId().get(r.getRoadSectionId())))
                    .toList();
            return new AccessibilityLinkData(links, nwbVersionId, trafficSignData.maxEventTimestamp());
        }
    }

    private Link mapToLink(NwbRoadSectionDto roadSection, List<TrafficSignJsonDtoV3> trafficSigns) {
        Link link = nwbRoadSectionToLinkMapper.map(roadSection);
        if (trafficSigns != null) {
            trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        }
        return link;
    }

    public record AccessibilityLinkData(List<Link> links, int nwbVersionId, Instant trafficSignTimestamp) {

    }
}
