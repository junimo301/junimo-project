package com.example.junimoapp.models;

/**
 * Class to get and set the current user.
 */
public class UserSession {
    /*
     * Tracking login
     *
     */
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        UserSession.currentUser = currentUser;
    }
}