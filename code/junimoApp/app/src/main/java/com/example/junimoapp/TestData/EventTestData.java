
package com.example.junimoapp.TestData;

import com.example.junimoapp.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventTestData {
    /*
    * TEST DATA: fake event details to test myEvents
    *

 */
    public static List<Event> getEvents() {

        List<Event> events = new ArrayList<>();

        events.add(new Event(
                "Music Festival",
                "Outdoor concert with local bands",
                "2026-06-01",
                "2026-06-10",
                "2026-06-10",
                50,
                60,
                30.00,
                false,
                "poster1",
                "event001",
                "Downtown Park",
                "id:8910",
                "Entertainment"
        ));

        events.add(new Event(
                "Hackathon",
                "24 hour coding competition",
                "2026-07-05",
                "2026-07-12",
                "2026-07-15",
                100,
                150,
                50.00,
                true,
                "poster2",
                "event002",
                "University Hall",
                "id:1234",
                "Education"
        ));

        events.add(new Event(
                "Art Workshop",
                "Learn watercolor painting",
                "2026-05-15",
                "2026-05-20",
                "2026-06-10",
                10,
                15,
                20.00,
                true,
                "poster3",
                "event003",
                "Community Center",
                "id:5678",
                "None"
        ));

        return events;
    }
}
