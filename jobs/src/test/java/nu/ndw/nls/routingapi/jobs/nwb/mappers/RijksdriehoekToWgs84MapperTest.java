package nu.ndw.nls.routingapi.jobs.nwb.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

class RijksdriehoekToWgs84MapperTest {

    private static final String RESULT_WGS84 = "LINESTRING ("
            + "3.3139550978689503 47.97486314954915,"
            + " 3.3136802664908767 47.975037719073214,"
            + " 3.314077661601135 47.975134977187615)";
    private static final String INPUT_LINESTRING_RIJKSDRIEHOEK = "LINESTRING (30 10, 10 30, 40 40)";
    private final RijksdriehoekToWgs84Mapper rijksdriehoekToWgs84Mapper = new RijksdriehoekToWgs84Mapper();

    @Test
    void map_ok() throws ParseException {
        WKTReader wktReader = new WKTReader();
        LineString rijksdriehoekLineString = (LineString) wktReader.read(INPUT_LINESTRING_RIJKSDRIEHOEK);

        LineString lineStringWgs84 = rijksdriehoekToWgs84Mapper.map(rijksdriehoekLineString);
        assertEquals(RESULT_WGS84, lineStringWgs84.toString());
    }
}
