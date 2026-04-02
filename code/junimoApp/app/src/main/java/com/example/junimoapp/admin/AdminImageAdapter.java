package com.example.junimoapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.junimoapp.R;

import java.util.List;

/**
 * RecyclerView adapter for image thumbnail, image type label (poster or Profile Photo)
 * label (event title or user name), remove button
 *
 * US 03.03.01 and US 03.06.01
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageViewHolder> {
    /**
     * Data container for one image row
     */
    public static class ImageItem {
        public final String documentId;
        public final String collection;
        public final String fieldName;
        public final String imageUrl;
        public final String type;
        public final String label;

        /**
         * Constructs an ImageItem
         * @param documentId Firestore doc ID
         * @param collection Firestore collection (events or users)
         * @param fieldName Firestore field to clear on removal
         * @param imageUrl URL of image to display
         * @param type short type label
         * @param label descriptive label (event title or user name)
         */
        public ImageItem(String documentId, String collection, String fieldName,
                         String imageUrl, String type, String label) {
            this.documentId = documentId;
            this.collection = collection;
            this.fieldName = fieldName;
            this.imageUrl = imageUrl;
            this.type = type;
            this.label = label;
        }
    }

    /**
     * Callback interface for Remove button
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(ImageItem image);
    }

    private List<ImageItem> imageList;

    private final OnDeleteClickListener deleteListener;

    /**
     * Constructs adapter
     * @param imageList list of ImageItem objects to display
     * @param deleteListener callback for delete button clicks
     */
    public AdminImageAdapter(List<ImageItem> imageList, OnDeleteClickListener deleteListener) {
        this.imageList = imageList;
        this.deleteListener = deleteListener;
    }

    /**
     * Called by RecyclerView to get row view
     * Inflates item_admin_image.xml, wraps in ImageViewHolder
     * @param parent the parent ViewGroup
     * @param viewType the view type (unused)
     * @return a new ImageViewHolder
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Called by RecyclerView to display data
     * @param holder the ViewHolder to bind data to
     * @param position the position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem image = imageList.get(position);
        holder.bind(image, deleteListener);
    }

    /**
     * Returns total number of items in the displayed list
     * @return item count
     */
    @Override
    public int getItemCount() {
        return imageList.size();
    }

    /**
     * Replaces the displayed list with a new (filtered) list, refreshes
     * @param filteredList new list to display
     */
    public void filterList(List<ImageItem> filteredList) {
        this.imageList = filteredList;
        notifyDataSetChanged();
    }

    /**
     * ImageViewHolder holds reference to views in one image row
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView typeText;
        TextView labelText;
        Button deleteButton;

        /**
         * Constructs ViewHolder and finds views in item_admin_image.xml
         * @param itemView the inflated view row
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.adminImageThumbnail);
            typeText = itemView.findViewById(R.id.adminImageType);
            labelText = itemView.findViewById(R.id.adminImageLabel);
            deleteButton = itemView.findViewById(R.id.adminImageDeleteButton);
        }

        /**
         * Binds one ImageItem's data to the row's views
         * @param image the ImageItem to display
         * @param deleteListener callback for the Remove button
         */
        public void bind(final ImageItem image, final OnDeleteClickListener deleteListener) {
            typeText.setText(image.type);
            labelText.setText(image.label);

            //using Glide to handle loading image
            com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(image.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(thumbnail);

            deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(image));
        }
    }
}














