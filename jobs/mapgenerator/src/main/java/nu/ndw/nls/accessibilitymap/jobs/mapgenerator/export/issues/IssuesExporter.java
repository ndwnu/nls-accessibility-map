package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.issues;

import java.util.Set;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.ExportProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.ExportType;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.export.Exporter;
import org.springframework.stereotype.Component;

@Component
public class IssuesExporter implements Exporter {

    @Override
    public void export(Accessibility accessibility, ExportProperties exportProperties) {
        // to be implemented
    }

    @Override
    public boolean isEnabled(Set<ExportType> exportTypes) {
        return exportTypes.contains(ExportType.ASYMMETRIC_TRAFFIC_SIGNS_ISSUES);
    }
}
