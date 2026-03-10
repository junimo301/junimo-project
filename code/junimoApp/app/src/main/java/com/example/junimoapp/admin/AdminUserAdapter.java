package com.example.junimoapp.admin;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;

import java.util.List;

/**
 * Adapter for browsing profiles as an admin, takes a list of UserItem objects
 * and makes them into the item_admin_user layout (so admins can browse them)
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    //simple class to hold the info for one user row
    public static class UserItem {
        public final String documentId; //Firestore doc ID (device ID)
        public final String name;
        public final String email;

        public UserItem(String documentId, String name, String email) {
            this.documentId = documentId;
            this.name = name;
            this.email = email;
        }
    }

    //interface to handle delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(UserItem user);
    }

    private final List<UserItem> userList;
    private final OnDeleteClickListener deleteListener;

    public AdminUserAdapter(List<UserItem> userList, OnDeleteClickListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout for one user row
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
    return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        //get user at curr pos, bind its data to the ViewHolder
        UserItem user = userList.get(position);
        holder.bind(user, deleteListener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //The ViewHolder holds the view for one user row
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView emailText;
        Button deleteButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.adminUserName);
            emailText = itemView.findViewById(R.id.adminUserEmail);
            deleteButton = itemView.findViewById(R.id.adminUserDeleteButton);
        }

        //helper for binding the data to the views
        public void bind(final UserItem user, final OnDeleteClickListener listener) {
            nameText.setText(user.name);
            emailText.setText(user.email);
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(user));
        }
    }
}
