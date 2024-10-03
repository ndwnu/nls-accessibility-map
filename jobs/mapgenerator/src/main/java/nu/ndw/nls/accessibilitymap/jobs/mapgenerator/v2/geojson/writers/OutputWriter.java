package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.geojson.writers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;

public interface OutputWriter {

    void writeToFile(Accessibility roadSections, MapGenerationProperties properties);
    OutputFormat getOutputFormat();
}
