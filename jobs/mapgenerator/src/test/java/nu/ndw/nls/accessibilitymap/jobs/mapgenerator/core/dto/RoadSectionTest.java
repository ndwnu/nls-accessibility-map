//package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto;
//
//import java.util.List;
//import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSign;
//import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.core.dto.trafficsign.TrafficSignType;
//import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.test.unit.ValidationTest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class RoadSectionTest extends ValidationTest {
//
//    private RoadSection roadSection;
//
//    @BeforeEach
//    void setUp() {
//
//        roadSection = RoadSection.builder()
//                .id(1)
//                .build();
//        RoadSectionFragment roadSectionFragment = RoadSectionFragment.builder()
//                .id(2)
//                .roadSection(roadSection)
//                .build();
//        roadSection.getRoadSectionFragments().add(roadSectionFragment);
//
//        DirectionalSegment directionalSegment = DirectionalSegment.builder()
//                .accessible(true)
//                .direction(Direction.FORWARD)
//                .roadSectionFragment(roadSectionFragment)
//                .trafficSign(TrafficSign.builder()
//                        .id(3)
//                        .roadSectionId(1)
//                        .direction(Direction.FORWARD)
//                        .fraction(2d)
//                        .longitude(3d)
//                        .latitude(4d)
//                        .textSigns(List.of())
//                        .trafficSignType(TrafficSignType.C7)
//                        .build())
//                .build();
//
//        roadSectionFragment.setForwardSegment(directionalSegment);
//
//    }
//
//    @Test
//    void validate_ok() {
//
//        validate(roadSection, List.of(), List.of());
//    }
//
//    @Override
//    protected Class<?> getClassToTest() {
//
//        return RoadSection.class;
//    }
//}