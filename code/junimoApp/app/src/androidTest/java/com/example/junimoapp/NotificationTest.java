package com.example.junimoapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for notification features.
 *
 * Stories covered:
 *  - US 01.04.01: Notifications button is visible and navigates correctly
 *  - US 01.04.03: Notification opt-out switch works correctly in the UI
 */
@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    @Rule
    public ActivityScenarioRule<UserHomeActivity> activityRule =
            new ActivityScenarioRule<>(UserHomeActivity.class);

    // ── US 01.04.01 / 01.04.02 ───────────────────────────────────────────

    /**
     * US 01.04.01 / US 01.04.02
     * Verifies that the Notifications button is visible on the user home screen
     * so entrants can tap it to view their invite and lottery notifications.
     */
    @Test
    public void notificationsButton_isVisible() {
        onView(withId(R.id.notificationsButton))
                .check(matches(isDisplayed()));
    }

    /**
     * US 01.04.01 / US 01.04.02
     * Verifies the Notifications button has the correct label.
     */
    @Test
    public void notificationsButton_hasCorrectLabel() {
        onView(withId(R.id.notificationsButton))
                .check(matches(withText("Notifications")));
    }

    /**
     * US 01.04.01 / US 01.04.02
     * Tapping the Notifications button should open NotificationsActivity
     * without crashing. Smoke test for the navigation.
     */
    @Test
    public void notificationsButton_opensNotificationsScreen() {
        onView(withId(R.id.notificationsButton))
                .perform(click());
        // If NotificationsActivity opened successfully its recycler will be visible
        onView(withId(R.id.notificationsRecycler))
                .check(matches(isDisplayed()));
    }

    // ── US 01.04.03 ───────────────────────────────────────────────────────

    /**
     * US 01.04.03
     * Verifies the notification switch is visible on the user home screen.
     */
    @Test
    public void notifSwitch_isVisible() {
        onView(withId(R.id.notifSwitch))
                .check(matches(isDisplayed()));
    }

    /**
     * US 01.04.03
     * Verifies the switch starts in the ON (checked) state by default,
     * since notifications are enabled unless the user opts out.
     */
    @Test
    public void notifSwitch_isOnByDefault() {
        onView(withId(R.id.notifSwitch))
                .check(matches(isChecked()));
    }

    /**
     * US 01.04.03
     * Verifies that tapping the switch toggles it to OFF (unchecked),
     * which represents the user opting out of notifications.
     */
    @Test
    public void notifSwitch_canBeTurnedOff() {
        // Tap the switch to turn it off
        onView(withId(R.id.notifSwitch))
                .perform(click());

        onView(withId(R.id.notifSwitch))
                .check(matches(isNotChecked()));
    }

    /**
     * US 01.04.03
     * Verifies that tapping the switch twice returns it to ON,
     * confirming the user can re-enable notifications after opting out.
     */
    @Test
    public void notifSwitch_canBeToggledBackOn() {
        // Off
        onView(withId(R.id.notifSwitch)).perform(click());
        // Back on
        onView(withId(R.id.notifSwitch)).perform(click());

        onView(withId(R.id.notifSwitch))
                .check(matches(isChecked()));
    }
}
