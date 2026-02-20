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
     * 
     * For method getMentionedUsers:
     * Partition the Tweets case into:
     * 1.mention repetitive:repeated or not
     * 2.mentioned users:0 or more
     * 3.precede character:valid character,invalid character or none
     * 4.follow character:valid character,invalid character or none
     * 5.username case:all lowercase,all upper case,mixed case
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = d2.plusNanos(1); //close to d2 
    private static final Instant d4 = Instant.parse("1996-02-17T12:00:00Z"); //far from other days,unordered element
    private static final Instant d5 = d3; //same as d3
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "@bbitdiddle,is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "bitdavis", "@alyssa, check out this nano-second difference!", d3);
    private static final Tweet tweet4 = new Tweet(4, "charles", "Throwback to 1996! No @mentions here.", d4);
    private static final Tweet tweet5 = new Tweet(5, "alyssa", "@bbitdiddle ,Replying again at the exact same time ", d5);
    private static final Tweet tweet6 = new Tweet(6, "charles_newVersion", "How to use at ?,is help@mit.edu a legal format?", d5);
    private static final Tweet tweet7 = new Tweet(7, "alyssa", "At should not be preceded by any characters, @charles_newVersion", d5);
    private static final Tweet tweet8 = new Tweet(8, "charles_newVersion", "Thx,I'v known the way to use @ !", d5);
    private static final Tweet tweet9 = new Tweet(9, "bitdavis", "@alyssa, @ALYSSA, @Alyssa ! loooook at this!", d3);
    
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
    
    /************* Test Case for GetMentionedUsers *************/
    
    //This test covers users=0
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet2));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    //This test covers repetitive=true,users=more,precede=none,follow=invalid character
    @Test
    public void testGetMentionedUsersRepeatedNames() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1,tweet5));
        
        assertTrue("expected only one User", mentionedUsers.size()==1);
    }
    
    //This test covers precede=valid character,follow=valid character
    @Test
    public void testGetMentionedUsersPrecededFormat() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet6));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    //This test covers follow=valid character,precede=invalid character
    @Test
    public void testGetMentionedUsersFollowedFormat() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet7));
        String name = mentionedUsers.iterator().next();
        assertEquals("charles_newversion", name.toLowerCase());
    }
    
    //This test covers precede=none,follow=none
    @Test
    public void testGetMentionedUsersEmptyFormat() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet8));
        
        assertTrue("expected empty set",mentionedUsers.isEmpty());
    }
    
    //This test covers username case=upper,lower,mixed
    @Test
    public void testGetMentionedUserstDifferentCases() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet9));
        
        assertTrue("expected only one User", mentionedUsers.size()==1);
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
