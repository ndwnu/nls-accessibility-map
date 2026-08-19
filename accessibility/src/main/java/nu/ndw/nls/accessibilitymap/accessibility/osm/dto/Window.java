package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public record Window(List<MonthDayRange> dateRanges, EnumSet<DayOfWeek> days, LocalTime start, LocalTime end) {

    /**
     * True if the closed interval [start, end] overlaps this window on at least one instant - a visit that only
     * partially covers the window, or that touches it on a single boundary, still matches.
     */
    public boolean matches(LocalDateTime start, LocalDateTime end) {
        LocalDate date = start.toLocalDate();
        LocalDate lastDate = end.toLocalDate();

        while (!date.isAfter(lastDate)) {
            LocalTime rangeStart = date.equals(start.toLocalDate()) ? start.toLocalTime() : LocalTime.MIDNIGHT;
            LocalTime rangeEnd = date.equals(lastDate) ? end.toLocalTime() : LocalTime.MAX;
            if (matchesDate(date) && overlapsTimeRange(rangeStart, rangeEnd)) {
                return true;
            }

            date = date.plusDays(1);
        }

        return false;
    }

    private boolean matchesDate(LocalDate date) {
        if (!dateRanges.isEmpty() && dateRanges.stream().noneMatch(dateRange -> dateRange.contains(date))) {
            return false;
        }

        return days.isEmpty() || days.contains(date.getDayOfWeek());
    }

    private boolean overlapsTimeRange(LocalTime rangeStart, LocalTime rangeEnd) {
        if (start.equals(end)) {
            return true;
        }

        if (start.isBefore(end)) {
            return !rangeStart.isAfter(end) && !rangeEnd.isBefore(start);
        }

        // A window whose end lies before its start runs over midnight, so it is the union of an evening part
        // [start, end of day] and a morning part [start of day, end]; overlapping either one is enough.
        return !rangeStart.isAfter(end) || !rangeEnd.isBefore(start);
    }
}
