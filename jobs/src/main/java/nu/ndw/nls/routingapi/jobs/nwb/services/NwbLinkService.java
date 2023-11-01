package nu.ndw.nls.routingapi.jobs.nwb.services;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingapi.jobs.nwb.mappers.NwbRoadSectionToLinkMapper;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NwbLinkService {

    private final NwbRoadSectionToLinkMapper nwbRoadSectionToLinkMapper;
    private final NwbRoadSectionService roadSectionService;

    @Transactional(readOnly = true)
    public List<Link> getLinks(int versionId) {
        // To reduce memory footprint, the streams are lazy and keep an open database connection and therefore need to
        // be closed after use.
        try (Stream<NwbRoadSectionDto> roadSectionStream = roadSectionService.findLazyCar(versionId)) {
            return roadSectionStream
                    .map(nwbRoadSectionToLinkMapper::map)
                    .toList();
        }
    }
}
