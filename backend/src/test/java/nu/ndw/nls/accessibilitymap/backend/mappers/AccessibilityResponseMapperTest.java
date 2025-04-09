//package nu.ndw.nls.accessibilitymap.backend.mappers;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.Mockito.when;
//
//import jakarta.validation.Valid;
//import java.util.List;
//import java.util.SortedMap;
//import java.util.TreeMap;
//import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.AccessibilityMapResponseJson;
//import nu.ndw.nls.accessibilitymap.backend.generated.model.v1.RoadSectionJson;
//import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.dto.response.RoadSection;
//import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.AccessibilityResponseMapper;
//import nu.ndw.nls.accessibilitymap.backend.accessibility.controllers.mappers.response.RoadSectionJsonResponseMapper;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class AccessibilityResponseMapperTest {
//
//    private static final int ID_A = 1;
//    private static final int ID_B = 2;
//    private static final int ID_C = 3;
//    private static final int ID_D = 4;
//    private static final int ID_REQUESTED = 5;
//    @Mock
//    private RoadSectionJsonResponseMapper roadSectionJsonResponseMapper;
//
//    @Mock
//    private RoadSection roadSectionA;
//
//    @Mock
//    private RoadSection roadSectionB;
//    @Mock
//    private RoadSection roadSectionC;
//    @Mock
//    private RoadSection roadSectionD;
//
//    @Mock
//    private RoadSection requestedRoadSection;
//    @Mock
//    private RoadSectionJson roadSectionJsonB;
//    @Mock
//    private RoadSectionJson roadSectionJsonC;
//    @Mock
//    private RoadSectionJson roadSectionJsonD;
//    @Mock
//    private RoadSectionJson matchedRoadSectionJson;
//
//    @InjectMocks
//    private AccessibilityResponseMapper accessibilityResponseMapper;
//
//    @Test
//    void map() {
//        SortedMap<Integer, RoadSection> idToRoadSection = new TreeMap<>();
//        idToRoadSection.put(ID_A, roadSectionA);
//        idToRoadSection.put(ID_B, roadSectionB);
//        idToRoadSection.put(ID_C, roadSectionC);
//        idToRoadSection.put(ID_D, roadSectionD);
//        idToRoadSection.put(ID_REQUESTED, requestedRoadSection);
//
//        when(roadSectionA.getForwardAccessible()).thenReturn(Boolean.TRUE);
//        when(roadSectionA.getBackwardAccessible()).thenReturn(Boolean.TRUE);
//
//        when(roadSectionB.getForwardAccessible()).thenReturn(Boolean.FALSE);
//
//        when(roadSectionC.getForwardAccessible()).thenReturn(Boolean.TRUE);
//        when(roadSectionC.getBackwardAccessible()).thenReturn(Boolean.FALSE);
//
//        when(roadSectionD.getForwardAccessible()).thenReturn(Boolean.FALSE);
//
//        when(roadSectionJsonResponseMapper.mapToRoadSection(roadSectionB)).thenReturn(roadSectionJsonB);
//        when(roadSectionJsonResponseMapper.mapToRoadSection(roadSectionC)).thenReturn(roadSectionJsonC);
//        when(roadSectionJsonResponseMapper.mapToRoadSection(roadSectionD)).thenReturn(roadSectionJsonD);
//        when(roadSectionJsonResponseMapper.mapToRoadSection(requestedRoadSection)).thenReturn(matchedRoadSectionJson);
//
//        AccessibilityMapResponseJson result = accessibilityResponseMapper.map(idToRoadSection, ID_REQUESTED);
//
//        assertNotNull(result);
//        List<@Valid RoadSectionJson> inaccessibleRoadSections = result.getInaccessibleRoadSections();
//        assertEquals(List.of(roadSectionJsonB, roadSectionJsonC, roadSectionJsonD, matchedRoadSectionJson),
//                inaccessibleRoadSections);
//        assertEquals(matchedRoadSectionJson, result.getMatchedRoadSection());
//    }
//}