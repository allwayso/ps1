/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import java.util.*;
import java.time.Instant;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures and specifications of these methods, but
 * you should implement their method bodies, and you may add new public or
 * private methods or classes if you like.
 */
public class Extract {

    /**
     * Get the time period spanned by tweets.
     * 
     * @param tweets list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of every
     *         tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            throw new IllegalArgumentException("tweets list cannot be empty");
        }
        Instant min_stamp = Instant.MAX;
        Instant max_stamp = Instant.MIN;
        for (Tweet tweet : tweets) {
            Instant current_stamp = tweet.getTimestamp();
            if (current_stamp.isBefore(min_stamp))
                min_stamp = current_stamp;
            if (current_stamp.isAfter(max_stamp))
                max_stamp = current_stamp;
        }
        Timespan result = new Timespan(min_stamp, max_stamp);
        return result;
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets. A
     *         username-mention is "@" followed by a Twitter username (as defined by
     *         Tweet.getAuthor()'s spec). The username-mention cannot be immediately
     *         preceded or followed by any character valid in a Twitter username.
     *         For this reason, an email address like bitdiddle@mit.edu does NOT
     *         contain a mention of the username mit. Twitter usernames are
     *         case-insensitive, and the returned set may include a username at most
     *         once.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> users = new HashSet<String>();
        for (Tweet t : tweets) {
            String text = t.getText();
            if (!text.contains("@"))
                continue;
            int idx = text.indexOf('@');
            while (idx != -1) {
                if (idx == 0 || !isValidChar(text.charAt(idx - 1))) {
                    int end = idx + 1;
                    while (end < text.length() && isValidChar(text.charAt(end)))
                        end++;
                    String name = text.substring(idx + 1, end);
                    if (end > idx + 1) {
                        name = name.toLowerCase();
                        users.add(name);
                    }
                }
                idx = text.indexOf('@', idx + 1);
            }
        }
        return users;
    }

    /*
     * Judge the legality of character
     * 
     * @param c:the character to be judged
     * 
     * @return whether the character is among letters (A-Z or a-z), digits,
     * underscore ("_"), or hyphen ("-")
     */
    
    public static boolean isValidChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_') || (c == '-');
    }
}
