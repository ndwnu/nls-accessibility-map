package nu.ndw.nls.accessibilitymap.accessibility.osm.openinghours;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import nu.ndw.nls.accessibilitymap.accessibility.osm.dto.MonthDayRange;
import nu.ndw.nls.accessibilitymap.accessibility.osm.dto.Window;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursBaseVisitor;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursLexer;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Date_fromContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Date_offsetContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Date_toContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Extended_timeContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Hour_minutesContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.MonthContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Month_dayContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Monthday_rangeContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Monthday_selectorContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.OpeningHoursContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Rule_sequenceContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Selector_sequenceContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Small_range_selectorContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.TimespanContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.WdayContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Weekday_rangeContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Weekday_selectorContext;
import nu.ndw.nls.accessibilitymap.osm.openinghours.OpeningHoursParser.Wide_range_selectorContext;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Only supports the subset of the OSM opening_hours specification needed to express traffic sign time validity: optional month range,
 * optional weekday range and a simple time range. Years, weeks, holidays, nth-weekday entries, sunrise/sunset, open-ended ranges and rule
 * modifiers (open/closed/off/unknown) are rejected so that an unsupported expression fails loudly instead of being silently
 * misinterpreted.
 */
@Slf4j
public class OpeningHoursVisitor extends OpeningHoursBaseVisitor<List<Window>> {

    private static final Map<String, Month> MONTHS_BY_ABBREVIATION = Map.ofEntries(
            Map.entry("Jan", Month.JANUARY),
            Map.entry("Feb", Month.FEBRUARY),
            Map.entry("Mar", Month.MARCH),
            Map.entry("Apr", Month.APRIL),
            Map.entry("May", Month.MAY),
            Map.entry("Jun", Month.JUNE),
            Map.entry("Jul", Month.JULY),
            Map.entry("Aug", Month.AUGUST),
            Map.entry("Sep", Month.SEPTEMBER),
            Map.entry("Oct", Month.OCTOBER),
            Map.entry("Nov", Month.NOVEMBER),
            Map.entry("Dec", Month.DECEMBER));

    private static final Map<String, DayOfWeek> DAYS_BY_ABBREVIATION = Map.of(
            "Mo", DayOfWeek.MONDAY,
            "Tu", DayOfWeek.TUESDAY,
            "We", DayOfWeek.WEDNESDAY,
            "Th", DayOfWeek.THURSDAY,
            "Fr", DayOfWeek.FRIDAY,
            "Sa", DayOfWeek.SATURDAY,
            "Su", DayOfWeek.SUNDAY);

    private static final Map<String, Optional<List<Window>>> PARSED_WINDOWS_BY_EXPRESSION = new ConcurrentHashMap<>();

    public static Optional<List<Window>> parse(String openingHoursExpression) {
        return PARSED_WINDOWS_BY_EXPRESSION.computeIfAbsent(openingHoursExpression, OpeningHoursVisitor::doParse);
    }

    static void clearCache() {
        PARSED_WINDOWS_BY_EXPRESSION.clear();
    }

    private static Optional<List<Window>> doParse(String openingHoursExpression) {
        try {
            OpeningHoursLexer openingHoursLexer = new OpeningHoursLexer(CharStreams.fromString(openingHoursExpression));
            openingHoursLexer.removeErrorListeners();
            OpeningHoursParser openingHoursParser = new OpeningHoursParser(new CommonTokenStream(openingHoursLexer));
            openingHoursParser.removeErrorListeners();
            openingHoursParser.setErrorHandler(new BailErrorStrategy());

            return Optional.of(new OpeningHoursVisitor().visit(openingHoursParser.openingHours()));
        } catch (RuntimeException runtimeException) {
            log.warn("Could not parse opening hours expression '{}'", openingHoursExpression, runtimeException);
            return Optional.empty();
        }
    }

    @Override
    public List<Window> visitOpeningHours(OpeningHoursContext ctx) {
        List<Window> windows = new ArrayList<>();
        for (Rule_sequenceContext ruleSequence : ctx.rule_sequence()) {
            windows.addAll(visitRule_sequence(ruleSequence));
        }

        return windows;
    }

