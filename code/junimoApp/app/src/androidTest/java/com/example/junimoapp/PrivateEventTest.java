package com.example.junimoapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.junimoapp.Organizer.CreateEvent;
import com.example.junimoapp.Organizer.PrivateInviteActivity;
import com.example.junimoapp.models.Event;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for private event creation and invitations.
 *
 * Stories covered in this file:
 *   - US 02.01.02: Organizer creates a private event (no public listing, no QR code)
 *   - US 02.01.03: Organizer invites specific entrants to a private event
 *                  by searching via name, phone number and/or email
 */
@RunWith(AndroidJUnit4.class)
public class PrivateEventTest {

    @Rule
    public ActivityScenarioRule<CreateEvent> activityRule =
            new ActivityScenarioRule<>(CreateEvent.class);

    // ── US 02.01.02 ───────────────────────────────────────────────────────

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
        onView(withId(R.id.QR_code_button))
                .perform(scrollTo())
                .check(matches(isEnabled()));

        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click());

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
        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click());
        onView(withId(R.id.check_private_event))
                .perform(click());

        onView(withId(R.id.QR_code_button))
                .check(matches(isEnabled()));
    }

    /**
     * US 02.01.02
     * Unit test for the Event model — verifies that:
     *   1. isPrivate defaults to false (events are public by default)
     *   2. setPrivate(true) correctly flips the flag to true
     */
    @Test
    public void eventModel_isPrivateDefaultsFalse_thenCanBeSetTrue() {
        Event event = new Event(
                "Test Event", "", "", "", "2025-01-01",
                10, 5, 0.0,
                new GeoPoint(0, 0), "", "test-id-123",
                "Test Location", "organizer-abc"
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
     * must not generate a QR code — the button should remain disabled.
     */
    @Test
    public void clickingQRWhilePrivate_buttonRemainsDisabled() {
        onView(withId(R.id.check_private_event))
                .perform(scrollTo(), click());

        onView(withId(R.id.QR_code_button))
                .check(matches(isNotEnabled()));
    }

    // ── US 02.01.03 ───────────────────────────────────────────────────────

    /**
     * US 02.01.03
     * PrivateInviteActivity should open and display the search field
     * when launched with valid event extras.
     */
    @Test
    public void privateInviteActivity_launchesAndShowsSearchField() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                PrivateInviteActivity.class
        );
        intent.putExtra("eventId", "test-event-id");
        intent.putExtra("eventTitle", "Test Private Event");

        try (ActivityScenario<PrivateInviteActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.search_field))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * US 02.01.03
     * The results recycler should be visible when the invite screen opens,
     * ready to display results once the organizer starts typing.
     */
    @Test
    public void privateInviteActivity_resultsRecyclerIsVisible() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                PrivateInviteActivity.class
        );
        intent.putExtra("eventId", "test-event-id");
        intent.putExtra("eventTitle", "Test Private Event");

        try (ActivityScenario<PrivateInviteActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.results_recycler))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * US 02.01.03
     * The back button should be visible so the organizer can return
     * without inviting anyone.
     */
    @Test
    public void privateInviteActivity_backButtonIsVisible() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                PrivateInviteActivity.class
        );
        intent.putExtra("eventId", "test-event-id");
        intent.putExtra("eventTitle", "Test Private Event");

        try (ActivityScenario<PrivateInviteActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.back_button_private))
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * US 02.01.03
     * Typing in the search field should not crash the app.
     * Smoke test confirming the TextWatcher is wired up correctly.
     */
    @Test
    public void privateInviteActivity_typingInSearchDoesNotCrash() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                PrivateInviteActivity.class
        );
        intent.putExtra("eventId", "test-event-id");
        intent.putExtra("eventTitle", "Test Private Event");

        try (ActivityScenario<PrivateInviteActivity> scenario =
                     ActivityScenario.launch(intent)) {
            onView(withId(R.id.search_field))
                    .perform(typeText("John"));
            onView(withId(R.id.search_field))
                    .check(matches(isDisplayed()));
        }
    }
}

