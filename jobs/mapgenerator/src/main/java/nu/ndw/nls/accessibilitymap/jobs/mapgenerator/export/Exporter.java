package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;

public interface Exporter {

    void export(Accessibility accessibility, ExportProperties exportProperties);

    boolean isEnabled(Set<ExportType> exportTypes);
}
