package nu.ndw.nls.accessibilitymap.jobs.services;

import static com.conductor.stream.utils.OrderedStreamUtils.groupBy;
import static com.conductor.stream.utils.OrderedStreamUtils.join;

import com.conductor.stream.utils.join.JoinType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.accessibilitymap.jobs.nwb.services.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers.TrafficSignToLinkTagMapper;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.services.TrafficSignService;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityLinkService {

    private final NwbRoadSectionService roadSectionService;
    private final TrafficSignService trafficSignService;
    private final NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;
    private final TrafficSignToLinkTagMapper trafficSignToLinkTagMapper;

    @Transactional(readOnly = true)
    public List<Link> getLinks(int nwbVersionId) {
        // To reduce memory footprint, the streams are lazy and keep an open database connection and therefore need to
        // be closed after use.
        try (Stream<NwbRoadSectionDto> roadSections = roadSectionService.findLazyCar(nwbVersionId);
                Stream<TrafficSignJsonDtoV3> trafficSigns = trafficSignService.getTrafficSigns()) {

            Stream<List<TrafficSignJsonDtoV3>> trafficSignsByRoadSectionId = groupBy(trafficSigns,
                    t -> t.getLocation().getRoad().getRoadSectionId());

            Stream<Link> links = join(roadSections,
                    trafficSignsByRoadSectionId,
                    Comparator.naturalOrder(),
                    NwbRoadSectionDto::getRoadSectionId,
                    this::getTrafficSignsRoadSectionId,
                    this::mapToLink,
                    JoinType.LEFT);
            return links.toList();
        }
    }

    private long getTrafficSignsRoadSectionId(List<TrafficSignJsonDtoV3> trafficSigns) {
        return Long.parseLong(trafficSigns.get(0).getLocation().getRoad().getRoadSectionId());
    }

    private Link mapToLink(NwbRoadSectionDto roadSection, List<TrafficSignJsonDtoV3> trafficSigns) {
        Link link = nwbRoadSectionToLinkMapper.map(roadSection);
        if (trafficSigns != null) {
            trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        }
        return link;
    }
}
