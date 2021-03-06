package de.setsoftware.reviewtool.ordering.efficientalgorithm;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Tests for {@link TourCalculator}.
 */
public class TourCalculatorTest {

    /**
     * Helper class to build the test data.
     */
    private static final class TourCalculatorInput {
        private final List<String> parts = new ArrayList<>();
        private final List<MatchSet<String>> matchSets = new ArrayList<>();
        private final List<PositionRequest<String>> positionRequests = new ArrayList<>();
        private Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }
        };

        public static TourCalculatorInput tourCalculatorFor(String... parts) {
            final TourCalculatorInput ret = new TourCalculatorInput();
            ret.parts.addAll(Arrays.asList(parts));
            return ret;
        }

        public TourCalculatorInput matchChained(String... parts) {
            assert parts.length >= 2;
            for (int i = 0; i < parts.length - 1; i++) {
                this.match(parts[i], TargetPosition.FIRST, parts[i + 1]);
            }
            return this;
        }

        public TourCalculatorInput matchSymmetric(String... parts) {
            final Set<String> set = new TreeSet<>();
            set.addAll(Arrays.asList(parts));
            final MatchSet<String> ms = new MatchSet<>(set);
            this.matchSets.add(ms);
            return this;
        }

        public TourCalculatorInput match(String distinguishedElement, TargetPosition pos, String... others) {
            final Set<String> set = new TreeSet<>();
            set.add(distinguishedElement);
            set.addAll(Arrays.asList(others));
            final MatchSet<String> ms = new MatchSet<>(set);
            this.matchSets.add(ms);
            this.positionRequests.add(new PositionRequest<>(ms, distinguishedElement, pos));
            return this;
        }

        public TourCalculatorInput comparator(Comparator<String> c) {
            this.comparator = c;
            return this;
        }

        public TourCalculator<String> calculate() throws Exception {
            return TourCalculator.calculateFor(
                    this.parts,
                    this.matchSets,
                    this.positionRequests,
                    this.comparator,
                    new TourCalculatorControl() {
                        @Override
                        public boolean isCanceled() {
                            return false;
                        }

                        @Override
                        public boolean isFastModeNeeded() {
                            return false;
                        }
                    });
        }
    }

    @Test
    public void testRelatedTogether() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("callee", "other", "caller")
                .match("callee", TargetPosition.SECOND, "caller")
                .calculate();

        assertEquals(Arrays.asList("caller", "callee", "other"), actual.getTour());
    }

    @Test
    public void testRelatedTogether2() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("callee", "other1", "caller", "other2")
                .match("callee", TargetPosition.SECOND, "caller")
                .calculate();

        assertEquals(Arrays.asList("caller", "callee", "other1", "other2"), actual.getTour());
    }

    @Test
    public void testDeclUseAndCallFlow() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("useAndCallee", "decl", "caller")
                .match("decl", TargetPosition.FIRST, "useAndCallee")
                .match("useAndCallee", TargetPosition.SECOND, "caller")
                .calculate();

        assertEquals(Arrays.asList("decl", "useAndCallee", "caller"), actual.getTour());
    }

    @Test
    public void testMatchingWithClustering() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("declA", "useA", "declB", "useB", "declC", "useC", "commonCallee", "other")
                .match("declA", TargetPosition.FIRST, "useA")
                .match("declB", TargetPosition.FIRST, "useB")
                .match("declC", TargetPosition.FIRST, "useC")
                .match("commonCallee", TargetPosition.SECOND, "useA", "useB", "useC")
                .calculate();

        assertEquals(
                Arrays.asList("declA", "useA", "commonCallee", "declC", "useC", "useB", "declB", "other"),
                actual.getTour());
    }

    @Test
    public void testMatchingWithClustering2() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor(
                        "f1_A", "f1_B", "f1_C",
                        "f2_A", "f2_B", "f2_C",
                        "f3_A", "f3_B", "f3_C",
                        "f4_A", "f4_B", "f4_C")
                .matchSymmetric("f1_A", "f1_B", "f1_C")
                .matchSymmetric("f2_A", "f2_B", "f2_C")
                .matchSymmetric("f3_A", "f3_B", "f3_C")
                .matchSymmetric("f4_A", "f4_B", "f4_C")
                .matchSymmetric("f1_A", "f2_A")
                .matchSymmetric("f3_A", "f4_A")
                .matchSymmetric("f2_A", "f4_A")
                .calculate();

        assertEquals(
                Arrays.asList(
                        "f1_C", "f1_B", "f1_A",
                        "f2_A", "f2_B", "f2_C",
                        "f4_C", "f4_B", "f4_A",
                        "f3_A", "f3_B", "f3_C"),
                actual.getTour());
    }

    @Test
    public void testFurther() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("declA", "useA1", "useA2", "declB", "useB1", "useB2")
                .match("declA", TargetPosition.FIRST, "useA1", "useA2")
                .match("declB", TargetPosition.FIRST, "useB1", "useB2")
                .match("useA1", TargetPosition.SECOND, "useB1", "useB2")
                .calculate();

        assertEquals(
                Arrays.asList("declA", "useA2", "useA1", "useB1", "useB2", "declB"),
                actual.getTour());
    }

    @Test
    public void testGraphFromInterviews() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("Maus", "Birne", "Stern", "Homer", "Baum", "Strasse", "Buch")
                //addRelationStar(g, TestRelationTypes.SAME_METHOD, "determineChange()", "Strasse", "Baum");
                .match("Strasse", TargetPosition.FIRST, "Baum")
                //addRelationStar(g, TestRelationTypes.SAME_METHOD, "<init>()", "Stern", "Birne");
                .match("Stern", TargetPosition.FIRST, "Birne")
                //addRelationStar(g, TestRelationTypes.DECLARATION_USE, "att:maxTextDiffThreshold",
                //     "Homer", "Strasse", "Baum", "Birne");
                .match("Homer", TargetPosition.FIRST, "Strasse", "Baum", "Birne")
                //addRelationStar(g, TestRelationTypes.DECLARATION_USE, "par:maxTextDiffThreshold", "Stern", "Birne");
                .match("Stern", TargetPosition.FIRST, "Birne")
                //addRelationChain(g, TestRelationTypes.DATA_FLOW, "xml_param", "Buch", "Maus",
                //     "Stern", "Birne", "Strasse", "Baum");
                .matchChained("Buch", "Maus")
                //addRelationStar(g, TestRelationTypes.CALL_FLOW, "SvnChangeSource()", "Maus", "Stern");
                .match("Stern", TargetPosition.SECOND, "Maus")
                //addRelationSymmetric(g, TestRelationTypes.SIMILARITY, "foo", "Strasse", "Baum");
                .matchSymmetric("Strasse", "Baum")
                .calculate();

        assertEquals(
                Arrays.asList("Buch", "Maus", "Stern", "Birne", "Strasse", "Baum", "Homer"),
                actual.getTour());
    }

    private static Comparator<String> naturalComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
    }

    private static Comparator<String> inverseNaturalComparator() {
        return new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        };
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequests1() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "C", "D", "B")
                .comparator(naturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("A", "B", "C", "D"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequests2() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "C", "D", "B")
                .matchSymmetric("A", "C")
                .matchSymmetric("D", "B")
                .comparator(naturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("A", "C", "B", "D"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequests3() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "C", "D", "B")
                .matchSymmetric("A", "C")
                .matchSymmetric("D", "B")
                .comparator(inverseNaturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("D", "B", "C", "A"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed1() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "C", "D", "B")
                .matchSymmetric("A", "C")
                .matchSymmetric("C", "D")
                .matchSymmetric("D", "B")
                .comparator(naturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("A", "C", "D", "B"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed2() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "C", "D", "B")
                .matchSymmetric("A", "C")
                .matchSymmetric("C", "D")
                .matchSymmetric("D", "B")
                .comparator(inverseNaturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("B", "D", "C", "A"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed3() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "B", "C", "D", "E", "F")
                .matchSymmetric("A", "B", "C", "D")
                .matchSymmetric("C", "D", "E", "F")
                .comparator(naturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("A", "B", "C", "D", "E", "F"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed4() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "B", "C", "D", "E", "F")
                .matchSymmetric("A", "B", "C", "D")
                .matchSymmetric("C", "D", "E", "F")
                .comparator(inverseNaturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("F", "E", "D", "C", "B", "A"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed5() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "B", "C", "D", "E", "F")
                .matchSymmetric("A", "B", "E", "F")
                .matchSymmetric("C", "D", "E", "F")
                .comparator(naturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("A", "B", "E", "F", "C", "D"),
                actual.getTour());
    }

    @Test
    public void testWithTieBreakingWithoutPositionRequestsWithFullyFixed6() throws Exception {
        final TourCalculator<String> actual = TourCalculatorInput
                .tourCalculatorFor("A", "B", "C", "D", "E", "F")
                .matchSymmetric("A", "B", "E", "F")
                .matchSymmetric("C", "D", "E", "F")
                .comparator(inverseNaturalComparator())
                .calculate();
        assertEquals(
                Arrays.asList("D", "C", "F", "E", "B", "A"),
                actual.getTour());
    }

    private static void doTestWithGeneratedData(Random r) throws Exception {
        final int size = r.nextInt(30) + 3;
        final int matches = r.nextInt(2 * size) + 1;
        doTestWithGeneratedData(r, size, matches);
    }

    private static void doTestWithGeneratedData(Random r, final int size, final int matches) throws Exception {
        final List<String> ints = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            ints.add(Integer.toString(i));
        }
        final TourCalculatorInput b = TourCalculatorInput.tourCalculatorFor(ints.toArray(new String[ints.size()]));
        for (int round = 0; round < matches; round++) {
            Collections.shuffle(ints, r);
            final int setSize = 2 + r.nextInt(ints.size() - 2);
            final String[] set = ints.subList(0, setSize).toArray(new String[setSize]);
            TargetPosition pos;
            switch (r.nextInt(3)) {
            case 0:
                pos = TargetPosition.FIRST;
                break;
            case 1:
                pos = TargetPosition.SECOND;
                break;
            case 2:
                pos = TargetPosition.LAST;
                break;
            default:
                throw new AssertionError();
            }
            b.match(set[0], pos, Arrays.copyOfRange(set, 1, set.length));
        }
        final TourCalculator<String> result = b.calculate();
        assertEquals(result.getTour().size(), ints.size());
    }

    @Test
    public void testSmokeTest() throws Exception {
        for (int i = 0; i < 100; i++) {
            try {
                doTestWithGeneratedData(new Random(i));
            } catch (final AssertionError e) {
                throw new AssertionError("problem with seed " + i, e);
            }
        }
    }

//    @Test
//    public void testPerformance() throws Exception {
//        final List<String> allChangeParts = new ArrayList<>();
//        final List<MatchSet<String>> matchSets = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            final String a = "p" + i + "_A";
//            final String b = "p" + i + "_B";
//            final String c = "p" + i + "_C";
//            allChangeParts.add(a);
//            allChangeParts.add(b);
//            allChangeParts.add(c);
//            matchSets.add(new MatchSet<>(new HashSet<String>(Arrays.asList(a, b, c))));
//        }
//        for (int i = 0; i < allChangeParts.size() / 3; i++) {
//            for (int j = i + 1; j < allChangeParts.size() / 3; j++) {
//                matchSets.add(new MatchSet<>(new HashSet<String>(Arrays.asList(
//                        allChangeParts.get(3 * i),
//                        allChangeParts.get(3 * j)))));
//            }
//        }
//
//        final List<PositionRequest<String>> positionRequests = new ArrayList<>();
//        System.out.println("init done");
//        final List<Long> durations = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
////            while (System.in.read() != 'a') {
////            }
//            final long start = System.currentTimeMillis();
//            final TourCalculator<String> t = TourCalculator.calculateFor(allChangeParts, matchSets, positionRequests);
//            final long end = System.currentTimeMillis();
//            final long duration = end - start;
//            System.out.println("elapsed: " + duration);
//            System.out.println("tour size: " + t.getTour().size());
//            durations.add(duration);
////            while (System.in.read() != 'b') {
////            }
//        }
//        Collections.sort(durations);
//        System.out.println("Median: " + durations.get(durations.size() / 2));
//    }

}
