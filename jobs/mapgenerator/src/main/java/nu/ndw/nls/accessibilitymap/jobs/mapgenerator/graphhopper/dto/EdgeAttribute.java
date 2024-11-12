package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.graphhopper.dto;

import lombok.Builder;

@Builder
public record EdgeAttribute(String key, Object value) {

}
