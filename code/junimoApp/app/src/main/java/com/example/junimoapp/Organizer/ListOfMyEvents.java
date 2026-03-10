package com.example.junimoapp.Organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            description = itemView.findViewById(android.R.id.text2);
        }
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListOfMyEvents.EventViewHolder holder, int position) {
        Event event =eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }


}
