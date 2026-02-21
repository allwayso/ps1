/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing strategy for writtenBy: 
     * 1. tweets.size(): 0 or more 
     * 2. Number of tweets by the specified username: 0 or more 
     * 3. Case sensitivity: 
     * - username and author match exactly (both lowercase/uppercase) 
     * - username and author differ in case (should still match)
     * 
     * Testing strategy for writtenBy: 
     * 1.tweets.size():0 , 1 more 
     * 2.number of tweets in timespan:some,all,0 
     * 3.tweet instant : strictly within timespan,at the border,strictly out of timespan
     */

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T10:05:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T10:10:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T10:15:00Z");
    private static final Instant d5 = Instant.parse("2016-02-17T10:20:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa",
            "I've been waiting for my latte for 10 years @bbitdiddle", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle",
            "@Alyssa Keep calm, I'm just fighting with the espresso machine!", d2);
    private static final Tweet tweet3 = new Tweet(3, "ALYSSA", "The machine won. My coffee is on the floor. #sad", d3);
    private static final Tweet tweet4 = new Tweet(4, "charles",
            "Watching @alyssa and @bbitdiddle is better than Netflix.", d4);
    private static final Tweet tweet5 = new Tweet(5, "bbitdiddle",
            "Success! I fixed it. @alyssa, come back! Free refills for the floor-latte incident! ☕️", d5);

    @Test(expected = AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }

    /***************** test case for WrittenBy ******************/

    // This test covers size=more,number=more,sensitivity=differ
    @Test
    public void testSensitivityDiff() {
        List<Tweet> res = Filter.writtenBy(Arrays.asList(tweet1, tweet3), "Alyssa");
        assertEquals(res, Arrays.asList(tweet1, tweet3));
    }

    // This test covers size=0
    @Test
    public void testEmptyInput() {
        List<Tweet> res = Filter.writtenBy(new ArrayList<>(), "Alyssa");
        assertTrue(res.isEmpty());
    }

    // This test covers size=more,number=0
    @Test
    public void testNoMatch() {
        List<Tweet> res = Filter.writtenBy(Arrays.asList(tweet1, tweet3), "bbitdiddle");
        assertTrue(res.isEmpty());
    }

    // This test covers size=more,number=more,sensitivity=same
    @Test
    public void testSensitivitySame() {
        List<Tweet> res = Filter.writtenBy(Arrays.asList(tweet2, tweet5), "bbitdiddle");
        assertEquals(res, Arrays.asList(tweet2, tweet5));
    }

    // This test covers size!=number
    @Test
    public void test() {
        List<Tweet> res = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5), "bbitdiddle");
        assertEquals(res, Arrays.asList(tweet2, tweet5));
    }

    /**************** test case for InTimespan *****************/

    // This test covers size=more,number=all
    @Test
    public void testAllFit() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");

        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2,tweet3,tweet4,tweet5), new Timespan(testStart, testEnd));

        assertEquals(inTimespan, Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5));
    }

    // This test covers size=0
    @Test
    public void test0size() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(new ArrayList<>(), new Timespan(testStart, testEnd));
        assertTrue(inTimespan.isEmpty());
    }

    // This test covers size=1,number=0,instant=out
    @Test
    public void testSingleOutTimespan() {
        Instant testStart = Instant.parse("2016-02-17T11:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1), new Timespan(testStart, testEnd));
        assertTrue(inTimespan.isEmpty());
    }
    
    //This test covers size=more,number=0,instant=out
    @Test
    public void testAllOutTimespan() {
        Instant testStart = Instant.parse("2016-02-17T11:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), new Timespan(testStart, testEnd));
        assertTrue(inTimespan.isEmpty());
    }
    
    //This test covers size=1,number=all,instant=border
    @Test
    public void testAtSingleBorder() {
        Instant testStart = Instant.parse("2016-02-17T10:20:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet5), new Timespan(testStart, testEnd));
        assertTrue(!inTimespan.isEmpty());
        assertEquals(inTimespan,Arrays.asList(tweet5));
    }
    //This test covers size=more,number=all,instant=border
    @Test
    public void testAtBothBorder() {
        Instant testStart = Instant.parse("2016-02-17T10:15:00Z");
        Instant testEnd = Instant.parse("2016-02-17T10:20:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet4,tweet5), new Timespan(testStart, testEnd));
        assertEquals(inTimespan,Arrays.asList(tweet4,tweet5));
    }
    
    //This test covers size=more,number=some,instant=in,out,at
    @Test
    public void testSomeWithin() {
        Instant testStart = Instant.parse("2016-02-17T10:10:00Z");
        Instant testEnd = Instant.parse("2016-02-17T12:00:00Z");
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), new Timespan(testStart, testEnd));
        assertEquals(inTimespan,Arrays.asList(tweet3,tweet4,tweet5));
    }
    
    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1), Arrays.asList("talk"));

        assertFalse("expected non-empty list", containing.isEmpty());
        assertTrue("expected list to contain tweets", containing.containsAll(Arrays.asList(tweet1, tweet2)));
        assertEquals("expected same order", 0, containing.indexOf(tweet1));
    }

    /*
     * Warning: all the tests you write here must be runnable against any Filter
     * class that follows the spec. It will be run against several staff
     * implementations of Filter, which will be done by overwriting (temporarily)
     * your version of Filter with the staff's version. DO NOT strengthen the spec
     * of Filter or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own that
     * you have put in Filter, because that means you're testing a stronger spec
     * than Filter says. If you need such helper methods, define them in a different
     * class. If you only need them in this test class, then keep them in this test
     * class.
     */

}
