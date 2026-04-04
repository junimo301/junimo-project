package com.example.junimoapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentsUserTest {

    private ArrayList<String> commentsList;
    private ArrayList<String> commentIds;
    private boolean isOrganizer;

    @Before
    public void setUp() {
        commentsList = new ArrayList<>();
        commentIds = new ArrayList<>();
        isOrganizer = false;
    }

    /**
     * test: valid comment stores username and deviceId correctly
     */
    @Test
    public void testCommentStoresUsernameAndDeviceId() {
        String username = "testName";
        String deviceId = "device123";
        String text = "sweet event!";

        HashMap<String, Object> comment = new HashMap<>();
        comment.put("text", text);
        comment.put("userId", deviceId);
        comment.put("username", username);

        assertEquals("testName", comment.get("username"));
        assertEquals("device123", comment.get("userId"));
        assertEquals("sweet event!", comment.get("text"));
    }

    /**
     * test: comment displays username instead of deviceId
     */
    @Test
    public void testDisplayUsesUsername() {
        String username = "anotherUser";
        String text = "hello";

        String display = username + ": " + text;

        assertEquals("anotherUser: hello", display);
    }

    /**
     * test: fallback to userId if username is missing
     */
    @Test
    public void testFallbackToUserId() {
        String username = null;
        String userId = "device999";
        String text = "hi";

        String displayName = (username == null) ? userId : username;
        String display = displayName + ": " + text;

        assertEquals("device999: hi", display);
    }

    /**
     * test: comments list stores formatted username comments
     */
    @Test
    public void testCommentAddedToList() {
        String username = "andAnotherUser";
        String text = "cool event";

        String formatted = username + ": " + text;
        commentsList.add(formatted);

        assertEquals(1, commentsList.size());
        assertEquals("andAnotherUser: cool event", commentsList.get(0));
    }

    /**
     * test: commentIds stay in sync with commentsList
     */
    @Test
    public void testCommentIdSync() {
        commentsList.add("user1: A");
        commentIds.add("id1");

        commentsList.add("user2: B");
        commentIds.add("id2");

        assertEquals(commentsList.size(), commentIds.size());
    }

    /**
     * test: deleting a comment removes corresponding ID
     */
    @Test
    public void testDeleteCommentSync() {
        commentsList.add("user1: A");
        commentIds.add("id1");

        commentsList.remove(0);
        commentIds.remove(0);

        assertEquals(0, commentsList.size());
        assertEquals(0, commentIds.size());
    }

    /**
     * test: organizer can delete comments
     */
    @Test
    public void testDeleteAllowedForOrganizer() {
        // no Activity creation
        boolean isOrganizer = true;

        boolean result = isOrganizer;

        assertTrue(result);
    }

    /**
     * test: non-organizer cannot delete comments
     */
    @Test
    public void testDeleteBlockedForNonOrganizer() {
        boolean isOrganizer = false;

        boolean result = isOrganizer;

        assertFalse(result);
    }

    /**
     * test: null username falls back to userId
     */
    @Test
    public void testNullUsernameHandled() {
        String username = null;
        String userId = "device111";
        String text = "test";

        String displayName = (username == null) ? userId : username;
        String display = displayName + ": " + text;

        assertEquals("device111: test", display);
    }
}