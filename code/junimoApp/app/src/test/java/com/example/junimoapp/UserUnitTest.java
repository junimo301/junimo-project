package com.example.junimoapp;

import com.example.junimoapp.models.Event;
import com.example.junimoapp.models.User;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.ArrayList;

/**
 * unit tests related to UserHomeActivity logic
 * tests
 */
public class UserUnitTest {
    //test that user is added when event.enroll is called
    @Test
    public void UserAddedToWaitlist() {

        String deviceId = "device123";

        Event event = new Event(
                "Swimming Lessons",    // title
                "Beginner swim class", // description
                "2025-01-01",          // startDate
                "2025-01-07",          // endDate
                "2025-01-15",          // dateEvent
                20,                    // maxCapacity
                0,                     // waitingListLimit (0 = no limit)
                60.0,                  // price
                null,                  // geoLocation
                "",                    // poster
                "event-uuid-001",      // eventID
                "Rec Centre",          // eventLocation
                "organizer-device-id"  // organizerID
        );
        ArrayList<String> testList=event.getWaitList();
        testList.add(deviceId);

        event.enrollInWaitList(deviceId);

        assertEquals(event.getWaitList(),testList);
    }
    //test that user is removed when event.remove is called
    @Test
    public void UserRemovedFromWaitlist() {

        String deviceId = "device456";
        Event event = new Event(
                "Swimming Lessons",    // title
                "Beginner swim class", // description
                "2025-01-01",          // startDate
                "2025-01-07",          // endDate
                "2025-01-15",          // dateEvent
                20,                    // maxCapacity
                0,                     // waitingListLimit (0 = no limit)
                60.0,                  // price
                null,                  // geoLocation
                "",                    // poster
                "event-uuid-001",      // eventID
                "Rec Centre",          // eventLocation
                "organizer-device-id"  // organizerID
        );
        ArrayList<String> testList=event.getWaitList();
        testList.add(deviceId);

        event.enrollInWaitList(deviceId);
        assertEquals(event.getWaitList(),testList);

        event.removeFromWaitList(deviceId);
        testList.remove(deviceId);

        assertEquals(event.getWaitList(),testList);
    }
}
