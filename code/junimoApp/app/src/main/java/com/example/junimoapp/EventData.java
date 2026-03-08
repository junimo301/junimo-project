package com.example.junimoapp;

import java.util.ArrayList;
import java.util.List;

public class EventData {
    /*
    * All created events data
    * */
    private static List<OrganizerEvent> events = new ArrayList<>();

    public static OrganizerEvent searchEventID(String eventID) {
        for (OrganizerEvent event : events) {
            if (event.getEventID().equals(eventID)) {
                return event;
            }
        }
        return null;
    }
    public static void addOrEditEvent(OrganizerEvent event) {
        OrganizerEvent created = searchEventID(event.getEventID());
        if (created != null) {
            events.remove(created);
        }
        events.add(event);
    }
    public static List<OrganizerEvent> listOfEvents() {
        return new ArrayList<>(events);
    }



}
