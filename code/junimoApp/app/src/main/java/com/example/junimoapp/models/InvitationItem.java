package com.example.junimoapp.models;
public class InvitationItem {

    String eventId;
    String title;

    public InvitationItem(String eventId, String title) {
        this.eventId = eventId;
        this.title = title;
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }
}