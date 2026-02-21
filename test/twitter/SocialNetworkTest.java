/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.Instant;
import org.junit.Test;

public class SocialNetworkTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     *
     * Testing strategy for guessFollowGraph:
     * 1.Tweets.size=0,1,more
     * 2.key object:author , @-mentioned or not existed
     * 3.mention object:self-mention,mention others,mention nobody
     * 4.case-insensitivity:same or differ
     * 5.mention repeat:same object in same or differ tweets
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T10:05:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T10:10:00Z");
    private static final Instant d4 = Instant.parse("2016-02-17T10:15:00Z");
    private static final Instant d5 = Instant.parse("2016-02-17T10:20:00Z");

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "I've been waiting for my latte for 10 years @bbitdiddle", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "@alyssa sorry! The machine is broken again. #java", d2);
    private static final Tweet tweet3 = new Tweet(3, "charlie", "Hey @alyssa and @Bbitdiddle, do you want to join the meeting?", d3);
    private static final Tweet tweet4 = new Tweet(4, "alyssa", "Is @evans coming to the office today?", d4);
    private static final Tweet tweet5 = new Tweet(5, "evans", "@bbitdiddle can you check my pull request?", d5);
    private static final Tweet tweet6 = new Tweet(6, "frank", "Great talk today @george! #MIT", d1);
    private static final Tweet tweet7 = new Tweet(7, "george", "I agree with @Alyssa about the latte situation.", d2);
    private static final Tweet tweet8 = new Tweet(8, "heather", "@george check out this new paper.", d3);
    private static final Tweet tweet9 = new Tweet(9, "alyssa", "Still waiting @bbitdiddle...", d4);
    private static final Tweet tweet10 = new Tweet(10, "ivan", "Who is @alyssa and why is she so famous? Anybody knows @ivan !", d5);
    
    private static final ArrayList<Tweet> totalTweets=new ArrayList<>(Arrays.asList(
            tweet1, tweet2, tweet3, tweet4, tweet5, 
            tweet6, tweet7, tweet8, tweet9, tweet10
        ));
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /**********assisting assert for case-insensitivity***********/
    
    /**
     * Asserts that a follow relationship exists between the given author and mentioned user,
     * ignoring case sensitivity for both usernames.
     * * @param graph the social network map to test
     * @param author the expected follower
     * @param mentioned the expected user being followed
     */
    private void assertConnectionExists(Map<String, Set<String>> graph, String author, String mentioned) {
        String foundAuthorKey = null;
        for (String key : graph.keySet()) {
            if (key.equalsIgnoreCase(author)) {
                foundAuthorKey = key;
                break;
            }
        }
        
        assertNotNull("Expected author '" + author + "' not found in graph keys", foundAuthorKey);
        
        Set<String> following = graph.get(foundAuthorKey);
        boolean foundMention = false;
        for (String s : following) {
            if (s.equalsIgnoreCase(mentioned)) {
                foundMention = true;
                break;
            }
        }
        
        assertTrue("Author '" + foundAuthorKey + "' should follow '" + mentioned + "'", foundMention);
    }
    
    private void assertConnectionNotExist(Map<String, Set<String>> graph, String author, String mentioned) {
        String foundAuthorKey = null;
        for (String key : graph.keySet()) {
            if (key.equalsIgnoreCase(author)) {
                foundAuthorKey = key;
                break;
            }
        }

        if (foundAuthorKey != null) {
            Set<String> following = graph.get(foundAuthorKey);
            for (String s : following) {
                assertFalse("Author '" + foundAuthorKey + "' should NOT follow '" + mentioned + "'", 
                             s.equalsIgnoreCase(mentioned));
            }
        }
    }
    /**************test case for guessFollowGraph**************/
    
    //This test covers tweets.size=0
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(new ArrayList<>());
        
        assertTrue("expected empty graph", followsGraph.isEmpty());
    }
    
    //This test covers tweets.size=1,case=different(both for keys and values)
    @Test
    public void testDifferCase() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1,tweet3));
        assertConnectionExists(followsGraph, "alyssa", "bbitdiddle");
        assertConnectionExists(followsGraph, "charles", "bbitdiddle");
    }
    
    //This test covers tweets.size=1,mention object=self-mention,key=author,case=same
    @Test
    public void testSelfMention() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet10));
        assertConnectionNotExist(followsGraph, "ivan", "ivan");
    }
    
    //This test covers tweets.size=1,mention object=mention others,key=author,case=same
    @Test
    public void testNormalMention() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet10));
        assertConnectionNotExist(followsGraph, "ivan", "alyssa");
    }
    
  //This test covers tweets.size=1,mention object=mention others,key=author,case=same
    @Test
    public void testAuthorKey() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet10));
        String foundAuthorKey = null;
        String author="ivan";
        for (String key : followsGraph.keySet()) {
            if (key.equalsIgnoreCase(author)) {
                foundAuthorKey = key;
                break;
            }
        }
        
        assertNotNull("Expected author '" + author + "' not found in graph keys", foundAuthorKey);
        assertTrue(followsGraph.get(foundAuthorKey).isEmpty());
    }
    
    
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> followsGraph = new HashMap<>();
        List<String> influencers = SocialNetwork.influencers(followsGraph);
        
        assertTrue("expected empty list", influencers.isEmpty());
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * SocialNetwork class that follows the spec. It will be run against several
     * staff implementations of SocialNetwork, which will be done by overwriting
     * (temporarily) your version of SocialNetwork with the staff's version.
     * DO NOT strengthen the spec of SocialNetwork or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in SocialNetwork, because that means you're testing a
     * stronger spec than SocialNetwork says. If you need such helper methods,
     * define them in a different class. If you only need them in this test
     * class, then keep them in this test class.
     */

}
