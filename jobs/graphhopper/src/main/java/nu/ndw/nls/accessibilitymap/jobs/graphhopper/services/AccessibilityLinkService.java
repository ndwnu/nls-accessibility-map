package nu.ndw.nls.accessibilitymap.jobs.graphhopper.services;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.accessibility.nwb.service.NwbRoadSectionService;
import nu.ndw.nls.accessibilitymap.jobs.graphhopper.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessibilityLinkService {

    private final NwbRoadSectionService nwbRoadSectionService;

    private final NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;

    @Transactional(readOnly = true)
    public List<AccessibilityLink> getLinks(int nwbVersionId) {

        // To reduce memory footprint, the stream is lazy and keeps an open database connection and therefore needs to
        // be closed after use.
        try (Stream<NwbRoadSectionDto> nwbRoadSections = nwbRoadSectionService.findLazyCar(nwbVersionId)) {
            return nwbRoadSections
                    .map(nwbRoadSectionToLinkMapper::map)
                    .toList();
        }
    }

}
