package com.example.junimoapp.Organizer;

import com.example.junimoapp.models.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all created events data
 * can search for an event by ID and add and edit
 * */
public class EventData {
    /*
    * All created events data
    * */

    /** stores all events */
    private static List<Event> events = new ArrayList<>();


    /**
     * searches event by ID
     * @param eventID event id
     * @return the event, otherwise returns null
     * */
    public static Event searchEventID(String eventID) {
        for (Event event : events) {
            if (event.getEventID().equals(eventID)) {
                return event;
            }
        }
        return null;
    }

    /**
     * adds or edits an event
     * @param event to add or delete
     * */
    public static void addOrEditEvent(Event event) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getEventID().equals(event.getEventID())) {
                events.set( i, event);
                return;
            }
        }
        events.add(event);
    }

    /** returns list of all the events
     * @return list of events
     * */
    public static List<Event> listOfEvents() {
        return new ArrayList<>(events);
    }

    /**
     * returns internal list of events
     * @returns internal list of events
     * */
    public static List<Event> getEvents() {
        return events;
    }
}
