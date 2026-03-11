package com.example.junimoapp.Organizer;

import com.example.junimoapp.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventData {
    /*
    * All created events data
    * */
    private static List<Event> events = new ArrayList<>();

    public static Event searchEventID(String eventID) {
        for (Event event : events) {
            if (event.getEventID().equals(eventID)) {
                return event;
            }
        }
        return null;
    }
    public static void addOrEditEvent(Event event) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventID().equals(event.getEventID())) {
                events.set( i, event);
                return;
            }
        }
        events.add(event);
    }
    public static List<Event> listOfEvents() {
        return new ArrayList<>(events);
    }



}
