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
     * 
     * 
     * Testing strategy for containing:
     * Partition the inputs as follows:
     * 1. tweets.size(): 0, 1, >1
     * 2. words.size(): 0, 1, >1
     * 3. Word position in text: start, middle, end
     * 4. Word-to-Target relation: 
     * - exact match (apple vs apple)
     * - case-insensitive match (apple vs APPLE)
     * - partial match (substring) (apple vs pineapple, apple vs app) -> should NOT match
     * - multiple words in text match one target word
     * - one tweet matches multiple target words
     * 5. match condition:
     * - many for one:multiple words in one single tweet
     * - one for many:multiple tweets fit one single word
     * - one for one
     * 6.other char:only letters , mixed with chars(not blank)
     */ 
     
    
    
     

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T10:05:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T10:10:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T10:15:00Z");
    private static final Instant d5 = Instant.parse("2016-02-17T10:20:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa",
            "I've been waiting for my latte for 10 years @bbitdiddle", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle",
            "@Alyssa Keep calm, I'm just fighting with the espresso machine !", d2);
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
        assertEquals(Arrays.asList(tweet1, tweet3),res);
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
    public void testMixedCase() {
        List<Tweet> res = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3, tweet4, tweet5), "bbitdiddle");
        assertEquals(Arrays.asList(tweet2, tweet5),res);
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
    
    /****************test for Containing*****************/
    
    //This test covers tweet.size=0
    @Test
    public void testEmptyTweet() {
        List<Tweet> res = Filter.containing(new ArrayList<>(), Arrays.asList("espresso"));
        assertTrue(res.isEmpty());
    }
    
    //This test covers words.size=0
    @Test
    public void testEmptyWords() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), new ArrayList<>());
        assertTrue(res.isEmpty());
    }
    
    //This test covers position=start,tweets.size=1,words.size=1,relation=exact match,match condition=one for one,other char:only letters
    @Test
    public void testStartPostion() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet4), Arrays.asList("Watching"));
        assertEquals(res,Arrays.asList(tweet4));
    }
    
    //This test covers position=end,tweets.size=1,words.size=1,relation=exact match,match condition=one for one,other char:only letters
    @Test
    public void testEndPostion() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1), Arrays.asList("@bbitdiddle"));
        assertEquals(res,Arrays.asList(tweet1));
    }
    
    //This Test covers position=middle,tweets.size=1,words.size=more,relation=exact match,match condition=many for one,other char:only letters
    @Test
    public void testManyForOne() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1), Arrays.asList("latte","waiting"));
        assertEquals(res,Arrays.asList(tweet1));
    }
    
    //This test covers position=middle,tweets.size=more,words.size=1,relation=exact match,match condition=one for many,other char:only letters
    @Test
    public void testOneForMany() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("machine"));
        assertEquals(Arrays.asList(tweet2,tweet3),res);
    }
    
    //This test covers position=middle,tweets.size=more,words.size=more,relation=case-insensitive match,match condition=one for many,other char:only letters
    @Test
    public void testCaseInsensitive() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("Machine"));
        assertEquals(Arrays.asList(tweet2,tweet3),res);
    }
    
    //This test covers position=middle,tweets.size=more,words.size=more,relation=substring
    @Test
    public void testSubstring() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("mach"));
        assertTrue(res.isEmpty());
    }
    
    //This test covers position=middle,tweets.size=more,words.size=more,relation=opposite substring
    @Test
    public void testOppositeSubstring() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("machinary"));
        assertTrue(res.isEmpty());
    }
    
    //This test covers position=middle,tweets.size=more,words.size=more,relation=exact match,other chars=mixed
    @Test
    public void testMixedChars() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("sad"));
        assertTrue(res.isEmpty());
    }
    
    //This test covers position=middle,tweets.size=more,words.size=more,relation=exact match,other chars=mixed
    @Test
    public void testMixedChars2() {
        List<Tweet> res = Filter.containing(Arrays.asList(tweet1,tweet2,tweet3,tweet4,tweet5), Arrays.asList("#sad"));
        assertEquals(res,Arrays.asList(tweet3));
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
