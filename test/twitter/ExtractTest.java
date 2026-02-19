/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;
import java.util.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * Testing strategy:
     * 
     * For method getTimespan:
     * Partition the Tweets case into:
     * 1. tweets.size(): 1, >1  
     * 2. Time stamp similarity: 
     * - all tweets have same timestamp
     * - timestamps are very close (nanoseconds)
     * - timestamps are far apart (years)
     * 3.Time stamp order:ordered or unordered
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = d2.plusNanos(1); //close to d2 
    private static final Instant d4 = Instant.parse("1996-02-17T12:00:00Z"); //far from other days,unordered element
    private static final Instant d5 = d3; //same as d3
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "bitdavis", "@alyssa, check out this nano-second difference!", d3);
    private static final Tweet tweet4 = new Tweet(4, "charles", "Throwback to 1996! No @mentions here.", d4);
    private static final Tweet tweet5 = new Tweet(5, "alyssa", "Replying again at the exact same time @bitdavis", d5);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    //This test covers amount=some,similarity=different,order=ordered
    @Test
    public void testGetTimespanTwoFarTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet4, tweet1));
        
        assertEquals("expected start", d4, timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }
    
    //This test covers amount=1
    @Test
    public void testGetTimespan1Tweet() {
        Timespan timespan=Extract.getTimespan(Arrays.asList(tweet1));
        assertEquals("expected start",d1,timespan.getStart());
        assertEquals("expected end", d1, timespan.getEnd());
    }
    
    //This test covers amount=some,similarity=same
    @Test
    public void testGetTimespan2SameTweets() {
        Timespan timespan=Extract.getTimespan(Arrays.asList(tweet3,tweet5));
        assertEquals("expected start",d3,timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }
    
    //This test covers amount=some,similarity=close,ordered=unordered
    @Test
    public void testGetTimespanCloseUnorderedTweets() {
        Timespan timespan=Extract.getTimespan(Arrays.asList(tweet3,tweet2));
        assertEquals("expected start",d2,timespan.getStart());
        assertEquals("expected end", d3, timespan.getEnd());
    }
    
    //This test covers amount=some,similarity=far,ordered=unordered
    @Test
    public void testGetTimespanFarUnorderedTweets() {
        Timespan timespan=Extract.getTimespan(Arrays.asList(tweet2,tweet4,tweet1));
        assertEquals("expected start",d4,timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
