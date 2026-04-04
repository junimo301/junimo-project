package com.example.junimoapp;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.junimoapp.adapters.EventSearchAdapter;
import com.example.junimoapp.firebase.FirebaseManager;
import com.example.junimoapp.models.Event;
import com.example.junimoapp.utils.BaseActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for searching and filtering public events
 * Implements:
 * US 01.01.04: Search by keyword
 * US 01.01.05: Search by tag/category
 * US 01.01.06: Search by date/capacity range
 */
public class EventSearchActivity extends BaseActivity {
    private EditText keywordInput, startDateInput, endDateInput;
    private EditText minCapacityInput, maxCapacityInput; //capacity ranges
    private Spinner tagSpinner;
    private Button applySearchButton, clearFiltersButton;
    private TextView backButton;
    private ListView resultsListView;

    private FirebaseFirestore db;
    private ArrayList<Event> allPublicEvents; //all events fetched from firebase (all public events)
    private ArrayList<Event> filteredEvents; //all events currently matching filters
    private EventSearchAdapter adapter;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);

        //Initialize UI elements
        keywordInput       = findViewById(R.id.searchKeywordInput);
        tagSpinner         = findViewById(R.id.searchTagSpinner);
        startDateInput     = findViewById(R.id.searchStartDate);
        endDateInput       = findViewById(R.id.searchEndDate);
        minCapacityInput   = findViewById(R.id.searchMinCapacity);
        maxCapacityInput   = findViewById(R.id.searchMaxCapacity);
        applySearchButton  = findViewById(R.id.applySearchButton);
        clearFiltersButton = findViewById(R.id.clearFiltersButton);
        backButton         = findViewById(R.id.searchBackButton);
        resultsListView    = findViewById(R.id.searchResultsList);

        //Initialize data structures
        db = FirebaseManager.getDB();
        allPublicEvents = new ArrayList<>();
        filteredEvents = new ArrayList<>();
        adapter = new EventSearchAdapter(this, filteredEvents);
        resultsListView.setAdapter(adapter);

        //Setup date pickers for the EditTexts
        setupDatePicker(startDateInput);
        setupDatePicker(endDateInput);

        //Load all public events initially
        loadAllEvents();

        //setup button listeners
        backButton.setOnClickListener(v -> finish());

        clearFiltersButton.setOnClickListener(v -> {
            keywordInput.setText("");
            tagSpinner.setSelection(0); //reset to "None"
            startDateInput.setText("");
            endDateInput.setText("");
            minCapacityInput.setText("");
            maxCapacityInput.setText("");
            //reset list to show all events
            filteredEvents.clear();
            filteredEvents.addAll(allPublicEvents);
            adapter.notifyDataSetChanged();
        });

        applySearchButton.setOnClickListener(v -> applyFilters());

        //handle clicking on a search result
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            Event clickedEvent = filteredEvents.get(position);
            Intent intent = new Intent(EventSearchActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", clickedEvent.getEventID());
            startActivity(intent);
        });
    }

    /**
     * Attaches a DatePickerDialog to an EditText so the user can easily select a date
     * @param editText the EditText to attach the picker to
     */
    private void setupDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EventSearchActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        //format selected date to DB format
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);
                        editText.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    /**
     * Fetches all public events from firestore
     * US 02.01.02 excludes private events
     */
    private void loadAllEvents() {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allPublicEvents.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        //skip private events
                        Boolean isPrivate = doc.getBoolean("isPrivate");
                        if (Boolean.TRUE.equals(isPrivate)) continue;

                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        String startDate = doc.getString("startDate");
                        String endDate = doc.getString("endDate");
                        String dateEvent = doc.getString("dateEvent");

                        //handle potential nulls for numbers
                        Long maxCapLong = doc.getLong("maxCapacity");
                        int maxCapacity = maxCapLong != null ? maxCapLong.intValue() : 0;

                        Long waitListLong = doc.getLong("waitingListLimit");
                        int waitingListLimit = waitListLong != null ? waitListLong.intValue() : 0;

                        Double priceDouble = doc.getDouble("price");
                        double price = priceDouble != null ? priceDouble : 0.0;

                        GeoPoint geoLocation = doc.getGeoPoint("geoLocation");
                        String poster = doc.getString("poster");
                        String eventID = doc.getString("eventID");
                        String eventLocation = doc.getString("eventLocation");
                        String organizerID = doc.getString("organizerID");

                        //read tag, default to empty string if no tag present
                        String tag = doc.getString("tag");
                        if (tag == null) tag = "";

                        Event event = new Event(title, description, startDate, endDate,
                                dateEvent, maxCapacity, waitingListLimit, price,
                                geoLocation, poster, eventID, eventLocation, organizerID, tag);

                        allPublicEvents.add(event);
                    }

                    //show all events initially
                    filteredEvents.clear();
                    filteredEvents.addAll(allPublicEvents);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("EventSearch", "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Applies keyword, tag, date range and capacity range filters to the list of
     * all events. Only events matching all criteria are kept
     * Empty criteria are ignored: optional
     */
    private void applyFilters() {
        String keyword = keywordInput.getText().toString().trim().toLowerCase();
        String selectedTag = tagSpinner.getSelectedItem().toString();
        String startStr = startDateInput.getText().toString();
        String endStr = endDateInput.getText().toString();
        String minCapStr = minCapacityInput.getText().toString().trim();
        String maxCapStr = maxCapacityInput.getText().toString().trim();

        Date filterStartDate = null;
        Date filterEndDate = null;

        //parse dates if provided
        try {
            if (!startStr.isEmpty()) filterStartDate = dateFormat.parse(startStr);
            if (!endStr.isEmpty()) filterEndDate = dateFormat.parse(endStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }

        //parse capacity bounds if provided
        // -1 as a sentinel value for no bounds set
        int filterMinCap = -1;
        int filterMaxCap = -1;
        try {
            if (!minCapStr.isEmpty()) filterMinCap = Integer.parseInt(minCapStr);
            if (!maxCapStr.isEmpty()) filterMaxCap = Integer.parseInt(maxCapStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid capacity value", Toast.LENGTH_SHORT).show();
            return;
        }

        filteredEvents.clear();

        for (Event event : allPublicEvents) {
            boolean matchesKeyword = true;
            boolean matchesTag = true;
            boolean matchesDate = true;
            boolean matchesCapacity = true;

            //Check keyword (searches title and description)
            if (!keyword.isEmpty()) {
                String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";
                String desc = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
                if (!title.contains(keyword) && !desc.contains(keyword)) {
                    matchesKeyword = false;
                }
            }

            //Check tag
            if (!selectedTag.equals("None")) {
                String eventTag = event.getTag() != null ? event.getTag() : "";
                if (!eventTag.equals(selectedTag)) {
                    matchesTag = false;
                }
            }

            //Check date range (event date falls within specified range)
            if (filterStartDate != null || filterEndDate != null) {
                try {
                    String eventDateStr = event.getDateEvent();
                    if (eventDateStr != null && !eventDateStr.isEmpty()) {
                        Date eventDate = dateFormat.parse(eventDateStr);
                        if (eventDate != null) {
                            if (filterStartDate != null && eventDate.before(filterStartDate)) {
                                matchesDate = false;
                            }
                            if (filterEndDate != null && eventDate.after(filterEndDate)) {
                                matchesDate = false;
                            }
                        } else {
                            matchesDate = false; //can't parse event date
                        }
                    } else {
                        matchesDate = false; // no date set on event
                    }
                } catch (ParseException e) {
                    matchesDate = false; //unparseable event date
                }
            }

            //Check capacity range
            //filterMinCap == -1 means no lower bound set, skip
            //filterMaxCap == -1 means no upper bound set, skip
            if (filterMinCap != -1 || filterMaxCap != -1) {
                int eventCap = event.getMaxCapacity();
                if (filterMinCap != -1 && eventCap < filterMinCap) {
                    matchesCapacity = false;
                }
                if (filterMaxCap != -1 && eventCap > filterMaxCap) {
                    matchesCapacity = false;
                }
            }

            //if it passes all filters, add it to search results
            if (matchesKeyword && matchesTag && matchesDate && matchesCapacity) {
                filteredEvents.add(event);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredEvents.isEmpty()) {
            Toast.makeText(this, "No events found matching criteria", Toast.LENGTH_SHORT).show();
        }
    }
}
