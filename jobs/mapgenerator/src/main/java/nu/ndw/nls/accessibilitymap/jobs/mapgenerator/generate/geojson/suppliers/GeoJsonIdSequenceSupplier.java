package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.generate.geojson.suppliers;

public class GeoJsonIdSequenceSupplier {

    private long i = 1;

    public long next() {
        return i++;
    }
}
