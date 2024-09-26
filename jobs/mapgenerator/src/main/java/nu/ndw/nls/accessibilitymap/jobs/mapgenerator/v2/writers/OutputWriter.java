package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.writers;

import java.util.List;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.MapGenerationProperties;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.OutputFormat;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.v2.model.RoadSection;

public interface OutputWriter {

    void writeToFile(List<RoadSection> roadSections, MapGenerationProperties properties);
    OutputFormat getOutputFormat();
}
