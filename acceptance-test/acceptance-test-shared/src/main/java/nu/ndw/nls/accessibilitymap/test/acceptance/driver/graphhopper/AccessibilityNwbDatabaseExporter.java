package nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper;

import nu.ndw.nls.accessibilitymap.test.acceptance.driver.graphhopper.supplier.AccessibilityVersionSupplier;
import nu.ndw.nls.data.api.nwb.dtos.NwbVersionDto;
import nu.ndw.nls.db.nwb.jooq.repositories.JooqNwbRoadSectionCrudRepository;
import nu.ndw.nls.db.nwb.jooq.repositories.JooqNwbVersionCrudRepository;
import nu.ndw.nls.springboot.test.graph.dto.Graph;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.NwbDataAccessSettings;
import nu.ndw.nls.springboot.test.graph.exporter.database.nwb.dto.supplier.VersionDtoSupplier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AccessibilityNwbDatabaseExporter extends nu.ndw.nls.springboot.test.graph.exporter.database.nwb.NwbDatabaseExporter {

    private final JooqNwbVersionCrudRepository versionRepository;

    private final JooqNwbRoadSectionCrudRepository roadSectionRepository;

    public AccessibilityNwbDatabaseExporter(JooqNwbVersionCrudRepository versionRepository,
            JooqNwbRoadSectionCrudRepository roadSectionRepository
    ) {
        super(versionRepository, roadSectionRepository);
        this.versionRepository = versionRepository;
        this.roadSectionRepository = roadSectionRepository;
    }

    public void export(Graph graph, NwbDataAccessSettings nwbDataAccessSettings) {
        VersionDtoSupplier versionDtoSupplier = nwbDataAccessSettings.getVersionDtoSupplier();
        if (versionDtoSupplier instanceof AccessibilityVersionSupplier accessibilityVersionSupplier) {
            accessibilityVersionSupplier.createList().forEach(versionRepository::insert);
            NwbVersionDto networkVersion = accessibilityVersionSupplier.create();
            graph.getEdges()
                    .forEach(link -> roadSectionRepository.insert(nwbDataAccessSettings.getNwbRoadSectionDtoSupplier()
                            .create(link, networkVersion)));
        } else {
            NwbVersionDto version = nwbDataAccessSettings.getVersionDtoSupplier().create();
            versionRepository.insert(version);
            graph.getEdges()
                    .forEach(link -> roadSectionRepository.insert(nwbDataAccessSettings.getNwbRoadSectionDtoSupplier()
                            .create(link, version)));
        }
    }
}