    @Override
    public List<Window> visitRule_sequence(Rule_sequenceContext ctx) {
        if (ctx.rule_modifier() != null) {
            throw new UnsupportedOperationException(
                    "Rule modifiers ('open', 'closed', 'off', 'unknown' or comments) are not supported: " + ctx.getText());
        }

        return visitSelector_sequence(ctx.selector_sequence());
    }

    @Override
    public List<Window> visitSelector_sequence(Selector_sequenceContext ctx) {
        if ("24/7".equals(ctx.getText())) {
            return List.of(new Window(
                    List.of(), EnumSet.noneOf(DayOfWeek.class),
                    LocalTime.MIDNIGHT, LocalTime.MIDNIGHT));
        }

        List<MonthDayRange> dateRanges = List.of();
        if (ctx.wide_range_selector() != null) {
            dateRanges = toDateRanges(ctx.wide_range_selector());
        }

        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        List<TimespanContext> timespans = List.of();
        if (ctx.small_range_selector() != null) {
            Small_range_selectorContext smallRangeSelector = ctx.small_range_selector();
            if (smallRangeSelector.weekday_selector() != null) {
                days.addAll(toDays(smallRangeSelector.weekday_selector()));
            }
            if (smallRangeSelector.time_selector() != null) {
                timespans = smallRangeSelector.time_selector().timespan();
            }
        }

        if (timespans.isEmpty()) {
            return List.of(new Window(dateRanges, days, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT));
        }

        List<Window> windows = new ArrayList<>();
        for (TimespanContext timespan : timespans) {
            windows.add(new Window(dateRanges, days, toStartTime(timespan), toEndTime(timespan)));
        }

        return windows;
    }

    private List<MonthDayRange> toDateRanges(Wide_range_selectorContext ctx) {
        if (ctx.year_selector() != null || ctx.week_selector() != null) {
            throw new UnsupportedOperationException("Year and week selectors are not supported: " + ctx.getText());
        }

        Monthday_selectorContext monthdaySelector = ctx.monthday_selector();
        if (monthdaySelector == null) {
            throw new UnsupportedOperationException("Unsupported wide range selector: " + ctx.getText());
        }

        List<MonthDayRange> dateRanges = new ArrayList<>();
        for (Monthday_rangeContext monthdayRange : monthdaySelector.monthday_range()) {
            dateRanges.add(toDateRange(monthdayRange));
        }

        return List.copyOf(dateRanges);
    }

    private MonthDayRange toDateRange(Monthday_rangeContext ctx) {
        validateSupportedMonthdayRange(ctx);

        Date_fromContext dateFrom = ctx.date_from();
        if (dateFrom == null) {
            List<MonthContext> monthContexts = ctx.month();
            Month fromMonth = toMonth(monthContexts.getFirst());
            Month toMonth = monthContexts.size() > 1 ? toMonth(monthContexts.get(1)) : fromMonth;

            return new MonthDayRange(MonthDay.of(fromMonth, 1), MonthDay.of(toMonth, toMonth.maxLength()));
        }

        Month fromMonth = toMonth(dateFrom.month());
        MonthDay from = toMonthDay(fromMonth, dateFrom.month_day(), ctx);

        // The grammar exposes the trailing plus of an open ended range only as unlabelled text, never as an accessor.
        if (ctx.getText().endsWith("+")) {
            return new MonthDayRange(from, MonthDay.of(Month.DECEMBER, 31));
        }

        Date_toContext dateTo = ctx.date_to();
        if (dateTo == null) {
            return new MonthDayRange(from, from);
        }

        if (dateTo.date_from() != null) {
            Month toMonth = toMonth(dateTo.date_from().month());
            return new MonthDayRange(from, toMonthDay(toMonth, dateTo.date_from().month_day(), ctx));
        }

        // A bare day of month on the right hand side ('Apr 01-15') repeats the month of the left hand side.
        return new MonthDayRange(from, toMonthDay(fromMonth, dateTo.month_day(), ctx));
    }

