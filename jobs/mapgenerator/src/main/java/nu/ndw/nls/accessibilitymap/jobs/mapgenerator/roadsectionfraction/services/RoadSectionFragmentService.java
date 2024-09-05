package nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nu.ndw.nls.accessibilitymap.jobs.mapgenerator.roadsectionfraction.model.RoadSectionFragment;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class RoadSectionFragmentService {

    private static final double FRACTION_START_0 = 0;
    private static final double FRACTION_END_1 = 1;

    private enum Source {
        PARAM_A,
        PARAM_B
    }

    private static final Double EPSILON = 0.00000000000000000001;

    private static final BiPredicate<Double, Double> COMPARE_WITH_DELTA = (a, b) -> Math.abs(a - b) < EPSILON;


    /**
     * This method will iterate over the list and will call combiningPossible to verify if combining the previous
     * with the next item is possible and desired and if the result is true, will call combiner to merge the data into
     * one end result. If merging occurs, then the newly created result becomes the previous result for the next
     * fragment. Fraction equality is compared without a delta, it is assumed that the exact same fractions at which a
     * {@link RoadSectionFragment} ends is also used for where the next begins
     *
     * @param roadSectionFragments fragments of road sections that contain data, ordered by fractions asc
     * @param combiningPossible Receives two data arguments and should return if combining is desired and possible
     * @param combiner merges two data objects into a single one
     * @return List of which fragments are combined into a bigger fragments
     * @param <T> Data type
     */
    public <T> List<RoadSectionFragment<T>> combineSequentials(List<RoadSectionFragment<T>>
            roadSectionFragments, BiPredicate<T, T> combiningPossible, BinaryOperator<T> combiner) {
        return combineSequentials(roadSectionFragments, combiningPossible, combiner, COMPARE_WITH_DELTA);
    }

    /**
     * Overloaded method of the public method, allows supplying your own equality method. Can be made public when
     * required
     *
     * @param roadSectionFragments fragments of road sections that contain data, ordered by fractions asc
     * @param combiningPossible Receives two data arguments and should return if combining is desired and possible
     * @param combiner merges two data objects into a single one
     * @param equalsPredicate Allows you to supply a custom double equality method.
     * @return List of which fragments are combined into a bigger fragments
     * @param <T> Data type
     */
    private <T> List<RoadSectionFragment<T>> combineSequentials(List<RoadSectionFragment<T>>
            roadSectionFragments, BiPredicate<T, T> combiningPossible, BinaryOperator<T> combiner,
            BiPredicate<Double,Double> equalsPredicate) {
        if (CollectionUtils.isEmpty(roadSectionFragments)) {
            return Collections.emptyList();
        }

        List<RoadSectionFragment<T>> result = new ArrayList<>();

        Iterator<RoadSectionFragment<T>> it = roadSectionFragments.iterator();
        RoadSectionFragment<T> previous = it.next();
        RoadSectionFragment<T> next;
        while (it.hasNext()) {
            next = it.next();

            if (isConnectingSequentialFragment(previous, next, equalsPredicate) &&
                combiningPossible.test(previous.getData(), next.getData())) {
                previous = mergeRoadSectionFragment(previous, next, combiner.apply(previous.getData(), next.getData()));
            } else {
                // combining not possible, add previous to list and continue
                result.add(previous);
                previous = next;
            }
        }

        if (previous != null) {
            result.add(previous);
        }

        return result;
    }

    /**
     * Both parameters accept null values, an empty list or a list with one or more fractions of road sections. Each
     * list should be sorted on start fraction ascending. List A and list B should not contain overlapping road section
     * fragments in the same list and needs to be sorted in ascending fraction occurrence.
     * The combiner is invoked with either one or two arguments, depending on whether a fragment of A and B needs to
     * be combined or whether there is only an A or B result. It's also invoked when both A and B fragments return null
     * data values. The first parameter is always the result from A and the second parameter is always the result of B,
     * this allows you to keep track of where the data is coming from.
     *
     * @param a null or list of {@link RoadSectionFragment}, sorted by fraction and no overlapping fragment areas
     * @param b null or list of {@link RoadSectionFragment}, sorted by fraction and no overlapping fragment areas
     * @param combiner is called for all specified fragment combinations as created from list A and B, first argument is
     *                 data from A and second argument is always data from B. Is also called when either A or B or both
     *                 have null data values.
     * @return a list containing all unique road section fraction ranges constructed from fractions in list a and b
     */
    public <T, R> List<RoadSectionFragment<R>> splitAndCombineData(List<RoadSectionFragment<T>> a,
            List<RoadSectionFragment<T>> b, BiFunction<T,T,R> combiner) {
        return splitAndCombineData(a, b, combiner, COMPARE_WITH_DELTA);
    }

    /**
     * Overloaded method of the public method, allows supplying your own equality method. Can be made public when
     * required
     *
     * @param a null or list of {@link RoadSectionFragment}, sorted by fraction and no overlapping fragment areas
     * @param b null or list of {@link RoadSectionFragment}, sorted by fraction and no overlapping fragment areas
     * @param combiner is called for all specified fragment combinations as created from list A and B, first argument is
     *                 data from A and second argument is always data from B. Is also called when either A or B or both
     *                 have null data values.
     * @param equalsPredicate Allows you to supply a custom double equality method
     * @return
     * @param <T>
     * @param <R>
     */
    private <T, R> List<RoadSectionFragment<R>> splitAndCombineData(List<RoadSectionFragment<T>> a,
            List<RoadSectionFragment<T>> b, BiFunction<T,T,R> combiner, BiPredicate<Double,Double> equalsPredicate) {
        if (CollectionUtils.isEmpty(a) && CollectionUtils.isEmpty(b)) {
            return Collections.emptyList();
        } else if (CollectionUtils.isEmpty(a)) {
            return combineSingleSource(b, combiner, Source.PARAM_B);
        } else if (CollectionUtils.isEmpty(b)) {
            return combineSingleSource(a, combiner, Source.PARAM_A);
        }

        // Both have data, create look up trees
        TreeMap<Double, RoadSectionFragment<T>> mapA = createStartFractionToDataLookupMap(a, equalsPredicate);
        TreeMap<Double, RoadSectionFragment<T>> mapB = createStartFractionToDataLookupMap(b, equalsPredicate);

        // Find out from which fraction to which fraction we have ranges
        Set<Double> fractions = new HashSet<>(mapUniqueFractions(a));
        fractions.addAll(mapUniqueFractions(b));

        LinkedHashSet<Double> sortedStartFractions = fractions.stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<RoadSectionFragment<R>> result = new ArrayList<>();

        Iterator<Double> it = sortedStartFractions.iterator();
        Double startFraction = it.next();
        while (it.hasNext()) {
            Double endFraction = it.next();

            Entry<Double, RoadSectionFragment<T>> entryA = mapA.floorEntry(startFraction);
            Entry<Double, RoadSectionFragment<T>> entryB = mapB.floorEntry(startFraction);

            // If an area has no A and B fragment, then skip this area
            if (isEntryCoveringAFragment(entryA) || isEntryCoveringAFragment(entryB)) {
                // Even if data from A and B is null, invoke the combiner. This gives the invoking code more control
                // of how to deal with this situation. If null values are not wanted, the invoking code can filter it
                // out, for example by post-processing the result by calling combineSequentials
                result.add(RoadSectionFragment.<R>builder()
                        .data(combiner.apply(getDataIfAreaHasFragment(entryA), getDataIfAreaHasFragment(entryB)))
                        .fromFraction(startFraction)
                        .toFraction(endFraction)
                        .build());
            }

            startFraction=endFraction;
        }

        return result;
    }

    private <T> RoadSectionFragment<T> mergeRoadSectionFragment(RoadSectionFragment<T> first,
            RoadSectionFragment<T> second, T combinedData) {
        return  RoadSectionFragment.<T>builder()
                .fromFraction(first.getFromFraction())
                .toFraction(second.getToFraction())
                .data(combinedData)
                .build();
    }

    /**
     * Checks equality of fractions using == and not using a delta, because it is assumed that the origin of the
     * fractions are constants and not calculated values
     *
     * @param first a road section
     * @param second another road section
     * @return true of the fractions exactly match
     * @param <T> some data we don't care about
     */
    private <T> boolean isConnectingSequentialFragment(RoadSectionFragment<T> first, RoadSectionFragment<T> second,
            BiPredicate<Double,Double> equalDouble) {
        // the doubles are compared to be the exact same doubles, we do not use a delta as we do not expect calculated
        // values
        return equalDouble.test(first.getToFraction(), second.getFromFraction());
    }

    private <T> boolean isEntryCoveringAFragment(Entry<Double, RoadSectionFragment<T>> entry) {
        return entry.getValue() != null;
    }

    private <T> T getDataIfAreaHasFragment(Entry<Double, RoadSectionFragment<T>> entry) {
        if (!isEntryCoveringAFragment(entry)) {
            return null;
        }

        return entry.getValue().getData();
    }

    private <T, R> List<RoadSectionFragment<R>> combineSingleSource(List<RoadSectionFragment<T>> fragments,
            BiFunction<T,T,R> combiner, Source source) {
        return fragments.stream()
                .map(roadSectionFragment -> cloneRoadSectionFragment(roadSectionFragment, combiner, source))
                .toList();
    }

    private <T> Set<Double> mapUniqueFractions(List<RoadSectionFragment<T>> fragments) {
        return fragments.stream()
                .flatMap(fragment -> Stream.of(fragment.getFromFraction(), fragment.getToFraction()))
                .collect(Collectors.toSet());
    }

    /**
     * Creates a map with all from fragments as keys along with the corresponding fragment data. If there is gap between
     * a successor fragment, then a key with the previous end fraction is added with a null value to mark the end of
     * the previous fragment, which will later on allow us to lookup ranges.
     *
     * @param fragments all the fragments
     * @return lookup map supporting ranges
     * @param <T> data
     */
    private <T> TreeMap<Double, RoadSectionFragment<T>> createStartFractionToDataLookupMap(
            List<RoadSectionFragment<T>> fragments, BiPredicate<Double,Double> equalDouble) {
        TreeMap<Double, RoadSectionFragment<T>> map = new TreeMap<>();

        Iterator<RoadSectionFragment<T>> it = fragments.iterator();

        double previousEndFraction = FRACTION_START_0;

        while (it.hasNext()) {
            RoadSectionFragment<T> fragment = it.next();

            if (isThereAGapBetweenFragments(previousEndFraction, fragment.getFromFraction(), equalDouble)) {
                // insert end as key indicating a data-less area
                map.put(previousEndFraction, null);
            }

            map.put(fragment.getFromFraction(), fragment);
            previousEndFraction = fragment.getToFraction();
        }

        // If we did not reach the end, add an extra start fraction with the previous end fraction to mark an area
        // without any fragments
        if (!equalDouble.test(previousEndFraction, FRACTION_END_1)) {
            map.put(previousEndFraction, null);
        }

        return map;
    }

    private boolean isThereAGapBetweenFragments(double previousEndFraction, double nextFromFraction,
            BiPredicate<Double,Double> equalDouble){
        return !equalDouble.test(previousEndFraction, nextFromFraction);
    }

    private <T, R> RoadSectionFragment<R> cloneRoadSectionFragment(RoadSectionFragment<T> original,
            BiFunction<T,T, R> combiner, Source source) {

        R data;
        if (source == Source.PARAM_A) {
            data = combiner.apply(original.getData(), null);
        } else {
            data = combiner.apply(null, original.getData());
        }

        return RoadSectionFragment.<R>builder()
                .fromFraction(original.getFromFraction())
                .toFraction(original.getToFraction())
                .data(data)
                .build();
    }

}
