package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier;

import java.time.Instant;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.dto.VersionStatus;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.VersionDtoSupplier;

@RequiredArgsConstructor
public class AccessibilityVersionSupplier extends VersionDtoSupplier {

    private final String version;

    @Override
    public NwbVersionDto create() {
        return create(version);
    }

    public static NwbVersionDto create(String version) {
        int versionId = Integer.parseInt(version.replace("-", ""));

        return NwbVersionDto.builder()
                .revision(Instant.now())
                .versionId(versionId)
                .status(VersionStatus.OK)
                .referenceDate(LocalDate.parse(version))
                .imported(Instant.now())
                .build();
    }
}
