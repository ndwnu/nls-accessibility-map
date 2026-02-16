package nu.ndw.nls.accessibilitymap.backend.accessibility.v2.mapper.request.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.graphhopper.util.shapes.BBox;
import java.util.UUID;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.AccessibilityRequest.AccessibilityRequestBuilder;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.Municipality;
import nu.ndw.nls.accessibilitymap.backend.municipality.repository.dto.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.municipality.service.MunicipalityService;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.AreaRequestJson;
import nu.ndw.nls.accessibilitymap.backend.openapi.model.v2.MunicipalityAreaRequestJson;
import nu.ndw.nls.springboot.web.error.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestBuilderMunicipalityAreaMapperTest {

    private AccessibilityRequestBuilderMunicipalityAreaMapper accessibilityRequestBuilderMunicipalityAreaMapper;

    @Mock
    private MunicipalityService municipalityService;

    @BeforeEach
    void setUp() {

        accessibilityRequestBuilderMunicipalityAreaMapper = new AccessibilityRequestBuilderMunicipalityAreaMapper(municipalityService);
    }

    @Test
    void build() {

        MunicipalityAreaRequestJson municipalityAreaRequestJson = MunicipalityAreaRequestJson.builder()
                .id("GM0001")
                .build();

        Municipality municipality = Municipality.builder()
                .id("GM0001")
                .bounds(MunicipalityBoundingBox.builder()
                        .latitudeTo(1D)
                        .longitudeTo(2D)
                        .latitudeFrom(3D)
                        .longitudeFrom(4D)
                        .build())
                .searchDistanceInMetres(5)
                .startCoordinateLatitude(6D)
                .startCoordinateLongitude(7D)
                .build();
        when(municipalityService.getMunicipalityById("GM0001")).thenReturn(municipality);

        AccessibilityRequestBuilder accessibilityRequestBuilder = AccessibilityRequest.builder();

        accessibilityRequestBuilderMunicipalityAreaMapper.build(accessibilityRequestBuilder, municipalityAreaRequestJson);

        AccessibilityRequest accessibilityRequest = accessibilityRequestBuilder.build();
        assertThat(accessibilityRequest).isNotNull();
        assertThat(accessibilityRequest.requestArea()).isEqualTo(BBox.fromPoints(
                municipality.bounds().latitudeFrom(),
                municipality.bounds().longitudeFrom(),
                municipality.bounds().latitudeTo(),
                municipality.bounds().longitudeTo()
        ));
        assertThat(accessibilityRequest.searchArea()).isEqualTo(BBox.fromPoints(
                municipality.bounds().latitudeFrom(),
                municipality.bounds().longitudeFrom(),
                municipality.bounds().latitudeTo(),
                municipality.bounds().longitudeTo()
        ));
        assertThat(accessibilityRequest.maxSearchDistanceInMeters()).isEqualTo(municipality.searchDistanceInMetres().doubleValue());
        assertThat(accessibilityRequest.startLocationLatitude()).isEqualTo(municipality.startCoordinateLatitude());
        assertThat(accessibilityRequest.startLocationLongitude()).isEqualTo(municipality.startCoordinateLongitude());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void build_invalidType() {

        assertThatThrownBy(() -> accessibilityRequestBuilderMunicipalityAreaMapper
                .build(AccessibilityRequest.builder(), mock(AreaRequestJson.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AreaRequestJson must be of type MunicipalityAreaRequestJson");
    }

    @Test
    void build_municipalityNotFound() {

        MunicipalityAreaRequestJson municipalityAreaRequestJson = MunicipalityAreaRequestJson.builder()
                .id("GM0001")
                .build();

        AccessibilityRequestBuilder accessibilityRequestBuilder = AccessibilityRequest.builder();

        try {
            accessibilityRequestBuilderMunicipalityAreaMapper.build(accessibilityRequestBuilder, municipalityAreaRequestJson);
        } catch (ApiException exception) {
            assertThat(exception.getErrorId()).isEqualTo(UUID.fromString("06d84c7c-7be2-4f79-a8fd-00264f06267d"));
            assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(exception.getTitle()).isEqualTo("Invalid municipalityId");
            assertThat(exception.getDescription()).isEqualTo("Municipality with id 'GM0001' not found.");
        }
    }

    @Test
    void canProcessAreaRequest() {

        assertThat(accessibilityRequestBuilderMunicipalityAreaMapper.canProcessAreaRequest(mock(MunicipalityAreaRequestJson.class)))
                .isTrue();
    }

    @Test
    void canProcessAreaRequest_false() {

        assertThat(accessibilityRequestBuilderMunicipalityAreaMapper.canProcessAreaRequest(mock(AreaRequestJson.class)))
                .isFalse();
    }
}
