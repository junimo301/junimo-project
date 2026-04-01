package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.junimoapp.models.Event;

import com.example.junimoapp.R;

import java.util.List;

/**
 * Views list of the organizers events
 * shows event title, descrition and an edit button to edit the event
 *  - edit buttons opens CreateEvent screen
 * */
public class ListOfMyEvents extends RecyclerView.Adapter<ListOfMyEvents.EventViewHolder> {

    /** events to display */
    private List<Event> eventList;

    /** constructor
     * @param eventList
     */
    public ListOfMyEvents(List<Event> eventList) {
        this.eventList = eventList;
    }

    /**
     * holds the view of each event item
     * shows event title, description and an edit button to edit the event
     * */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        Button editEventButton;
        ImageView backgroundPoster;

        /**
         * initializes the view for each event
         * @param itemView
         * */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            editEventButton = itemView.findViewById(R.id.edit_event_button);
            backgroundPoster = itemView.findViewById(R.id.background_poster);
        }
    }

    /**
     * creates a new view holder when needed
     * @param parent
     * @param viewType
     * @return new view holder
     * */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_my_events, parent, false);
        return new EventViewHolder(v);
    }

    /**
     * binds event data to view holder
     * @param holder
     * @param position
     * */
    @Override
    public void onBindViewHolder(@NonNull ListOfMyEvents.EventViewHolder holder, int position) {
        Event event =eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        holder.editEventButton.setOnClickListener(view -> {
            Intent editEvent = new Intent(view.getContext(), CreateEvent.class);
            editEvent.putExtra("event_ID", event.getEventID());
            view.getContext().startActivity(editEvent);
        });
        if (event.getPoster() != null && !event.getPoster().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(event.getPoster())
                    .centerCrop()
                    .into(holder.backgroundPoster);
        } else {
            holder.backgroundPoster.setImageResource(R.drawable.bg_event_tile);
        }
    }

    /**
     * returns the number of events
     * @return number of events
     * */
    @Override
    public int getItemCount() {
        return eventList.size();
    }


}
