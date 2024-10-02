package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.writers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.OutputFormat;

public interface OutputWriter {

    void writeToFile(Accessibility roadSections, MapGenerationProperties properties);
    OutputFormat getOutputFormat();
}
