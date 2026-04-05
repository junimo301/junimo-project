package com.example.junimoapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;
import com.example.junimoapp.models.InvitationItem;

import java.util.List;

/**
 * adapter for the invitation stuff
 */

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    public interface InvitationListener {
        void onAccept(String eventID);
        void onDecline(String eventID);
    }

    private final List<InvitationItem> invitations;
    private final InvitationListener listener;

    public InvitationAdapter(List<InvitationItem> invitations, InvitationListener listener) {
        this.invitations = invitations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InvitationItem item = invitations.get(position);
        holder.titleText.setText(item.getTitle());

        holder.acceptButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onAccept(invitations.get(pos).getEventId());
            }
        });

        holder.declineButton.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDecline(invitations.get(pos).getEventId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    //static nested ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Button acceptButton;
        Button declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.invitationTitle);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}