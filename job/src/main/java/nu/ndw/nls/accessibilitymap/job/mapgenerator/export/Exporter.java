package nu.ndw.nls.accessibilitymap.job.mapgenerator.export;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.accessibility.core.dto.accessibility.Accessibility;
import nu.ndw.nls.accessibilitymap.job.mapgenerator.command.dto.ExportProperties;

public interface Exporter {

    void export(Accessibility accessibility, ExportProperties exportProperties);

    boolean isEnabled(Set<ExportType> exportTypes);
}
