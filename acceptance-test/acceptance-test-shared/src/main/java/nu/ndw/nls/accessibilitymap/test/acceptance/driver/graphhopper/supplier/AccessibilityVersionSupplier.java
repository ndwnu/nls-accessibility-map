package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.VersionDtoSupplier;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

@RequiredArgsConstructor
public class AccessibilityVersionSupplier extends VersionDtoSupplier {

    private final List<ImmutablePair<Boolean, NwbVersionDto>> versionDtos;

    @Override
    public NwbVersionDto create() {
        return versionDtos.stream()
                .filter(Pair::getLeft)
                .findFirst().map(Pair::getRight)
                .orElseThrow(() -> new IllegalStateException("No current version"));
    }

    public List<NwbVersionDto> createList() {
        return versionDtos.stream()
                .map(Pair::getRight)
                .toList();
    }
}
