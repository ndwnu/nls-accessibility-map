package nu.ndw.nls.accessibilitymap.backend.mappers;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import nu.ndw.nls.routingmapmatcher.network.NetworkGraphHopper;
import org.springframework.stereotype.Component;

@Component
public class GraphhopperVersionMapper {

    public int map(NetworkGraphHopper networkGraphHopper) {
        LocalDate localDate = networkGraphHopper.getDataDate().atOffset(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1);
        return Integer.parseInt(localDate.format(DateTimeFormatter.BASIC_ISO_DATE));
    }

}
