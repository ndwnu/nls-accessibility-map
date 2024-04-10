package nu.ndw.nls.accessibilitymap.backend.services;

import nu.ndw.nls.accessibilitymap.backend.model.Municipality;
import nu.ndw.nls.accessibilitymap.backend.model.MunicipalityBoundingBox;
import nu.ndw.nls.routingmapmatcher.model.IsochroneMatch;

public final class TestHelper {

    public static final String MUNICIPALITY_ID = "GM0307";
    public static final Municipality MUNICIPALITY = new Municipality(5.0,
            52.0,
            50000,
            MUNICIPALITY_ID,
            "Test",
            "http://iets-met-vergunningen.nl",
            new MunicipalityBoundingBox(1.0, 1.1, 2.1, 2.2));

    public static final int ID_1 = 1;
    public static final int ID_2 = 2;

    public static final IsochroneMatch INACCESSIBLE_MATCH = IsochroneMatch
            .builder()
            .reversed(false)
            .matchedLinkId(ID_2)
            .build();
    public static final IsochroneMatch ACCESSIBLE_MATCH = IsochroneMatch
            .builder()
            .reversed(false)
            .matchedLinkId(ID_1)
            .build();

    private TestHelper() {
    }
}
