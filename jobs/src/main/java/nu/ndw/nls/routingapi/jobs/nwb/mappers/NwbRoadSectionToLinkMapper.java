package nu.ndw.nls.routingapi.jobs.nwb.mappers;

import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NwbRoadSectionToLinkMapper {

    private static final String BACKWARD = "T";
    private static final String FORWARD = "H";
    private static final int NO_ACCESS_SPEED = 0;
    private static final int DEFAULT_SPEED = 50;

    private final RijksdriehoekToWgs84Mapper rijksdriehoekToWgs84Mapper;

    public Link map(NwbRoadSectionDto roadSectionDto) {
        String drivingDirection = roadSectionDto.getDrivingDirection();
        return Link.builder()
                .id(roadSectionDto.getRoadSectionId())
                .fromNodeId(roadSectionDto.getJunctionIdFrom())
                .toNodeId(roadSectionDto.getJunctionIdTo())
                .speedInKilometersPerHour(BACKWARD.equals(drivingDirection) ? NO_ACCESS_SPEED : DEFAULT_SPEED)
                .reverseSpeedInKilometersPerHour(FORWARD.equals(drivingDirection) ? NO_ACCESS_SPEED : DEFAULT_SPEED)
                .distanceInMeters(roadSectionDto.getGeometry().getLength())
                .geometry(rijksdriehoekToWgs84Mapper.map(roadSectionDto.getGeometry()))
                .build();
    }
}
