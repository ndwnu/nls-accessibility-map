package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public record Window(List<MonthDayRange> dateRanges, EnumSet<DayOfWeek> days, LocalTime start, LocalTime end) {

    /**
     * @param start window start time is treated as inclusive
     * @param end   window end time is treated as inclusive
     * @return True if the closed interval [start, end] overlaps this window on at least one instant - a visit that only partially covers
     * the window, or that touches it on a single boundary, still matches.
     */
    public boolean matches(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!startDate.isAfter(endDate)) {
            LocalTime startTime = startDate.equals(start.toLocalDate()) ? start.toLocalTime() : LocalTime.MIDNIGHT;
            LocalTime endTime = startDate.equals(endDate) ? end.toLocalTime() : LocalTime.MAX;
            if (matchesDate(startDate) && overlapsTimeRange(startTime, endTime)) {
                return true;
            }

            startDate = startDate.plusDays(1);
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

        return !rangeStart.isAfter(end) || !rangeEnd.isBefore(start);
    }
}
