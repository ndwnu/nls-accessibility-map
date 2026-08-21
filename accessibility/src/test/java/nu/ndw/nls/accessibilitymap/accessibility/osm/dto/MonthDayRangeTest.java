package nu.ndw.nls.accessibilitymap.accessibility.osm.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import org.junit.jupiter.api.Test;

class MonthDayRangeTest {

    @Test
    void contains_monthDay() {
        MonthDayRange monthDayRange = new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.OCTOBER, 5));

        assertThat(monthDayRange.contains(MonthDay.of(Month.JANUARY, 1))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.MARCH, 31))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.APRIL, 1))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.APRIL, 2))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.JULY, 13))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.OCTOBER, 4))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.OCTOBER, 5))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.OCTOBER, 6))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.DECEMBER, 31))).isFalse();
    }

    @Test
    void contains_localDate() {
        MonthDayRange monthDayRange = new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.OCTOBER, 5));

        assertThat(monthDayRange.contains(LocalDate.parse("2025-03-31"))).isFalse();
        assertThat(monthDayRange.contains(LocalDate.parse("2025-04-01"))).isTrue();
        assertThat(monthDayRange.contains(LocalDate.parse("2025-10-05"))).isTrue();
        assertThat(monthDayRange.contains(LocalDate.parse("2025-10-06"))).isFalse();
        assertThat(monthDayRange.contains(LocalDate.parse("2024-07-13"))).isTrue();
    }

    @Test
    void contains_rangeOverEndOfYear() {
        MonthDayRange monthDayRange = new MonthDayRange(MonthDay.of(Month.NOVEMBER, 1), MonthDay.of(Month.MARCH, 15));

        assertThat(monthDayRange.contains(MonthDay.of(Month.OCTOBER, 31))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.NOVEMBER, 1))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.DECEMBER, 31))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.JANUARY, 1))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.MARCH, 15))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.MARCH, 16))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.JULY, 13))).isFalse();
    }

    @Test
    void contains_singleDayRange() {
        MonthDayRange monthDayRange = new MonthDayRange(MonthDay.of(Month.APRIL, 1), MonthDay.of(Month.APRIL, 1));

        assertThat(monthDayRange.contains(MonthDay.of(Month.MARCH, 31))).isFalse();
        assertThat(monthDayRange.contains(MonthDay.of(Month.APRIL, 1))).isTrue();
        assertThat(monthDayRange.contains(MonthDay.of(Month.APRIL, 2))).isFalse();
    }

    @Test
    void contains_leapDay() {
        MonthDayRange monthDayRange = new MonthDayRange(MonthDay.of(Month.FEBRUARY, 1), MonthDay.of(Month.FEBRUARY, 29));

        assertThat(monthDayRange.contains(MonthDay.of(Month.FEBRUARY, 29))).isTrue();
        assertThat(monthDayRange.contains(LocalDate.parse("2024-02-29"))).isTrue();
        assertThat(monthDayRange.contains(LocalDate.parse("2025-02-28"))).isTrue();
        assertThat(monthDayRange.contains(LocalDate.parse("2025-03-01"))).isFalse();
    }
}
