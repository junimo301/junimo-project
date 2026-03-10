package com.example.junimoapp.Organizer;

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
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventID().equals(event.getEventID())) {
                events.set( i, event);
                return;
            }
        }
        events.add(event);
    }
    public static List<OrganizerEvent> listOfEvents() {
        return new ArrayList<>(events);
    }



}