    private void validateSupportedMonthdayRange(Monthday_rangeContext ctx) {
        // A date offset can match the empty string, so its context always exists and emptiness is the only reliable check.
        for (Date_offsetContext dateOffset : ctx.date_offset()) {
            if (dateOffset.getChildCount() > 0) {
                throw new UnsupportedOperationException("Date offsets are not supported: " + ctx.getText());
            }
        }

        if (ctx.year() != null) {
            throw new UnsupportedOperationException("Year qualified dates are not supported: " + ctx.getText());
        }

        validateSupportedDateFrom(ctx.date_from(), ctx);
        if (ctx.date_to() != null) {
            validateSupportedDateFrom(ctx.date_to().date_from(), ctx);
        }
    }

    private void validateSupportedDateFrom(Date_fromContext ctx, Monthday_rangeContext monthdayRange) {
        if (ctx == null) {
            return;
        }

        if (ctx.variable_date() != null) {
            throw new UnsupportedOperationException("Variable dates such as 'easter' are not supported: " + monthdayRange.getText());
        }

        if (ctx.year() != null) {
            throw new UnsupportedOperationException("Year qualified dates are not supported: " + monthdayRange.getText());
        }
    }

    private MonthDay toMonthDay(Month month, Month_dayContext ctx, Monthday_rangeContext monthdayRange) {
        int dayOfMonth = Integer.parseInt(ctx.getText());
        try {
            return MonthDay.of(month, dayOfMonth);
        } catch (DateTimeException dateTimeException) {
            throw new UnsupportedOperationException(
                    "Day of month %d does not exist in %s: %s".formatted(dayOfMonth, month, monthdayRange.getText()),
                    dateTimeException);
        }
    }

    private EnumSet<DayOfWeek> toDays(Weekday_selectorContext ctx) {
        if (ctx.holiday_sequence() != null) {
            throw new UnsupportedOperationException("Holiday selectors are not supported: " + ctx.getText());
        }

        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (Weekday_rangeContext weekdayRange : ctx.weekday_sequence().weekday_range()) {
            // The grammar only allows a day offset behind an nth entry, so checking the nth entries covers both.
            if (!weekdayRange.nth_entry().isEmpty()) {
                throw new UnsupportedOperationException("Nth weekday entries and day offsets are not supported: " + ctx.getText());
            }

            List<WdayContext> wdayContexts = weekdayRange.wday();
            DayOfWeek from = toDay(wdayContexts.get(0));
            DayOfWeek to = wdayContexts.size() > 1 ? toDay(wdayContexts.get(1)) : from;
            days.addAll(dayRange(from, to));
        }

        return days;
    }

    private void validateSimpleTimespan(TimespanContext ctx) {
        if (ctx.extended_time() == null || ctx.number() != null || ctx.getText().endsWith("+")) {
            throw new UnsupportedOperationException(
                    "Only simple time ranges (e.g. '09:00-17:00') are supported: " + ctx.getText());
        }

        if (ctx.time().variable_time() != null || ctx.extended_time().variable_time() != null) {
            throw new UnsupportedOperationException("Sunrise/sunset relative times are not supported: " + ctx.getText());
        }
    }

    private LocalTime toStartTime(TimespanContext ctx) {
        validateSimpleTimespan(ctx);
        return toLocalTime(ctx.time().hour_minutes());
    }

    private LocalTime toEndTime(TimespanContext ctx) {
        validateSimpleTimespan(ctx);
        Extended_timeContext extendedTime = ctx.extended_time();
        int hour = Integer.parseInt(extendedTime.extended_hour().getText()) % 24;
        int minute = Integer.parseInt(extendedTime.minute().getText());

        return LocalTime.of(hour, minute);
    }

    private LocalTime toLocalTime(Hour_minutesContext ctx) {
        int hour = Integer.parseInt(ctx.hour().getText());
        int minute = Integer.parseInt(ctx.minute().getText());

        return LocalTime.of(hour, minute);
    }

    private EnumSet<DayOfWeek> dayRange(DayOfWeek from, DayOfWeek to) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        DayOfWeek day = from;
        while (true) {
            days.add(day);
            if (day == to) {
                break;
            }
            day = day.plus(1);
        }

        return days;
    }

    private Month toMonth(MonthContext ctx) {
        return MONTHS_BY_ABBREVIATION.get(ctx.getText());
    }

    private DayOfWeek toDay(WdayContext ctx) {
        return DAYS_BY_ABBREVIATION.get(ctx.getText());
    }
}
