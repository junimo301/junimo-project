package com.example.junimoapp.models;

/**
 * represents an invitation to an event
 * events are identified via ID and name/title
 */
public class InvitationItem {

    String eventID;
    String title;

    private boolean coOrganizerInvite;

    public boolean isCoOrganizerInvite() { return coOrganizerInvite; }
    public void setCoOrganizerInvite(boolean coOrganizerInvite) { this.coOrganizerInvite = coOrganizerInvite; }

    /**
     * constructs a new InvitationItem with the given event ID and title
     * @param eventID
     * the unique identifier of the event
     * @param title
     * the title of the event
     */
    public InvitationItem(String eventID, String title) {
        this.eventID = eventID;
        this.title = title;
    }
    /**
     * returns the event ID associated with this invitation
     * @return
     * the event ID
     */
    public String getEventId() {
        return eventID;
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