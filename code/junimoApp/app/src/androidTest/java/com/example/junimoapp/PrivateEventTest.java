package com.example.junimoapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.junimoapp.Organizer.CreateEvent;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented tests for private event creation.
 * More stories will be added to this file as we go.
 *
 * Stories covered in this file so far:
 *   - US 02.01.02: Organizer creates a private event (no public listing, no QR code)
 */
@RunWith(AndroidJUnit4.class)
public class PrivateEventUnitTest {

    @Rule
    public ActivityScenarioRule<CreateEvent> activityRule =
            new ActivityScenarioRule<>(CreateEvent.class);
    /**
     * US 02.01.02
     * The private checkbox should be unchecked by default so that
     * all events start as public unless the organizer explicitly opts in.
     */
    @Test
    public void privateCheckbox_isUncheckedByDefault() {
        onView(withId(R.id.check_private_event))
                .perform(scrollTo())
                .check(matches(isNotChecked()));
    }

    /**
     * US 02.01.02
     * Checking the private checkbox must disable the QR Generate button.
     * Private events must not have a promotional QR code (per the user story).
     */
    @Test
    public void checkingPrivate_disablesQRButton() {
        // Confirm QR button starts enabled for a public event
        onView(withId(R.id.QR_code_button))
                .perform(scrollTo())
                .check(matches(isEnabled()));

        // Check the private checkbox
        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click());

        // QR button must now be disabled
        onView(withId(R.id.QR_code_button))
                .check(matches(isNotEnabled()));
    }

    /**
     * US 02.01.02
     * Unchecking the private checkbox must re-enable the QR Generate button
     * so the organizer can generate a QR code for a public event.
     */
    @Test
    public void uncheckingPrivate_reEnablesQRButton() {
        // Check then immediately uncheck
        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click()); // check
        onView(withId(R.id.check_private_event))
                .perform(click()); // uncheck

        // QR button should be enabled again
        onView(withId(R.id.QR_code_button))
                .check(matches(isEnabled()));
    }

    /**
     * US 02.01.02
     * Unit test for the Event model — verifies that:
     *   1. isPrivate defaults to false (events are public by default)
     *   2. setPrivate(true) correctly flips the flag to true
     * Note: setPrivate() also writes to Firestore, which is acceptable in
     * an instrumented test running against the real device/emulator.
     */
    @Test
    public void eventModel_isPrivateDefaultsFalse_thenCanBeSetTrue() {
        Event event = new Event(
                "Test Event",   // title
                "",             // description
                "",             // startDate
                "",             // endDate
                "2025-01-01",   // dateEvent
                10,             // maxCapacity
                5,              // waitingListLimit
                0.0,            // price
                new GeoPoint(0, 0), // geoLocation
                "",             // poster
                "test-id-123",  // eventID
                "Test Location", // eventLocation
                "organizer-abc" // organizerID
        );

        assertFalse("New events should default to public (isPrivate = false)",
                event.isPrivate());

        event.setPrivate(true);

        assertTrue("After setPrivate(true), isPrivate should return true",
                event.isPrivate());
    }

    /**
     * US 02.01.02
     * Tapping the QR Generate button while the private checkbox is checked
     * must NOT generate a QR code — the button should remain disabled and
     * the action should be blocked at the click listener level.
     */
    @Test
    public void clickingQRWhilePrivate_doesNothing() {
        // Check private first
        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click());

        // The QR button is disabled so Espresso will throw if we try to click it —
        // we just confirm it is still disabled (not enabled after any side effect)
        onView(withId(R.id.QR_code_button))
                .check(matches(isNotEnabled()));
    }
}
