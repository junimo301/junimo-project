package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.models.Event;

import java.util.List;

public class ListOfMyEvents extends RecyclerView.Adapter<ListOfMyEvents.EventViewHolder> {
    private List<Event> eventList;
    public ListOfMyEvents(List<Event> eventList) {
        this.eventList = eventList;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView description;
        Button editEventButton;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            editEventButton = itemView.findViewById(R.id.edit_event_button);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_of_my_events, parent, false);
        return new EventViewHolder(v);
    }

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

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


}
