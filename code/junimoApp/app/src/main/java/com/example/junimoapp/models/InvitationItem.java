package com.example.junimoapp.models;

/**
 * represents an invitation to an event
 * events are identified via ID and name/title
 */
public class InvitationItem {

    String eventId;
    String title;

    /**
     * constructs a new InvitationItem with the given event ID and title
     * @param eventId
     * the unique identifier of the event
     * @param title
     * the title of the event
     */
    public InvitationItem(String eventId, String title) {
        this.eventId = eventId;
        this.title = title;
    }
    /**
     * returns the event ID associated with this invitation
     * @return
     * the event ID
     */
    public String getEventId() {
        return eventId;
    }
    /**
     * returns the title of the event for this invitation
     * @return
     * the event title
     */
    public String getTitle() {
        return title;
    }
}