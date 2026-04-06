package com.example.junimoapp.Organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.junimoapp.models.Event;

import com.example.junimoapp.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.List;

/**
 * Views list of the organizers events
 * Shows:
 *  - event title
 *  - description,
 *  - an edit button to edit the event,
 *  - and a QR code button to view the qrcode associated with the event
 *  Buttons functions:
 *  - edit buttons opens CreateEvent screen
 *  - QR code button opens a qrcode pop up screen
 * */
public class ListOfMyEvents extends RecyclerView.Adapter<ListOfMyEvents.EventViewHolder> {

    /** events to display */
    private List<Event> eventList;

    /** constructor
     * @param eventList event list to display
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
        Button editEventButton, viewQRCodeButton;
        ImageView backgroundPoster;

        /**
         * initializes the view for each event
         * @param itemView view for each event
         * */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            description = itemView.findViewById(R.id.event_description);
            editEventButton = itemView.findViewById(R.id.edit_event_button);
            viewQRCodeButton = itemView.findViewById(R.id.view_QR_code_button);
            backgroundPoster = itemView.findViewById(R.id.background_poster);
        }
    }

    /**
     * creates a new view holder when needed
     * @param parent view group
     * @param viewType view type
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
     * @param holder view holder
     * @param position position of event in list
     * */
    @Override
    public void onBindViewHolder(@NonNull ListOfMyEvents.EventViewHolder holder, int position) {
        Event event =eventList.get(position);
        holder.title.setText(event.getTitle());
        holder.description.setText(event.getDescription());
        holder.editEventButton.setOnClickListener(view -> {
            Intent editEvent = new Intent(view.getContext(), CreateEvent.class);
            editEvent.putExtra("eventID", event.getEventID());
            view.getContext().startActivity(editEvent);
        });
        if (event.getPoster() != null && !event.getPoster().isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(event.getPoster())
                    .centerCrop()
                    .into(holder.backgroundPoster);
        } else {
            holder.backgroundPoster.setImageResource(R.drawable.bg_event_tile);
        }

        String QRCodeString = event.getQRCode();
        Log.d("QR DEBUG", "QR code: " + QRCodeString);
        holder.viewQRCodeButton.setOnClickListener(view -> {
            if (QRCodeString == null || QRCodeString.equals("")) {
                Toast.makeText(view.getContext(), "No QR code available", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                BitMatrix matrix = new MultiFormatWriter().encode(QRCodeString, BarcodeFormat.QR_CODE, 600, 600);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap QRBitmap = encoder.createBitmap(matrix);

                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.pop_up_qr_code, null);
                dialogView.<ImageView>findViewById(R.id.QR_image).setImageBitmap(QRBitmap);;
                dialogView.<TextView>findViewById(R.id.event_title).setText(event.getTitle());

                AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                        .setView(dialogView)
                        .create();

                dialogView.findViewById(R.id.close_button).setOnClickListener(v -> dialog.dismiss());
                dialog.show();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(android.R.color.transparent);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    window.setDimAmount(0.5f);

                    window.setGravity(Gravity.CENTER);
                    int screenWidth = view.getResources().getDisplayMetrics().widthPixels;
                    int dialogWidth = (int) (screenWidth * 0.75);
                    window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);

                    ImageView QRCodeImageView = dialogView.findViewById(R.id.QR_image);
                    int QRCodeSize = (int) (dialogWidth * 0.90);
                    QRCodeImageView.getLayoutParams().width = QRCodeSize;
                    QRCodeImageView.getLayoutParams().height = QRCodeSize;
                    QRCodeImageView.requestLayout();
                }

            } catch (Exception ignored) {}
        });
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
