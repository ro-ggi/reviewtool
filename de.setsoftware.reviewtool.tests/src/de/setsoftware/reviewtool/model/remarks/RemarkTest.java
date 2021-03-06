package de.setsoftware.reviewtool.model.remarks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;

import org.junit.Test;

/**
 * Tests for review remarks.
 */
public class RemarkTest {

    @Test
    public void testLoadWithWrongOrder() throws Exception {
        final String input = "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n"
                + "\n"
                + "Review 2:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n";

        final ReviewData d = ReviewData.parse(Collections.<Integer, String>emptyMap(), DummyMarker.FACTORY, input);

        assertEquals("Review 2:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n"
                + "\n"
                + "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n",
                d.serialize());
    }

    @Test
    public void testGapsInRoundNumbers() throws Exception {
        final String input = "Review 3:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n"
                + "\n"
                + "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n"
                + "\n"
                + "Review 5:\n"
                + "* muss\n"
                + "*# Anm E\n"
                + "*# Anm F\n";

        final ReviewData d = ReviewData.parse(Collections.<Integer, String>emptyMap(), DummyMarker.FACTORY, input);

        assertEquals("Review 5:\n"
                + "* muss\n"
                + "*# Anm E\n"
                + "*# Anm F\n"
                + "\n"
                + "Review 3:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n"
                + "\n"
                + "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n",
                d.serialize());
    }

    @Test
    public void testOptionalHashBeforeNumbers() throws Exception {
        final String input =
                "Review #1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n";

        final ReviewData d = ReviewData.parse(Collections.<Integer, String>emptyMap(), DummyMarker.FACTORY, input);

        assertEquals("Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n",
                d.serialize());
    }

    @Test
    public void testSeveralEmptyLinesBetweenReviews() throws Exception {
        final String input = "Review 2:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n"
                + "\n"
                + "\n"
                + "\n"
                + "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n";

        final ReviewData d = ReviewData.parse(Collections.<Integer, String>emptyMap(), DummyMarker.FACTORY, input);

        assertEquals("Review 2:\n"
                + "* muss\n"
                + "*# Anm C\n"
                + "*# Anm D\n"
                + "\n"
                + "Review 1:\n"
                + "* muss\n"
                + "*# Anm A\n"
                + "*# Anm B\n",
                d.serialize());
    }

    @Test
    public void testMissingTypeLeadsToParseException() throws Exception {
        final String input =
                "Review 1:\n"
                + "*# Anm A\n"
                + "*# Anm B\n";

        try {
            ReviewData.parse(Collections.<Integer, String>emptyMap(), DummyMarker.FACTORY, input);
            fail("expected exception");
        } catch (final ReviewRemarkException e) {
            assertTrue(e.getMessage().contains("Anm A"));
        }
    }

}
