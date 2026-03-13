package com.example.junimoapp.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
<<<<<<< Updated upstream
=======
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
>>>>>>> Stashed changes

public class FirebaseManager {
    private FirebaseManager() {}

    //get firestore instance
    public static FirebaseFirestore getDB() {
        return FirebaseFirestore.getInstance();
    }
<<<<<<< Updated upstream
=======

    /**
     * Adds an event to firebase
     * @param event
     * the event to be added
     * @param eventsRef
     * a reference to the events collection
     */
    public void addEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.set(event);
    }

    /**
     * Deletes an event from firebase
     * @param event
     * event to be deleted
     * @param eventsRef
     * collection reference to events collection
     */
    public void deleteEvent(Event event, CollectionReference eventsRef) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.delete();
    }

    /**
     * Adds a user to firebase
     * @param user
     * the user to be added
     * @param usersRef
     * a reference to the users collection
     */
    public void addUser(User user, CollectionReference usersRef) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.set(user);
    }

    //updates to various different event fields (newValue type changes)

    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, String newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, Long newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, GeoPoint newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, ArrayList<String> newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }
    /**
     * Updates specified event field with specified value
     * @param eventsRef
     * a reference to the events collection
     * @param event
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateEvent(CollectionReference eventsRef, Event event, String field, double newValue) {
        DocumentReference docRef = eventsRef.document(event.getEventID());
        docRef.update(field, newValue);
    }

    //updates to user
    /**
     * Updates specified user field with specified value
     * @param usersRef
     * a reference to the users collection
     * @param user
     * the event to be updated
     * @param field
     * the field to be updated
     * @param newValue
     * the new value to be added
     */
    public void updateUser(CollectionReference usersRef, User user, String field, String newValue) {
        DocumentReference docRef = usersRef.document(user.getDeviceId());
        docRef.update(field,newValue);
    }

//    public ArrayList<Event> getEvents(CollectionReference eventsRef){
//        ArrayList<Event> eventArrayList = new ArrayList<>();
//        eventsRef.addSnapshotListener((value,error)-> {
//            if(error != null){
//                Log.e("Firestore", error.toString());
//            }
//            if(value != null && !value.isEmpty()){
//                for(QueryDocumentSnapshot snapshot : value){
//                    //Fields in events
//                    String title = snapshot.getString("Title");
//                    String description = snapshot.getString("Description");
//                    String startDate = snapshot.getString("startDate");
//                    String endDate = snapshot.getString("endDate");
//                    String dateEvent = snapshot.getString("dateEvent");
//                    int maxCapacity = (snapshot.getLong("maxCapacity")).intValue();
//                    int waitingListLimit = (snapshot.getLong("waitingListLimit")).intValue();
//                    double price = snapshot.getDouble("price");
//                    GeoPoint geoLocation = snapshot.getGeoPoint("geoLocation"); //geoPoint is a type apparently? seems helpful??
//                    String poster = snapshot.getString("poster");
//                    String eventID = snapshot.getString("eventID");
//                    String eventLocation = snapshot.getString("eventLocation");
//
//                    String organizerID = snapshot.getString("organizerID");
//
//                    eventArrayList.add(new Event(title,description,startDate,endDate,dateEvent,maxCapacity,waitingListLimit,price,geoLocation,poster,eventID,eventLocation,organizerID));
//                }
//            }
//        });
//        return eventArrayList;
//    }

>>>>>>> Stashed changes
}