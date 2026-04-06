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
 * adapter for displaying a list of invitations (normal, private, co-organizer)
 */
public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    private final List<InvitationItem> invitations;
    private final InvitationListener listener;

    /**
     *listener interface for accept/decline actions
     */
    public interface InvitationListener {
        void onAccept(String eventID, boolean isCoOrganizerInvite);
        void onDecline(String eventID, boolean isCoOrganizerInvite);
    }

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

        //accept button
        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAccept(item.getEventId(), item.isCoOrganizerInvite());
            }
        });

        //decline button
        holder.declineButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecline(item.getEventId(), item.isCoOrganizerInvite());
            }
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    /**
     * viewholder for each invitation row
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Button acceptButton;
        Button declineButton;

        public ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.invitationTitle);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }
}