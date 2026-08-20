package nu.ndw.nls.accessibilitymap.accessibility.osm;

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

    public static final int END_OF_MONTH = 31;

    public static final int HOURS_IN_A_DAY = 24;

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
    public List<Window> visitOpeningHours(OpeningHoursContext context) {
        List<Window> windows = new ArrayList<>();
        for (Rule_sequenceContext ruleSequence : context.rule_sequence()) {
            windows.addAll(visitRule_sequence(ruleSequence));
        }

        return windows;
    }

    @Override
    public List<Window> visitRule_sequence(Rule_sequenceContext context) {
        if (context.rule_modifier() != null) {
            throw new UnsupportedOperationException(
                    "Rule modifiers ('open', 'closed', 'off', 'unknown' or comments) are not supported: " + context.getText());
        }

        return visitSelector_sequence(context.selector_sequence());
    }

    @Override
    public List<Window> visitSelector_sequence(Selector_sequenceContext context) {
        if ("24/7".equals(context.getText())) {
            return List.of(new Window(
                    List.of(), EnumSet.noneOf(DayOfWeek.class),
                    LocalTime.MIDNIGHT, LocalTime.MIDNIGHT));
        }

        List<MonthDayRange> dateRanges = List.of();
        if (context.wide_range_selector() != null) {
            dateRanges = toDateRanges(context.wide_range_selector());
        }

        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        List<TimespanContext> timespans = List.of();
        if (context.small_range_selector() != null) {
            Small_range_selectorContext smallRangeSelector = context.small_range_selector();
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

    private List<MonthDayRange> toDateRanges(Wide_range_selectorContext context) {
        if (context.year_selector() != null || context.week_selector() != null) {
            throw new UnsupportedOperationException("Year and week selectors are not supported: " + context.getText());
        }

        Monthday_selectorContext monthdaySelector = context.monthday_selector();
        if (monthdaySelector == null) {
            throw new UnsupportedOperationException("Unsupported wide range selector: " + context.getText());
        }

        List<MonthDayRange> dateRanges = new ArrayList<>();
        for (Monthday_rangeContext monthdayRange : monthdaySelector.monthday_range()) {
            dateRanges.add(toDateRange(monthdayRange));
        }

        return List.copyOf(dateRanges);
    }

    @SuppressWarnings("java:S1142")
    private MonthDayRange toDateRange(Monthday_rangeContext context) {
        validateSupportedMonthdayRange(context);

        Date_fromContext dateFrom = context.date_from();
        if (dateFrom == null) {
            List<MonthContext> monthContexts = context.month();
            Month fromMonth = toMonth(monthContexts.getFirst());
            Month toMonth = monthContexts.size() > 1 ? toMonth(monthContexts.get(1)) : fromMonth;

            return new MonthDayRange(MonthDay.of(fromMonth, 1), MonthDay.of(toMonth, toMonth.maxLength()));
        }

        Month fromMonth = toMonth(dateFrom.month());
        MonthDay from = toMonthDay(fromMonth, dateFrom.month_day(), context);

        // The grammar exposes the trailing plus of an open-ended range only as unlabelled text, never as an accessor.
        if (context.getText().endsWith("+")) {
            return new MonthDayRange(from, MonthDay.of(Month.DECEMBER, END_OF_MONTH));
        }

        Date_toContext dateTo = context.date_to();
        if (dateTo == null) {
            return new MonthDayRange(from, from);
        }

        if (dateTo.date_from() != null) {
            Month toMonth = toMonth(dateTo.date_from().month());
            return new MonthDayRange(from, toMonthDay(toMonth, dateTo.date_from().month_day(), context));
        }

        // A bare day of month on the right-hand side ('Apr 01-15') repeats the month of the left-hand side.
        return new MonthDayRange(from, toMonthDay(fromMonth, dateTo.month_day(), context));
    }

    private void validateSupportedMonthdayRange(Monthday_rangeContext context) {
        // A date offset can match the empty string, so its context always exists and emptiness is the only reliable check.
        for (Date_offsetContext dateOffset : context.date_offset()) {
            if (dateOffset.getChildCount() > 0) {
                throw new UnsupportedOperationException("Date offsets are not supported: " + context.getText());
            }
        }

        if (context.year() != null) {
            throw new UnsupportedOperationException("Year qualified dates are not supported: " + context.getText());
        }

        validateSupportedDateFrom(context.date_from(), context);
        if (context.date_to() != null) {
            validateSupportedDateFrom(context.date_to().date_from(), context);
        }
    }

    private void validateSupportedDateFrom(Date_fromContext context, Monthday_rangeContext monthdayRange) {
        if (context == null) {
            return;
        }

        if (context.variable_date() != null) {
            throw new UnsupportedOperationException("Variable dates such as 'easter' are not supported: " + monthdayRange.getText());
        }

        if (context.year() != null) {
            throw new UnsupportedOperationException("Year qualified dates are not supported: " + monthdayRange.getText());
        }
    }

    private MonthDay toMonthDay(Month month, Month_dayContext context, Monthday_rangeContext monthdayRange) {
        int dayOfMonth = Integer.parseInt(context.getText());
        try {
            return MonthDay.of(month, dayOfMonth);
        } catch (DateTimeException dateTimeException) {
            throw new UnsupportedOperationException(
                    "Day of month %d does not exist in %s: %s".formatted(dayOfMonth, month, monthdayRange.getText()),
                    dateTimeException);
        }
    }

    private EnumSet<DayOfWeek> toDays(Weekday_selectorContext context) {
        if (context.holiday_sequence() != null) {
            throw new UnsupportedOperationException("Holiday selectors are not supported: " + context.getText());
        }

        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (Weekday_rangeContext weekdayRange : context.weekday_sequence().weekday_range()) {
            // The grammar only allows a day offset behind a nth entry, so checking the nth entries covers both.
            if (!weekdayRange.nth_entry().isEmpty()) {
                throw new UnsupportedOperationException("Nth weekday entries and day offsets are not supported: " + context.getText());
            }

            List<WdayContext> weekDayContexts = weekdayRange.wday();
            DayOfWeek from = toDay(weekDayContexts.get(0));
            DayOfWeek to = weekDayContexts.size() > 1 ? toDay(weekDayContexts.get(1)) : from;
            days.addAll(dayRange(from, to));
        }

        return days;
    }

    private void validateSimpleTimespan(TimespanContext context) {
        if (context.extended_time() == null || context.number() != null || context.getText().endsWith("+")) {
            throw new UnsupportedOperationException(
                    "Only simple time ranges (e.g. '09:00-17:00') are supported: " + context.getText());
        }

        if (context.time().variable_time() != null || context.extended_time().variable_time() != null) {
            throw new UnsupportedOperationException("Sunrise/sunset relative times are not supported: " + context.getText());
        }
    }

    private LocalTime toStartTime(TimespanContext context) {
        validateSimpleTimespan(context);
        return toLocalTime(context.time().hour_minutes());
    }

    private LocalTime toEndTime(TimespanContext context) {
        validateSimpleTimespan(context);
        Extended_timeContext extendedTime = context.extended_time();
        int hour = Integer.parseInt(extendedTime.extended_hour().getText()) % HOURS_IN_A_DAY;
        int minute = Integer.parseInt(extendedTime.minute().getText());

        return LocalTime.of(hour, minute);
    }

    private LocalTime toLocalTime(Hour_minutesContext context) {
        int hour = Integer.parseInt(context.hour().getText());
        int minute = Integer.parseInt(context.minute().getText());

        return LocalTime.of(hour, minute);
    }

    private EnumSet<DayOfWeek> dayRange(DayOfWeek from, DayOfWeek to) {
        EnumSet<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        DayOfWeek dayOfWeek = from;
        while (true) {
            days.add(dayOfWeek);
            if (dayOfWeek == to) {
                break;
            }
            dayOfWeek = dayOfWeek.plus(1);
        }

        return days;
    }

    private Month toMonth(MonthContext context) {
        return MONTHS_BY_ABBREVIATION.get(context.getText());
    }

    private DayOfWeek toDay(WdayContext context) {
        return DAYS_BY_ABBREVIATION.get(context.getText());
    }
}
