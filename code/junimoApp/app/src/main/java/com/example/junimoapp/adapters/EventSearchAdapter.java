package com.example.junimoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Adapter for displaying Event objects in a ListView during a search
 * Binds Event data to item_search_result.xml layout
 */
public class EventSearchAdapter extends ArrayAdapter<Event> {
    private Context context;
    private ArrayList<Event> events;

    /**
     * Constructor for EventSearchAdapter
     *
     * @param context the current context
     * @param events list of events to disply
     */
    public EventSearchAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        this.context = context;
        this.events = events;
    }

    /**
     * Provides a view for an AdapterView (ListView)
     *
     * @param position position of an item in the adapter's dataset
     * @param convertView old view to reuse if possible
     * @param parent parent the view will eventually be attached to
     * @return View corresponding to data at specified position
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //inflate layout if it doesn't yet exist
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
        }

        //get event object for this position
        Event currentEvent = events.get(position);

        //Find textviews in layout
        TextView titleText = convertView.findViewById(R.id.search_item_title);
        TextView dateText = convertView.findViewById(R.id.search_item_date);
        TextView tagText = convertView.findViewById(R.id.search_item_tag);
        TextView capacityText = convertView.findViewById(R.id.search_item_capacity);

        //populate TextViews with Event's data
        titleText.setText(currentEvent.getTitle());
        dateText.setText(context.getString(R.string.date) + currentEvent.getDateEvent());

        //Display tag if it exists, otherwise "No Tag"
        String tag = currentEvent.getTag();
        if (tag != null && !tag.isEmpty() && !tag.equals(context.getString(R.string.none))) {
            tagText.setText(tag);
        } else {
            tagText.setText(R.string.no_tag);
        }

        //Display event max capacity
        capacityText.setText(context.getString(R.string.capacity_colon) + currentEvent.getMaxCapacity());

        return convertView;
    }
}
