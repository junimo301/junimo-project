package com.example.junimoapp;


import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.junimoapp.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Unit tests for event search/filtering
 */
public class SearchUnitTest {
    private List<Event> allEvents;
    private SimpleDateFormat dateFormat;

    /**
     * Sets up a mock list of events before running the tests
     */
    @Before
    public void setUp() {
        allEvents = new ArrayList<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Event event1 = new Event("Concert", "Music outdoors", "2026-05-01", "2026-05-10",
                "2026-06-15", 500, 50, 25.0, false, "",
                "e1", "Park", "org1", "Entertainment");

        Event event2 = new Event("Hackathon", "Coding competition", "2026-04-01", "2026-05-10",
                "2026-04-10", 30, 10, 0.0, true, "",
                "e2", "Library", "org2", "Professional");

        Event event3 = new Event("Wrestling Tournament", "Local competition", "2026-07-01", "2026-07-15",
                "2026-07-20", 100, 20, 5.0, false, "",
                "e3", "Stadium", "org3", "Athletics");

        allEvents.add(event1);
        allEvents.add(event2);
        allEvents.add(event3);
    }

    /**
     * US 01.01.04
     * As an entrant, I want to search for events by keyword
     * Tests general search keyword filtering
     */
    @Test
    public void testFilterByKeyword() {
        String keyword = "music"; //should only match event 1
        List<Event> filteredList = new ArrayList<>();

        //simulate filtering logic from EventSearchActivity
        for (Event event : allEvents) {
            String title = event.getTitle().toLowerCase();
            String desc = event.getDescription().toLowerCase();
            if (title.contains(keyword.toLowerCase()) || desc.contains(keyword.toLowerCase())) {
                filteredList.add(event);
            }
        }

        //verify only Concert is returned
        assertEquals(1, filteredList.size());
        assertEquals("Concert", filteredList.get(0).getTitle());
    }

    /**
     * US 01.01.05
     * As an entrant, I want to search by tag
     * Tests filtering by tag only returns events with that tag
     */
    @Test
    public void testFilterByTag() {
        String selectedTag = "Professional"; //should only match event 2
        List<Event> filteredList = new ArrayList<>();

        //Simulate tag filtering from EventSearchActivity
        for (Event event : allEvents) {
            String eventTag = event.getTag() != null ? event.getTag() : "";
            if (eventTag.equals(selectedTag)) {
                filteredList.add(event);
            }
        }

        //verify only the Hackathon is returned
        assertEquals(1, filteredList.size());
        assertEquals("Hackathon", filteredList.get(0).getTitle());
    }

    /**
     * US 01.01.06
     * As an entrant, I want to search by date range
     * Tests filtering by start and end date
     */
    @Test
    public void testFilterByDateRange() throws ParseException {
        //set range from May 1st to June 30th
        Date filterStartDate = dateFormat.parse("2026-05-01");
        Date filterEndDate = dateFormat.parse("2026-06-30");

        List<Event> filteredList = new ArrayList<>();

        //Simulate date range filtering from EventSearchActivity
        for (Event event : allEvents) {
            Date eventDate = dateFormat.parse(event.getDateEvent());
            boolean matchesDate = true;

            if (filterStartDate != null && eventDate.before(filterStartDate)) {
                matchesDate = false;
            }
            if (filterEndDate != null && eventDate.after(filterEndDate)) {
                matchesDate = false;
            }
            if (matchesDate) {
                filteredList.add(event);
            }
        }

        //verify only Concert is returned
        assertEquals(1, filteredList.size());
        assertEquals("Concert", filteredList.get(0).getTitle());
    }

    /**
     * US 01.01.06
     * As an entrant, I want to search by capacity
     * Tests filtering by capacity range
     */
    @Test
    public void testFilterByMaxCapacity() {
        int filterMaxCap = 50; //should only include event 2
        List<Event> filteredList = new ArrayList<>();

        //simulate capacity filtering logic in EventSearchActivity
        for (Event event : allEvents) {
            if (event.getMaxCapacity() <= filterMaxCap) {
                filteredList.add(event);
            }
        }

        //verify only Hackathon is returned
        assertEquals(1, filteredList.size());
        assertEquals("Hackathon", filteredList.get(0).getTitle());
    }


}
