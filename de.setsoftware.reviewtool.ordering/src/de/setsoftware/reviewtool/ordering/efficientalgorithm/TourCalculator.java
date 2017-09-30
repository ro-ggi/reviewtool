package de.setsoftware.reviewtool.ordering.efficientalgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class TourCalculator<T> {

    private final Set<MatchSet<T>> successfulMatches = new LinkedHashSet<>();
    private final Set<PositionRequest<T>> successfulPositionings = new LinkedHashSet<>();
    private List<T> resultingTour;

    private static final class FoldMatchingHelper<S> {

        private BundleCombinationTreeElement<S> bundler;
        private final Map<MatchSet<S>, List<MatchSet<S>>> unsatisfiedMatchesWithPotentiallyRelevantFolds;
        private final LinkedList<MatchSet<S>> todoQueue;
        private final Map<MatchSet<S>, List<MatchSet<S>>> matchedWithFolds;

        public FoldMatchingHelper(BundleCombinationTreeElement<S> bundler, List<MatchSet<S>> unsatisfiedMatches) {
            this.bundler = bundler;
            this.unsatisfiedMatchesWithPotentiallyRelevantFolds = new LinkedHashMap<>();
            for (final MatchSet<S> unsatisfiedMatch : unsatisfiedMatches) {
                this.unsatisfiedMatchesWithPotentiallyRelevantFolds.put(unsatisfiedMatch, new ArrayList<MatchSet<S>>());
            }
            this.todoQueue = new LinkedList<>();
            this.matchedWithFolds = new LinkedHashMap<>();
        }

        public void addPotentialFolds(Collection<MatchSet<S>> matches) {
            this.todoQueue.addAll(matches);
            while (!this.todoQueue.isEmpty()) {
                final MatchSet<S> toFold = this.todoQueue.poll();
                final Iterator<Entry<MatchSet<S>, List<MatchSet<S>>>> iter =
                        this.unsatisfiedMatchesWithPotentiallyRelevantFolds.entrySet().iterator();
                while (iter.hasNext()) {
                    final Entry<MatchSet<S>, List<MatchSet<S>>> e = iter.next();
                    if (Collections.disjoint(e.getKey().getChangeParts(), toFold.getChangeParts())) {
                        continue;
                    }
//                    if (willMakeCollapse(e.getKey(), e.getValue(), toFold)) {
//                        continue;
//                    }
                    e.getValue().add(toFold);
                    if (this.matchWithNewFold(e.getKey(), e.getValue())) {
                        iter.remove();
                    }
                }
            }
        }

        private boolean matchWithNewFold(MatchSet<S> toMatch, List<MatchSet<S>> potentialFolds) {
            final Set<Integer> activeFolds = new LinkedHashSet<>();
            for (int i = 0; i < potentialFolds.size(); i++) {
                activeFolds.add(i);
            }

            final boolean matchesWithFullSet = this.matchesWithFoldSubset(toMatch, potentialFolds, activeFolds);
            if (!matchesWithFullSet) {
                //does not match with the full set, cannot match with a subset either
                return false;
            }

            //determine a minimal subset that still allows the match to happen
            boolean foundUnnecessaryFold;
            do {
                foundUnnecessaryFold = false;
                for (final Integer index : activeFolds.toArray(new Integer[activeFolds.size()])) {
                    //try without a fold. if it still matches, this fold is unnecessary
                    activeFolds.remove(index);
                    if (this.matchesWithFoldSubset(toMatch, potentialFolds, activeFolds)) {
                        foundUnnecessaryFold = true;
                    } else {
                        activeFolds.add(index);
                    }
                }
            } while (foundUnnecessaryFold);
            final Set<S> extendedSet = this.determineExtendedSet(toMatch, potentialFolds, activeFolds);
            this.bundler = this.bundler.bundle(extendedSet);
            this.todoQueue.add(new MatchSet<>(extendedSet));
            this.matchedWithFolds.put(toMatch, this.selectActiveFolds(potentialFolds, activeFolds));
            return true;
        }

        private boolean matchesWithFoldSubset(
                MatchSet<S> toMatch, List<MatchSet<S>> potentialFolds, final Set<Integer> activeFolds) {
            final Set<S> extendedSet = this.determineExtendedSet(toMatch, potentialFolds, activeFolds);
            return this.bundler.bundle(extendedSet) != null;
        }

        private Set<S> determineExtendedSet(
                MatchSet<S> toMatch, List<MatchSet<S>> potentialFolds, Set<Integer> activeFolds) {

            final Set<S> combined = new LinkedHashSet<>(toMatch.getChangeParts());
            for (final Integer index : activeFolds) {
                combined.addAll(potentialFolds.get(index).getChangeParts());
            }
            return combined;
        }

        private List<MatchSet<S>> selectActiveFolds(
                List<MatchSet<S>> potentialFolds, Set<Integer> activeFolds) {

            final List<MatchSet<S>> ret = new ArrayList<>();
            for (final Integer index : activeFolds) {
                ret.add(potentialFolds.get(index));
            }
            return ret;
        }

        public BundleCombinationTreeElement<S> getBundler() {
            return this.bundler;
        }

    }

    public static<S> TourCalculator<S> calculateFor(
            List<S> allChangeParts,
            List<MatchSet<S>> matchSets,
            List<PositionRequest<S>> positionRequests) {
        assert new HashSet<>(allChangeParts).size() == allChangeParts.size() : "there are duplicate change parts";

        final TourCalculator<S> ret = new TourCalculator<>();
        if (allChangeParts.size() <= 1) {
            ret.resultingTour = new ArrayList<>(allChangeParts);
            return ret;
        }

        BundleCombinationTreeElement<S> bundler = BundleCombinationTreeElement.create(allChangeParts);
        final List<MatchSet<S>> unsatisfiedMatches = new ArrayList<>();
        for (final MatchSet<S> matchSet : matchSets) {
            final BundleCombinationTreeElement<S> next = bundler.bundle(matchSet.getChangeParts());
            if (next != null) {
                bundler = next;
                ret.successfulMatches.add(matchSet);
            } else {
                unsatisfiedMatches.add(matchSet);
            }
        }

        final FoldMatchingHelper<S> foldedBundler = new FoldMatchingHelper<>(bundler, unsatisfiedMatches);
        foldedBundler.addPotentialFolds(ret.successfulMatches);
        bundler = foldedBundler.getBundler();


        PositionTreeNode<S> positioner = (PositionTreeNode<S>) bundler.toPositionTree();
        final Map<Integer, List<PositionRequest<S>>> positionRequestsForFolds = new TreeMap<>();
        for (final PositionRequest<S> pr : positionRequests) {
            if (ret.successfulMatches.contains(pr.getMatchSet())) {
                final PositionTreeNode<S> next = positioner.fixPosition(
                        pr.getMatchSet().getChangeParts(),
                        pr.getDistinguishedPart(),
                        pr.getTargetPosition());
                if (next != null) {
                    positioner = next;
                    ret.successfulPositionings.add(pr);
                }
            } else if (foldedBundler.matchedWithFolds.containsKey(pr.getMatchSet())) {
                final int foldCount = foldedBundler.matchedWithFolds.get(pr.getMatchSet()).size();
                List<PositionRequest<S>> list = positionRequestsForFolds.get(foldCount);
                if (list == null) {
                    list = new ArrayList<>();
                    positionRequestsForFolds.put(foldCount, list);
                }
                list.add(pr);
            }
        }

        //TODO positions in folded graphs are currently not implemented in accordance with the paper
        for (final List<PositionRequest<S>> list : positionRequestsForFolds.values()) {
            for (final PositionRequest<S> pr : list) {
                final PositionTreeNode<S> next = positioner.fixPosition(
                        join(pr.getMatchSet(), foldedBundler.matchedWithFolds.get(pr.getMatchSet())),
                        pr.getDistinguishedPart(),
                        pr.getTargetPosition());
                if (next != null) {
                    positioner = next;
                    ret.successfulPositionings.add(pr);
                }
            }
        }

        ret.resultingTour = positioner.getPossibleOrder();
        return ret;
    }

    private static<S> Set<S> join(MatchSet<S> main, List<MatchSet<S>> others) {
        final Set<S> ret = new LinkedHashSet<>(main.getChangeParts());
        for (final MatchSet<S> ms : others) {
            ret.addAll(ms.getChangeParts());
        }
        return ret;
    }

    public List<T> getTour() {
        return this.resultingTour;
    }

}