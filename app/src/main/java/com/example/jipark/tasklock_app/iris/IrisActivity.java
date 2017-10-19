package com.example.jipark.tasklock_app.iris;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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


//TODO: lock feature, add more lock screen things like time, phone, app manager
//TODO: AppManager, https://stackoverflow.com/questions/22500959/detect-when-other-application-opened-or-launched
//TODO: Firebase, ".write": "auth != null", give app the same id, set up rule for firebase, just to give permission to write to the database

public class IrisActivity extends AppCompatActivity {
    private Utils SINGLETON;

    //room_create.xml
    private TextView mRoomCreateKeyDisplay;

    //room_join.xml
    private EditText mRoomJoinEditText;
    private Button mRoomJoinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SINGLETON = Utils.getInstance();

        if (SINGLETON.isJoiner()) {
            setContentView(R.layout.room_join_success);
        }
        else if(SINGLETON.isOwner()) {
            if(SINGLETON.isPaired()) {
                setContentView(R.layout.room_create_success);
            }
            else {
                setContentView(R.layout.room_create);
            }
        }
        else {
            setContentView(R.layout.activity_iris);
        }
    }

    //activity_iris.xml
    public void createRoom(View view) {
        //first check if internet is connected
        if (isInternetConnected()) {
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

                //change layout
                slideContentIn(R.layout.room_create);
                mRoomCreateKeyDisplay = (TextView)findViewById(R.id.iris_room_key);
                String displayKey = "Room Key: " + roomKey;
                mRoomCreateKeyDisplay.setText(displayKey);

                //listen on change
                final DatabaseReference joinerRoot = SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner");
                joinerRoot.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if((Boolean)dataSnapshot.getValue()) {
                            slideContentIn(R.layout.room_create_success);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//                slideContentIn(R.layout.room_create_success);
                //if joiner value is true, change to connection confirmation page
                //when joiner presses start tasks, display tasks for room owner and send push notifications on each task completion
            }
        }
        else {
            Toast.makeText(this, "Could not establish connection with the server.", Toast.LENGTH_SHORT).show();
        }
    }

    //room_create.xml
    public void cancelRoomCreate(View view) {
        //cancel room creation, reset local room owner values
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue();
        SINGLETON.setMasterRoomKey("");
        SINGLETON.setOwner(false);

        //change layout
        slideContentIn(R.layout.activity_iris);
    }

    //activity_iris.xml
    public void joinRoom(View view) {
        initRoomJoinLayout(); //load room_join.xml
    }

    //room_join.xml
    public void cancelRoomJoin(View view) {
        slideContentIn(R.layout.activity_iris);
    }

    //room_join.xml
    public void enterRoomJoin(View view) {
        final String inputRoomKey = mRoomJoinEditText.getText().toString(); //grab the user input key from editText
        final DatabaseReference rooms = SINGLETON.getRoomsReference();
        rooms.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(inputRoomKey)) {
                    if(!((Boolean)dataSnapshot.child(inputRoomKey).child("joiner").getValue())) {
                        SINGLETON.setJoiner(true);
                        SINGLETON.setMasterRoomKey(inputRoomKey);
                        rooms.child(inputRoomKey).child("joiner").setValue(true);

                        //change layout to success
                        slideContentIn(R.layout.room_join_success);
                        //TODO: begin heartbeat poll
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "This room is full!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Invalid room key!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //room_create_success.xml
    public void closeRoom(View view) { //for room owners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Confirm");
        alertDialog.setMessage("All connected devices will lose their connection to this room.\nAre you sure you want to close the room?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: notify database that we're deleting the room, notify room joiner
                SINGLETON.setMasterRoomKey("");
                SINGLETON.setOwner(false);
                slideContentIn(R.layout.activity_iris);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    public void closeConnection(View view) { //for room joiners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Confirm");
        alertDialog.setMessage("Disconnecting will notify the room owner.\nAre you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: notify database that joiner left the room, and notify room owner
                SINGLETON.setMasterRoomKey("");
                SINGLETON.setJoiner(false);
                slideContentIn(R.layout.activity_iris);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    //room_join_success.xml
    public void returnToMain(View view) {
        //go back to main activity with info saved
    }

    private void slideContentIn(int layout) {
        LayoutInflater inflater = getLayoutInflater();
        View view2 = inflater.inflate(layout, null, false);
        view2.startAnimation(AnimationUtils.loadAnimation(IrisActivity.this, android.R.anim.slide_in_left));
        setContentView(view2);
    }

    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;
    }

    private boolean initRoomJoinLayout() {
        slideContentIn(R.layout.room_join);
        TextInputLayout inputLayout = (TextInputLayout)findViewById(R.id.input_key_layout);
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(6);

        mRoomJoinButton = (Button)findViewById(R.id.join_room_enter_button);
        mRoomJoinEditText = (EditText)findViewById(R.id.join_room_key_input);
        mRoomJoinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mRoomJoinEditText.clearFocus();
                }
                return false;
            }
        });

        mRoomJoinEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length() == 0) {
                    mRoomJoinButton.setVisibility(View.INVISIBLE);
                }
                else {
                    mRoomJoinButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        return true;
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
