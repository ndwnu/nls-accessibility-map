package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public record Window(List<MonthDayRange> dateRanges, EnumSet<DayOfWeek> days, LocalTime start, LocalTime end) {

    /**
     * @param startInclusive window start time is treated as inclusive
     * @param endInclusive   window end time is treated as inclusive
     * @return True if the closed interval [start, end] overlaps this window on at least one instant - a visit that only partially covers
     * the window, or that touches it on a single boundary, still matches.
     */
    public boolean matches(LocalDateTime startInclusive, LocalDateTime endInclusive) {
        LocalDate startDateInclusive = startInclusive.toLocalDate();
        LocalDate endDateInclusive = endInclusive.toLocalDate();

        while (!startDateInclusive.isAfter(endDateInclusive)) {
            LocalTime startTimeInclusive = startDateInclusive.equals(startInclusive.toLocalDate())
                    ? startInclusive.toLocalTime()
                    : LocalTime.MIDNIGHT;
            LocalTime endTimeInclusive = startDateInclusive.equals(endDateInclusive)
                    ? endInclusive.toLocalTime()
                    : LocalTime.MAX;

            if (matchesDate(startDateInclusive) && overlapsTimeRange(startTimeInclusive, endTimeInclusive)) {
                return true;
            }

            startDateInclusive = startDateInclusive.plusDays(1);
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
