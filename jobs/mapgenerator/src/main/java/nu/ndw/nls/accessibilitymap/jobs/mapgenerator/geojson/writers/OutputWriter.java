package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.geojson.writers;

import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.accessibility.dto.Accessibility;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.command.dto.GeoGenerationProperties;

public interface OutputWriter {

    void writeToFile(Accessibility roadSections, GeoGenerationProperties properties);
}
