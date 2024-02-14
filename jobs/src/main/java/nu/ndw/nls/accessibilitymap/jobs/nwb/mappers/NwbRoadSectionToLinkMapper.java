package nu.ndw.nls.accessibilitymap.jobs.nwb.mappers;

import java.util.List;
import nu.ndw.nls.accessibilitymap.shared.model.AccessibilityLink;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignAccessibilityDto;
import nu.ndw.nls.accessibilitymap.trafficsignclient.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsign.mappers.TrafficSignToDtoMapper;
import nu.ndw.nls.data.api.nwb.dtos.NwbRoadSectionDto;
import nu.ndw.nls.routingmapmatcher.network.model.DirectionalDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = RijksdriehoekToWgs84Mapper.class)
public abstract class NwbRoadSectionToLinkMapper {

    private static final String BACKWARD = "T";
    private static final String FORWARD = "H";

    @Autowired
    private TrafficSignToDtoMapper trafficSignToDtoMapper;

    public AccessibilityLink map(NwbRoadSectionDto roadSectionDto, List<TrafficSignJsonDtoV3> trafficSigns) {
        return this.map(roadSectionDto, trafficSignToDtoMapper.map(trafficSigns));
    }

    @Mapping(source = "roadSection.roadSectionId", target = "id")
    @Mapping(source = "roadSection.junctionIdFrom", target = "fromNodeId")
    @Mapping(source = "roadSection.junctionIdTo", target = "toNodeId")
    @Mapping(source = "roadSection.geometry.length", target = "distanceInMeters")
    @Mapping(source = "roadSection.municipalityId", target = "municipalityCode")
    @Mapping(source = "roadSection.drivingDirection", target = "accessibility")
    protected abstract AccessibilityLink map(NwbRoadSectionDto roadSection, TrafficSignAccessibilityDto accessibility);

    protected DirectionalDto<Boolean> getAccessibility(String drivingDirection) {
        return DirectionalDto.<Boolean>builder()
                .forward(!BACKWARD.equals(drivingDirection))
                .reverse(!FORWARD.equals(drivingDirection))
                .build();
    }

}
