package com.example.jipark.tasklock_app.iris;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class IrisActivity extends AppCompatActivity {
    private Utils SINGLETON;
    private ProgressBar mProgress;
    private TextView mIrisTitle;
    private ImageView mIrisLogo;
    private TextView mIrisFlavor;
    private TextView mIrisGuide;
    private TextView mWaiting;
    private Button mCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris);
        SINGLETON = Utils.getInstance();

        mProgress = (ProgressBar)findViewById(R.id.waiting_progress);
        mIrisTitle = (TextView)findViewById(R.id.iris_title);
        mIrisLogo = (ImageView)findViewById(R.id.iris_logo);
        mIrisFlavor = (TextView)findViewById(R.id.iris_flavor);
        mIrisGuide = (TextView)findViewById(R.id.iris_guide);
        mWaiting = (TextView)findViewById(R.id.waiting_text);
        mCancelButton = (Button)findViewById(R.id.waiting_cancel);
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

            //give this client the owner rights
            SINGLETON.setOwner(true);
            SINGLETON.setMasterRoomKey(roomKey);

            //hide views

            //show loading

            //listen on change if value is true

            //change
        }
    }

    public void cancelRoom(View view) {
        //SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue();

    }

    public void joinRoom(View view) {
        final DatabaseReference rooms = SINGLETON.getRoomsReference();
        rooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("rK2HJv")) {
                    Toast.makeText(getApplicationContext(), "room exists", Toast.LENGTH_SHORT).show();
                    rooms.child("rK2HJv").child("joiner").setValue(true);
                    //TODO: begin heartbeat poll
                }
                else {
                    Toast.makeText(getApplicationContext(), "room doesn't exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void hideMainIris() {
        mIrisTitle.setVisibility(View.GONE);
        mIrisLogo.setVisibility(View.GONE);
        mIrisFlavor.setVisibility(View.GONE);
        mIrisGuide.setVisibility(View.GONE);
    }

    private void showCreateRoomLoading() {
        mProgress.setVisibility(View.VISIBLE);
        mWaiting.setVisibility(View.VISIBLE);
        mCancelButton.setVisibility(View.VISIBLE);
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
