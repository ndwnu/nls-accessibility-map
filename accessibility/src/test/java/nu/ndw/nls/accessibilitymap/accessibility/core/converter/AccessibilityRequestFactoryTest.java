package nu.ndw.nls.accessibilitymap.accessibility.core.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.TransportType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequest;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.request.AccessibilityRequestFactory;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.Restrictions;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSign;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.trafficsign.TrafficSignType;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.value.Maximum;
import nu.ndw.nls.accessibilitymap.accessibility.trafficsign.mappers.TrafficSignRestrictionsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccessibilityRequestFactoryTest {

    private AccessibilityRequestFactory accessibilityRequestFactory;

    @Mock
    private TrafficSignRestrictionsBuilder trafficSignRestrictionsBuilder;

    private Restrictions restrictions1;

    private Restrictions restrictions2;

    private Restrictions restrictionsEmpty;

    @BeforeEach
    void setUp() {
        restrictions1 = Restrictions.builder()
                .transportTypes(List.of(TransportType.CAR))
                .vehicleLengthInCm(Maximum.builder().value(1d).build())
                .vehicleWidthInCm(Maximum.builder().value(2d).build())
                .vehicleHeightInCm(Maximum.builder().value(3d).build())
                .vehicleWeightInKg(Maximum.builder().value(4d).build())
                .vehicleAxleLoadInKg(Maximum.builder().value(5d).build())
                .build();

        restrictions2 = Restrictions.builder()
                .transportTypes(List.of(TransportType.TRUCK))
                .vehicleLengthInCm(Maximum.builder().value(100d).build())
                .vehicleWidthInCm(Maximum.builder().value(100d).build())
                .vehicleHeightInCm(Maximum.builder().value(100d).build())
                .vehicleWeightInKg(Maximum.builder().value(100d).build())
                .vehicleAxleLoadInKg(Maximum.builder().value(100d).build())
                .build();

        restrictionsEmpty = Restrictions.builder().build();

        accessibilityRequestFactory = new AccessibilityRequestFactory(trafficSignRestrictionsBuilder);
    }

    @Test
    void convert() {

        when(trafficSignRestrictionsBuilder.buildFor(any())).thenAnswer(answer -> {
            TrafficSign trafficSign = answer.getArgument(0);
            if (trafficSign.trafficSignType().equals(TrafficSignType.C7)) {
                return restrictions1;
            } else if (trafficSign.trafficSignType().equals(TrafficSignType.C12)) {
                return restrictionsEmpty;
            } else if (trafficSign.trafficSignType().equals(TrafficSignType.C17)) {
                return restrictions2;
            }
            return null;
        });

        AccessibilityRequest accessibilityRequest = accessibilityRequestFactory.create(List.of(
                TrafficSignType.C7,
                TrafficSignType.C12,
                TrafficSignType.C17), analyserConfiguration.startLocationLatitude(), analyserConfiguration.startLocationLatitude(),
                analyserConfiguration.searchRadiusInMeters());

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .transportTypes(List.of(TransportType.CAR, TransportType.TRUCK))
                .vehicleLengthInCm(1d)
                .vehicleWidthInCm(2d)
                .vehicleHeightInCm(3d)
                .vehicleWeightInKg(4d)
                .vehicleAxleLoadInKg(5d)
                .build());
    }

    @Test
    void convert_missingValues() {

        when(trafficSignRestrictionsBuilder.buildFor(any())).thenAnswer(answer -> {
            TrafficSign trafficSign = answer.getArgument(0);
            if (trafficSign.trafficSignType().equals(TrafficSignType.C7)) {
                return restrictionsEmpty;
            }
            return null;
        });

        AccessibilityRequest accessibilityRequest = accessibilityRequestFactory.create(List.of(TrafficSignType.C7),
                analyserConfiguration.startLocationLatitude(), analyserConfiguration.startLocationLatitude(),
                analyserConfiguration.searchRadiusInMeters());

        assertThat(accessibilityRequest).isEqualTo(AccessibilityRequest.builder()
                .transportTypes(List.of())
                .build());
    }
}