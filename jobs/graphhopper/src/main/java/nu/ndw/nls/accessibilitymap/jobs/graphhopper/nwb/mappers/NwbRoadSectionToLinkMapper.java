package nu.ndw.nls.accessibilitymap.jobs.graphhopper.nwb.mappers;

import nu.ndw.nls.accessibilitymap.accessibility.graphhopper.dto.AccessibilityLink;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class NwbRoadSectionToLinkMapper {

    private static final String BACKWARD = "T";

    private static final String FORWARD = "H";

    @Mapping(source = "roadSection.roadSectionId", target = "id")
    @Mapping(source = "roadSection.junctionIdFrom", target = "fromNodeId")
    @Mapping(source = "roadSection.junctionIdTo", target = "toNodeId")
    @Mapping(source = "roadSection.geometry.length", target = "distanceInMeters")
    @Mapping(source = "roadSection.municipalityId", target = "municipalityCode")
    @Mapping(source = "roadSection.drivingDirection", target = "accessibility")
    public abstract AccessibilityLink map(NwbRoadSectionDto roadSection);

    protected DirectionalDto<Boolean> getAccessibility(String drivingDirection) {
        return DirectionalDto.<Boolean>builder()
                .forward(!BACKWARD.equals(drivingDirection))
                .reverse(!FORWARD.equals(drivingDirection))
                .build();
    }
}
