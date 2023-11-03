package nu.ndw.nls.accessibilitymap.jobs.trafficsigns.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.LocationJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.RoadJsonDtoV3;
import nu.ndw.nls.accessibilitymap.jobs.trafficsigns.dtos.TrafficSignJsonDtoV3;
import nu.ndw.nls.routingmapmatcher.domain.model.Link;
import nu.ndw.nls.routingmapmatcher.domain.model.LinkTag;
import org.junit.jupiter.api.Test;

class TrafficSignToLinkTagMapperTest {

    private final TrafficSignToLinkTagMapper trafficSignToLinkTagMapper = new TrafficSignToLinkTagMapper();

    @Test
    void setLinkTags_ok_noEntrySigns() {
        // All no entry signs are mapped to the same Boolean true value, so we have to test them separately to verify
        // the mapping from RVV code to LinkTag is correct.
        Map.of("C6", LinkTag.C6_CAR_ACCESS_FORBIDDEN,
                "C7", LinkTag.C7_HGV_ACCESS_FORBIDDEN,
                "C7a", LinkTag.C7A_AUTO_BUS_ACCESS_FORBIDDEN,
                "C7b", LinkTag.C7B_HGV_AND_AUTO_BUS_ACCESS_FORBIDDEN,
                "C10", LinkTag.C10_TRAILER_ACCESS_FORBIDDEN,
                "C11", LinkTag.C11_MOTOR_BIKE_ACCESS_FORBIDDEN,
                "C12", LinkTag.C12_MOTOR_VEHICLE_ACCESS_FORBIDDEN).forEach((rvvCode, linkTag) -> {
            Link link = Link.builder().build();
            List<TrafficSignJsonDtoV3> trafficSigns = List.of(createTrafficSignDto(rvvCode, null));
            trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
            assertEquals(Map.of(linkTag.getLabel() + LinkTag.FORWARD_SUFFIX, true,
                    linkTag.getLabel() + LinkTag.REVERSE_SUFFIX, true), link.getTags());
        });
    }

    @Test
    void setLinkTags_ok_maximumSigns() {
        // The maximum signs can be tested in a single call, because they each have a different value.
        Link link = Link.builder().build();
        List<TrafficSignJsonDtoV3> trafficSigns = List.of(
                createTrafficSignDto("C17", "10"),
                createTrafficSignDto("C18", "2.5"),
                createTrafficSignDto("C19", "2,8"),
                createTrafficSignDto("C20", "4.8"),
                createTrafficSignDto("C21", "5,4"));
        trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        assertEquals(Map.of(LinkTag.C17_MAX_LENGTH.getLabel() + LinkTag.FORWARD_SUFFIX, 10.0,
                LinkTag.C17_MAX_LENGTH.getLabel() + LinkTag.REVERSE_SUFFIX, 10.0,
                LinkTag.C18_MAX_WIDTH.getLabel() + LinkTag.FORWARD_SUFFIX, 2.5,
                LinkTag.C18_MAX_WIDTH.getLabel() + LinkTag.REVERSE_SUFFIX, 2.5,
                LinkTag.C19_MAX_HEIGHT.getLabel() + LinkTag.FORWARD_SUFFIX, 2.8,
                LinkTag.C19_MAX_HEIGHT.getLabel() + LinkTag.REVERSE_SUFFIX, 2.8,
                LinkTag.C20_MAX_AXLE_LOAD.getLabel() + LinkTag.FORWARD_SUFFIX, 4.8,
                LinkTag.C20_MAX_AXLE_LOAD.getLabel() + LinkTag.REVERSE_SUFFIX, 4.8,
                LinkTag.C21_MAX_WEIGHT.getLabel() + LinkTag.FORWARD_SUFFIX, 5.4,
                LinkTag.C21_MAX_WEIGHT.getLabel() + LinkTag.REVERSE_SUFFIX, 5.4), link.getTags());
    }

    @Test
    void setLinkTags_ok_unsupportedRvvCode() {
        Link link = Link.builder().build();
        List<TrafficSignJsonDtoV3> trafficSigns = List.of(createTrafficSignDto("C22", null));
        trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        assertEquals(Map.of(), link.getTags());
    }

    @Test
    void setLinkTags_ok_blackCodeNull() {
        Link link = Link.builder().build();
        List<TrafficSignJsonDtoV3> trafficSigns = List.of(createTrafficSignDto("C17", null));
        trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        assertEquals(Map.of(), link.getTags());
    }

    @Test
    void setLinkTags_ok_unsupportedBlackCode() {
        Link link = Link.builder().build();
        List<TrafficSignJsonDtoV3> trafficSigns = List.of(createTrafficSignDto("C17", "10 m"));
        trafficSignToLinkTagMapper.setLinkTags(link, trafficSigns);
        assertEquals(Map.of(), link.getTags());
    }

    private TrafficSignJsonDtoV3 createTrafficSignDto(String rvvCode, String blackCode) {
        return TrafficSignJsonDtoV3.builder()
                .rvvCode(rvvCode)
                .blackCode(blackCode)
                .location(LocationJsonDtoV3.builder()
                        .road(RoadJsonDtoV3.builder()
                                .roadSectionId("1")
                                .build())
                        .build())
                .build();
    }
}
