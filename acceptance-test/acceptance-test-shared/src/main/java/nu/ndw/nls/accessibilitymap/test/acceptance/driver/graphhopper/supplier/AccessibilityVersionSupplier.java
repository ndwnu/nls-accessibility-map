package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.VersionDtoSupplier;

@RequiredArgsConstructor
public class AccessibilityVersionSupplier extends VersionDtoSupplier {

    private final List<NwbVersionDto> versionDtos;

    @Override
    public NwbVersionDto create() {
        return versionDtos.getFirst();
    }

    public List<NwbVersionDto> createList() {
        return versionDtos;
    }
}
