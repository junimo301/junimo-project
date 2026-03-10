package com.example.junimoapp.TestData;

import com.example.junimoapp.Organizer.OrganizerEvent;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class EventTestData {
    /*
    * TEST DATA: fake event details to test myEvents
    * NOT CURRENTLY IMPLEMENTED
    * */
    public static List<OrganizerEvent> getEvents() {

        List<OrganizerEvent> events = new ArrayList<>();

        events.add(new OrganizerEvent(
                "Music Festival",
                "Outdoor concert with local bands",
                "2026-06-01",
                "2026-06-10",
                200,
                50,
                25.00,
                new GeoPoint(53.5461,-113.4938),
                "poster1",
                "event001",
                "Downtown Park"
        ));

        events.add(new OrganizerEvent(
                "Hackathon",
                "24 hour coding competition",
                "2026-07-05",
                "2026-07-12",
                100,
                20,
                10.00,
                new GeoPoint(53.5232,-113.5263),
                "poster2",
                "event002",
                "University Hall"
        ));

        events.add(new OrganizerEvent(
                "Art Workshop",
                "Learn watercolor painting",
                "2026-05-15",
                "2026-05-20",
                30,
                10,
                15.00,
                new GeoPoint(53.5400,-113.5000),
                "poster3",
                "event003",
                "Community Center"
        ));

        return events;
    }
}