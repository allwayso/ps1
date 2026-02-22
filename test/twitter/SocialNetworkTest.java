/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.util.*;
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

    private static final Tweet tweet1 = new Tweet(1, "alyssa", "I've been waiting for my latte for 10 years @bbitdiddle @bbitdiddle", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "@alyssa sorry! The machine is broken again. #java", d2);
    private static final Tweet tweet3 = new Tweet(3, "charlie", "Hey @alyssa and @Bbitdiddle, do you want to join the meeting?", d3);
    private static final Tweet tweet4 = new Tweet(4, "alyssa", "Is @evans coming to the office today?", d4);
    private static final Tweet tweet5 = new Tweet(5, "evans", "@bbitdiddle can you check my pull request?", d5);
    private static final Tweet tweet6 = new Tweet(6, "charlie", "@alyssa and @Bbitdiddle, reply to me as soon as possible!", d1);
    private static final Tweet tweet7 = new Tweet(7, "george", "I agree with @Alyssa about the latte situation.", d2);
    private static final Tweet tweet8 = new Tweet(8, "heather", "@george check out this new paper.", d3);
    private static final Tweet tweet9 = new Tweet(9, "alyssa", "Still waiting for bbitdiddle...", d4);
    private static final Tweet tweet10 = new Tweet(10, "ivan", "Who is @alyssa and why is she so famous? Anybody knows tell @ivan !", d5);
    
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
    
    //This test covers tweets.size=2,case=different(both for keys and values)
    @Test
    public void testDifferCase() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1,tweet3));
        assertConnectionExists(followsGraph, "alyssa", "bbitdiddle");
        assertConnectionExists(followsGraph, "charlie", "bbitdiddle");
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
        assertConnectionExists(followsGraph, "ivan", "alyssa");
    }
    
    //This test covers tweets.size=1,mention object=mention others,key=author,case=same
    @Test
    public void testAuthorKey() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet10));
        assertConnectionExists(followsGraph, "ivan", "alyssa");
        assertConnectionNotExist(followsGraph, "ivan", "ivan");
    }
    
    
    //This test covers tweets.size=more,mention object=mention others,key=author,case=differ
    @Test
    public void testMoreSize() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet2,tweet3));
        assertConnectionExists(followsGraph, "charlie","bbitdiddle" );
        assertConnectionExists(followsGraph, "bbitdiddle","alyssa" );
        assertConnectionExists(followsGraph, "charlie","alyssa" );
    }
    
    //This test covers tweets.size=1,mention object=mention others,key=author,case=same,repeat=same tweet
    @Test
    public void testRepeatSameTweet() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet1));
        assertConnectionExists(followsGraph, "alyssa","bbitdiddle" );
        
    }
    
    //This test covers tweets.size=more,mention object=mention others,key=author,case=same,repeat=differ
    @Test
    public void testRepeatDifferTweet() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(Arrays.asList(tweet3,tweet6));
        assertConnectionExists(followsGraph, "charlie","bbitdiddle" );
        assertConnectionExists(followsGraph, "charlie","alyssa" );
    }
    
    //This test covers tweets.size=more,mention object=all case,key=all case,case=all case,repeat=differ or same
    @Test
    public void testTotalTweets() {
        Map<String, Set<String>> followsGraph = SocialNetwork.guessFollowsGraph(totalTweets);
        
        assertConnectionExists(followsGraph, "alyssa", "bbitdiddle");
        assertConnectionExists(followsGraph, "bbitdiddle", "alyssa");
        
        assertConnectionExists(followsGraph, "charlie", "alyssa");
        assertConnectionExists(followsGraph, "charlie", "bbitdiddle");
        
        assertConnectionExists(followsGraph, "alyssa", "evans");
        assertConnectionExists(followsGraph, "george", "alyssa");
        
        assertConnectionNotExist(followsGraph, "alyssa", "ivan");
        assertConnectionNotExist(followsGraph, "frank", "charlie");
        assertConnectionNotExist(followsGraph, "alyssa", "alyssa");
    }
    
    
    // 1. Empty Graph
    public final Map<String, Set<String>> emptyGraph = new HashMap<>();

 // 2. Keys with Empty Sets (Authors with no mentions)
    private final Map<String, Set<String>> noMentionsGraph = new HashMap<String, Set<String>>() {{
        put("alyssa", new HashSet<>());
        put("bbitdiddle", new HashSet<>());
    }};

    // 3. Tied Followers (A and B both followed by C)
    private final Map<String, Set<String>> tiedGraph = new HashMap<String, Set<String>>() {{
        put("charlie", new HashSet<>(Arrays.asList("alyssa", "bbitdiddle")));
    }};

    // 4. Varying Follower Counts (A followed by 2 people, B followed by 1)
    private final Map<String, Set<String>> varyingGraph = new HashMap<String, Set<String>>() {{
        put("charlie", new HashSet<>(Arrays.asList("alyssa", "bbitdiddle")));
        put("evans", new HashSet<>(Arrays.asList("alyssa")));
    }};

    // 5. Case-Insensitive Multi-User
    private final Map<String, Set<String>> caseInsensitiveGraph = new HashMap<String, Set<String>>() {{
        put("charlie", new HashSet<>(Arrays.asList("alyssa")));
        put("bbitdiddle", new HashSet<>(Arrays.asList("Alyssa")));
        put("evans", new HashSet<>(Arrays.asList("george")));
    }};
    
    /*
     * Test strategy for influencers:
     * 1. input.size():0,1 or more
     * 2. tied followers:true or false
     * 3. case-insensitivity:same or differ
     * 4. set:empty or nonempty
     */
    
    //This test covers input.size()=0
    @Test
    public void testEmptyInput() {
     
        List<String> influencers = SocialNetwork.influencers(emptyGraph);
        assertTrue("expected empty list", influencers.isEmpty());
    }
    
    //This test covers input.size()=2,set=empty
    @Test
    public void testEmptySet() {
     
        List<String> influencers = SocialNetwork.influencers(noMentionsGraph);
        assertEquals("expected 2 users with 0 followers", 2, influencers.size());
        assertTrue("should contain alyssa", influencers.stream().anyMatch(u -> u.equalsIgnoreCase("alyssa")));
        assertTrue("should contain bbitdiddle", influencers.stream().anyMatch(u -> u.equalsIgnoreCase("bbitdiddle")));
    }
    
    //This test covers input.size()=1,tied=true
    @Test
    public void testTiedFollowers() {
     
        List<String> influencers = SocialNetwork.influencers(tiedGraph);
        assertTrue(influencers.size() >= 2);
        
        Set<String> topTwo = new HashSet<>();
        topTwo.add(influencers.get(0).toLowerCase());
        topTwo.add(influencers.get(1).toLowerCase());
        
        assertTrue(topTwo.contains("alyssa"));
        assertTrue(topTwo.contains("bbitdiddle"));
    }
    
    //This test covers input.size()=more,tied=false
    @Test
    public void testVaringRank() {
        List<String> influencers = SocialNetwork.influencers(varyingGraph);
        assertEquals(influencers.get(0).toLowerCase(),"alyssa");
        assertEquals(influencers.get(1).toLowerCase(),"bbitdiddle");
        
    }
    //This test covers input.size()=more,tied=false,case=differ
    @Test
    public void testDifferentCase() {
        List<String> influencers = SocialNetwork.influencers(caseInsensitiveGraph);
        assertEquals(influencers.get(0).toLowerCase(),"alyssa");
        assertEquals(influencers.get(1).toLowerCase(),"george");
        
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
