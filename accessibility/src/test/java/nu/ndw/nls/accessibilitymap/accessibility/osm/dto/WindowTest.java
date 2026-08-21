package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class WindowTest {
    @Test
    void matches() {
        Window window = new Window(
                List.of(new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.OCTOBER, 5))),
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SUNDAY),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));

        // time checks
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T06:00"), LocalDateTime.parse("2025-04-01T08:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T06:00"), LocalDateTime.parse("2025-04-01T09:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T09:00"), LocalDateTime.parse("2025-04-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T17:00"), LocalDateTime.parse("2025-04-01T23:59"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T17:01"), LocalDateTime.parse("2025-04-01T23:59"))).isFalse();

        // days
        assertThat(window.matches(LocalDateTime.parse("2025-03-31T09:00"), LocalDateTime.parse("2025-03-31T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T09:00"), LocalDateTime.parse("2025-04-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-02T09:00"), LocalDateTime.parse("2025-04-02T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-03T09:00"), LocalDateTime.parse("2025-04-03T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-04T09:00"), LocalDateTime.parse("2025-04-04T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-05T09:00"), LocalDateTime.parse("2025-04-05T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-06T09:00"), LocalDateTime.parse("2025-04-06T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-07T09:00"), LocalDateTime.parse("2025-04-07T17:00"))).isTrue(); //monday
        assertThat(window.matches(LocalDateTime.parse("2025-10-05T09:00"), LocalDateTime.parse("2025-10-05T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-10-06T09:00"), LocalDateTime.parse("2025-10-06T17:00"))).isFalse();

        //  months
        assertThat(window.matches(LocalDateTime.parse("2025-01-01T09:00"), LocalDateTime.parse("2025-01-01T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-02-01T09:00"), LocalDateTime.parse("2025-02-01T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-03-01T09:00"), LocalDateTime.parse("2025-03-01T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T09:00"), LocalDateTime.parse("2025-04-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-05-02T09:00"), LocalDateTime.parse("2025-05-02T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-06-01T09:00"), LocalDateTime.parse("2025-06-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-07-01T09:00"), LocalDateTime.parse("2025-07-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-08-01T09:00"), LocalDateTime.parse("2025-08-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-09-01T09:00"), LocalDateTime.parse("2025-09-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-10-01T09:00"), LocalDateTime.parse("2025-10-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-11-01T09:00"), LocalDateTime.parse("2025-11-01T17:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-12-01T09:00"), LocalDateTime.parse("2025-12-01T17:00"))).isFalse();
    }

    @Test
    void matches_withoutDateRangesAndWithoutDays() {
        Window window = new Window(List.of(), EnumSet.noneOf(DayOfWeek.class), LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(window.matches(LocalDateTime.parse("2025-01-01T09:00"), LocalDateTime.parse("2025-01-01T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-07-13T09:00"), LocalDateTime.parse("2025-07-13T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-12-31T17:01"), LocalDateTime.parse("2025-12-31T23:59"))).isFalse();
    }

    @Test
    void matches_withoutDays() {
        Window window = new Window(
                List.of(new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.APRIL, 30))),
                EnumSet.noneOf(DayOfWeek.class),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0));

        assertThat(window.matches(LocalDateTime.parse("2025-04-05T09:00"), LocalDateTime.parse("2025-04-05T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-06T09:00"), LocalDateTime.parse("2025-04-06T17:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-05-01T09:00"), LocalDateTime.parse("2025-05-01T17:00"))).isFalse();
    }

    @Test
    void matches_wholeDayWindow() {
        Window window = new Window(
                List.of(new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.APRIL, 30))),
                EnumSet.of(DayOfWeek.SATURDAY),
                LocalTime.MIDNIGHT,
                LocalTime.MIDNIGHT);

        assertThat(window.matches(LocalDateTime.parse("2025-04-05T00:00"), LocalDateTime.parse("2025-04-05T00:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-05T13:37"), LocalDateTime.parse("2025-04-05T13:37"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-04T13:37"), LocalDateTime.parse("2025-04-04T13:37"))).isFalse();
    }

    @Test
    void matches_windowOverMidnight() {
        Window window = new Window(List.of(), EnumSet.noneOf(DayOfWeek.class), LocalTime.of(22, 0), LocalTime.of(6, 0));

        assertThat(window.matches(LocalDateTime.parse("2025-04-01T06:01"), LocalDateTime.parse("2025-04-01T21:59"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T06:01"), LocalDateTime.parse("2025-04-02T21:59"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T22:00"), LocalDateTime.parse("2025-04-02T06:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T21:59"), LocalDateTime.parse("2025-04-02T06:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-01T22:00"), LocalDateTime.parse("2025-04-02T06:01"))).isTrue();
    }

    @Test
    void matches_spanningMultipleDays() {
        Window window = new Window(List.of(), EnumSet.of(DayOfWeek.MONDAY), LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThat(window.matches(LocalDateTime.parse("2025-04-06T18:00"), LocalDateTime.parse("2025-04-08T08:00"))).isTrue();
        assertThat(window.matches(LocalDateTime.parse("2025-04-05T18:00"), LocalDateTime.parse("2025-04-06T08:00"))).isFalse();
        assertThat(window.matches(LocalDateTime.parse("2025-04-08T18:00"), LocalDateTime.parse("2025-04-13T08:00"))).isFalse();
    }
}
