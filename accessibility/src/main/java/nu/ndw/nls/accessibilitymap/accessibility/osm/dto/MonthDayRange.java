package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import java.time.LocalDate;
import java.time.MonthDay;

public record MonthDayRange(MonthDay start, MonthDay end) {

    public boolean contains(LocalDate date) {
        return contains(MonthDay.from(date));
    }

    public boolean contains(MonthDay monthDay) {
        if (start.compareTo(end) <= 0) {
            return monthDay.compareTo(start) >= 0 && monthDay.compareTo(end) <= 0;
        }

        return monthDay.compareTo(start) >= 0 || monthDay.compareTo(end) <= 0;
    }
}
