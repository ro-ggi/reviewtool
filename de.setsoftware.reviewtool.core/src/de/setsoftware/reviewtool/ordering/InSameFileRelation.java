package de.setsoftware.reviewtool.ordering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import de.setsoftware.reviewtool.base.Multimap;
import de.setsoftware.reviewtool.model.api.IRevisionedFile;

/**
 * Groups stops that belong to the same file.
 */
public class InSameFileRelation implements RelationMatcher {

    private final HierarchyExplicitness explicitness;

    public InSameFileRelation(HierarchyExplicitness explicitness) {
        this.explicitness = explicitness;
    }

    @Override
    public Collection<? extends OrderingInfo> determineMatches(List<ChangePart> changeParts) {
        final Multimap<IRevisionedFile, ChangePart> grouping = new Multimap<>();
        for (final ChangePart c : changeParts) {
            grouping.put(c.getStops().get(0).getMostRecentFile(), c);
        }

        final List<OrderingInfo> ret = new ArrayList<>();
        for (final Entry<IRevisionedFile, List<ChangePart>> e : grouping.entrySet()) {
            ret.add(new SimpleUnorderedMatch(this.explicitness, e.getKey().toLocalPath().lastSegment(), e.getValue()));
        }
        return ret;
    }

}
