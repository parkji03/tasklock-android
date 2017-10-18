package com.example.jipark.tasklock_app.iris;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


//TODO: lock feature, add more lock screen things like time, phone, app manager
//TODO: AppManager, https://stackoverflow.com/questions/22500959/detect-when-other-application-opened-or-launched
//TODO: Firebase, ".write": "auth != null", give app the same id, set up rule for firebase, just to give permission to write to the database

public class IrisActivity extends AppCompatActivity {
    private Utils SINGLETON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris);
        SINGLETON = Utils.getInstance();
    }

    public void createRoom(View view) {
        if (SINGLETON.isJoiner()) { //only let non-joiners
            //client is a joiner, don't let him make without confirmation
        }
        else if(SINGLETON.isOwner()) {
            //client is an owner, don't let him make without confirmation
        }
        else {
            Map<String, Object> newRoom = new HashMap<>();
            Map<String, Object> roomOwner = new HashMap<>();
            Map<String, Object> roomJoiner = new HashMap<>();
            String roomKey = SINGLETON.generateRoomKey();
            String ownerID = SINGLETON.generateOwnerID();

            newRoom.put(roomKey, "");
            roomOwner.put("owner", ownerID);
            roomJoiner.put("joiner", false);

            SINGLETON.getRoomsReference().updateChildren(newRoom);
            DatabaseReference roomRoot = SINGLETON.getRoomsReference().child(roomKey);
            roomRoot.updateChildren(roomOwner);
            roomRoot.updateChildren(roomJoiner);

            //set client ownership
            SINGLETON.setOwner(true);
            SINGLETON.setMasterRoomKey(roomKey);

            setContentView(R.layout.room_create);

            //show loading
            //listen on change if value is true
            //change
        }
    }

    public void joinRoom(View view) {
        Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show();

//        final DatabaseReference rooms = SINGLETON.getRoomsReference();
//        rooms.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.hasChild("rK2HJv")) {
//                    Toast.makeText(getApplicationContext(), "room exists", Toast.LENGTH_SHORT).show();
//                    rooms.child("rK2HJv").child("joiner").setValue(true);
//                    //TODO: begin heartbeat poll
//                }
//                else {
//                    Toast.makeText(getApplicationContext(), "room doesn't exist", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });



    }


    //TODO: for parent

    //create modal and say you are waiting for child to connect
    //hitting cancel on modal will delete the room in the database
    //confirm will show if child connects
    //the parent will now receive push notifications when the child finishes a task.

    //TODO: for child


    //if room exists, save it and we're good to go... a green check mark represent it
        //we are now connected, we can't make a room...
    //if not, notify child that room doesn't exist, and double check the pass code

    //if we're connected and we start our tasks
    //send to database the list of all tasks I have


}
