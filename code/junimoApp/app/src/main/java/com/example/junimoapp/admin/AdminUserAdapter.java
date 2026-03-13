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
 * using a RecyclerView
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    /**
     * simple class to hold the info for one user row, containing all of their info
     * for ease of use
     */
    public static class UserItem {
        public final String documentId; //Firestore doc ID (device ID)
        public final String name;
        public final String email;

        /**
         * makes a new UserItem
         * @param documentId Firestore doc ID (device ID)
         * @param name user's name
         * @param email user's email
         */
        public UserItem(String documentId, String name, String email) {
            this.documentId = documentId;
            this.name = name;
            this.email = email;
        }
    }

    /**
     * interface to handle delete button clicks
     */
    public interface OnDeleteClickListener {
        /**
         * Called when delete button is clicked
         * @param user UserItem in the associated clicked row
         */
        void onDeleteClick(UserItem user);
    }

    private List<UserItem> userList;
    private final OnDeleteClickListener deleteListener;

    /**
     * Constructs the adapter
     * @param userList list of users to display
     * @param deleteListener listener to handle delete button clicks
     */
    public AdminUserAdapter(List<UserItem> userList, OnDeleteClickListener deleteListener) {
        this.userList = userList;
        this.deleteListener = deleteListener;
    }

    /**
     * For when the RecyclerView needs a UserViewHolder to represent the item
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return a new UserViewHolder w/ View of given view type
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout for one user row
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
    return new UserViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data at specified position
     * @param holder ViewHolder representing the contents of the item at the given pos in the data
     * @param position pos of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        //get user at curr pos, bind its data to the ViewHolder
        UserItem user = userList.get(position);
        holder.bind(user, deleteListener);
    }

    /**
     * Returns total number of items in the dataset held by the adapter
     * @return total number of items in the adapter
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * The ViewHolder class, holds the view for one user row, cachine the view
     * references
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        //UI elements for a single row
        TextView nameText;
        TextView emailText;
        Button deleteButton;

        /**
         * Constructs the ViewHolder
         * @param itemView the view for a single row
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            //find/cache the views in the item layout
            nameText = itemView.findViewById(R.id.adminUserName);
            emailText = itemView.findViewById(R.id.adminUserEmail);
            deleteButton = itemView.findViewById(R.id.adminUserDeleteButton);
        }

        /**
         * helper for binding the data to the views
         * @param user UserItem data to display
         * @param listener the listener to attach to the delete button
         */
        public void bind(final UserItem user, final OnDeleteClickListener listener) {
            nameText.setText(user.name);
            emailText.setText(user.email);
            //set the click listener for the delete button, calls the interface method
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(user));
        }
    }

    /**
     * Updates list of users displayed/refreshes UI, used for filtering
     * cased on search queries
     * @param filteredList new list of users to display
     */
    public void filterList(List<UserItem> filteredList) {
        //replaces the list directly. Would be better implementations for sure
        //but this is good enough
        this.userList = filteredList;
        notifyDataSetChanged();
    }
}
