package nu.ndw.nls.accessibilitymap.backend.municipality.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.LocalDate;
import nu.ndw.nls.accessibilitymap.backend.core.dto.response.MunicipalityBoundingBox;
import nu.ndw.nls.accessibilitymap.backend.municipality.MunicipalityProperty;
import nu.ndw.nls.accessibilitymap.backend.municipality.model.Municipality;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MunicipalityMapperTest {

    private static final double START_COORDINATE_LONGITUDE = 1D;

    private static final double START_COORDINATE_LATITUDE = 2D;

    private static final java.net.URL URL;

    private static final double SEARCH_DISTANCE_IN_METRES = 3D;

    private static final String MUNICIPALITY_ID_STRING = "123";

    private static final String NAME = "name";

    private static final int MUNICIPALITY_ID = 123;

    private static final LocalDate DATE_LAST_CHECK = LocalDate.of(2024, 7, 11);

    static {
        try {
            URL = URI.create("http://example.com").toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Mock
    private MunicipalityCoordinateMapper municipalityCoordinateMapper;

    @Mock
    private MunicipalityIdMapper municipalityIdMapper;

    private MunicipalityMapper municipalityMapper;

    @Mock
    private MunicipalityBoundingBox municipalityBoundingBox;

    @Mock
    private Point point;

    @BeforeEach
    void setUp() {
        municipalityMapper = new MunicipalityMapperImpl(municipalityCoordinateMapper, municipalityIdMapper);
    }

    @Test
    void map() {
        MunicipalityProperty municipalityProperty = new MunicipalityProperty(NAME, START_COORDINATE_LONGITUDE,
                START_COORDINATE_LATITUDE, SEARCH_DISTANCE_IN_METRES, MUNICIPALITY_ID_STRING,
                URL, municipalityBoundingBox, DATE_LAST_CHECK);

        when(municipalityCoordinateMapper.map(municipalityProperty)).thenReturn(point);
        when(municipalityIdMapper.map(MUNICIPALITY_ID_STRING)).thenReturn(MUNICIPALITY_ID);

        Municipality municipality = municipalityMapper.map(municipalityProperty);

        assertEquals(NAME, municipality.getName());
        assertEquals(municipalityBoundingBox, municipality.getBounds());
        assertEquals(URL, municipality.getRequestExemptionUrl());
        assertEquals(MUNICIPALITY_ID_STRING, municipality.getMunicipalityId());
        assertEquals(MUNICIPALITY_ID, municipality.getMunicipalityIdInteger());
        assertEquals(SEARCH_DISTANCE_IN_METRES, municipality.getSearchDistanceInMetres());
        assertEquals(DATE_LAST_CHECK, municipality.getDateLastCheck());

        assertEquals(point, municipality.getStartPoint());

    }
}