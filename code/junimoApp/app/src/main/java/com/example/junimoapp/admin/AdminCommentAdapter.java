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
 * AdminCommentAdapter is a RecyclerView adapter for AdminEventDetailActivity
 * Each row is a comment's author and the "Remove" button so the admin can
 * delete comments that violate app policy (and note which user to remove if
 * necessary).
 *
 * US 03.10.01: as an admin, I want to remove comments that violate app policy
 */
public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.CommentViewHolder> {
    /**
     * CommentItem contains one comment row
     */
    public static class CommentItem {
        public final String commentId;
        public final String userId;
        public final String text;

        /**
         * CommentItem constructor
         * @param commentId Firestore document ID for the comment
         * @param userId device ID of the comment's author
         * @param text the comment text
         */
        public CommentItem(String commentId, String userId, String text) {
            this.commentId = commentId;
            this.userId = userId;
            this.text = text;
        }
    }

    /**
     * Callback interface for the "Remove" button
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(CommentItem comment);
    }

    private final List<CommentItem> commentList;
    private final OnDeleteClickListener deleteListener;

    /**
     * Constructs the adapter
     * @param commentList list of CommentItem objects to display
     * @param deleteListener callback to handle delete button clicks
     */
    public AdminCommentAdapter(List<CommentItem> commentList, OnDeleteClickListener deleteListener) {
        this.commentList = commentList;
        this.deleteListener = deleteListener;
    }

    /**
     * Called by RecyclerView when it needs a new row view
     * Inflates item_admin_comment.cml, wraps it in CommentViewHolder
     * @param parent the parent ViewGroup
     * @param viewType the view type (unused)
     * @return a new CommentViewHolder
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data
     * @param holder Viewholder to bind data to
     * @param position position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentItem comment = commentList.get(position);
        holder.bind(comment, deleteListener);
    }

    /**
     * Returns total number of comments in list
     * @return item count
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * CommentViewHolder holds reference to views in one comment row
     */
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorText;
        TextView commentText;
        Button deleteButton;

        /**
         * Constructs ViewHolder and finds views in item_admin_comment.xml
         * @param itemView the inflated row view
         */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorText = itemView.findViewById(R.id.adminCommentAuthor);
            commentText = itemView.findViewById(R.id.adminCommentText);
            deleteButton = itemView.findViewById(R.id.adminCommentDeleteButton);
        }

        /**
         * Binds one COmmentItem's data to this row's values
         * @param comment the CommentItem to display
         * @param deleteListener callback for the Remove button
         */
        public void bind(final CommentItem comment, final OnDeleteClickListener deleteListener) {
            authorText.setText(comment.userId);
            commentText.setText(comment.text);
            deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(comment));
        }
    }
}
