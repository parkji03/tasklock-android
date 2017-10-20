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
import android.view.MenuItem;
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
    static boolean active = false;

    //room_create.xml
    private TextView mRoomCreateKeyDisplay;

    //room_join.xml
    private EditText mRoomJoinEditText;
    private Button mRoomJoinButton;

    private int currentLayout;


    /**
     * Display specific layout depending on the SINGLETON role (owner vs joiner).
     *
     * Status: done
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SINGLETON = Utils.getInstance();

        if (SINGLETON.isJoiner()) {
            currentLayout = R.layout.room_join_success;
            setContentView(R.layout.room_join_success);
        }
        else if (SINGLETON.isOwner()) {
            if (SINGLETON.hasReceivedTasks() && SINGLETON.isPaired()) {
                currentLayout = R.layout.room_create_task_received;
                setContentView(R.layout.room_create_task_received);
            }
            else if (!SINGLETON.hasReceivedTasks() && SINGLETON.isPaired()) {
                currentLayout = R.layout.room_create_success;
                setContentView(R.layout.room_create_success);
            }
            else {
                currentLayout = R.layout.room_create;
                setContentView(R.layout.room_create);
                mRoomCreateKeyDisplay = (TextView)findViewById(R.id.iris_room_key);
                String displayKey = "Room Key: " + SINGLETON.getMasterRoomKey();
                mRoomCreateKeyDisplay.setText(displayKey);
            }
        }
        else {
            currentLayout = R.layout.activity_iris;
            setContentView(R.layout.activity_iris);
        }
    }

    /**
     * activity_iris.xml ----> room_create.xml
     * Button Listener in activity_iris.xml
     *
     * First check if internet is connected, then create new room in database.
     * Create a listener for a person to join the room, change layout if they join.
     * If the joiner leaves after joining, send a push notification and wait for a person to join again.
     *
     * Status: incomplete
     */
    public void createRoom(View view) {
        //first check if internet is connected
        if (isInternetConnected()) {
            Map<String, Object> newRoom = new HashMap<>();
            Map<String, Object> roomOwner = new HashMap<>();
            Map<String, Object> roomJoiner = new HashMap<>();
            String roomKey = SINGLETON.generateRoomKey();

            newRoom.put(roomKey, "");
            roomOwner.put("owner", "connected");
            roomJoiner.put("joiner", "none");

            SINGLETON.getRoomsReference().updateChildren(newRoom);
            DatabaseReference roomRoot = SINGLETON.getRoomsReference().child(roomKey);
            roomRoot.updateChildren(roomOwner);
            roomRoot.updateChildren(roomJoiner);

            //set client ownership
            SINGLETON.setLocalOwnerValues(roomKey, false, false);

            //change layout
            slideContentIn(R.layout.room_create);
            mRoomCreateKeyDisplay = (TextView)findViewById(R.id.iris_room_key);
            String displayKey = "Room Key: " + roomKey;
            mRoomCreateKeyDisplay.setText(displayKey);

            //listen on change
            final DatabaseReference joinerRoot = SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner");
            joinerRoot.addValueEventListener(SINGLETON.waitForJoinerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.getValue()).equals("connected")) {
                        SINGLETON.setPaired(true);
                        slideContentIn(R.layout.room_create_success);
                    }
                    else if ((dataSnapshot.getValue()).equals("disconnected")) {
                        SINGLETON.setPaired(false);
                        Toast.makeText(getApplicationContext(), "Joiner disconnected!", Toast.LENGTH_SHORT).show();
                        if (active) {
                            slideContentIn(R.layout.room_create);
                            mRoomCreateKeyDisplay = (TextView)findViewById(R.id.iris_room_key);
                            String displayKey = "Room Key: " + SINGLETON.getMasterRoomKey();
                            mRoomCreateKeyDisplay.setText(displayKey);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        else {
            Toast.makeText(this, "Could not establish connection with the server.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * room_create.xml ----> activity_iris.xml
     * Button Listener in room_create.xml
     *
     * Remove the listener, then remove the room created in the database.
     * Reset local values in SINGLETON.
     *
     * Status: done
     */
    public void cancelRoomCreate(View view) {
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner").removeEventListener(SINGLETON.waitForJoinerListener);
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue(); //need to remove listener when removing values...
        SINGLETON.resetLocalOwnerValues();
        slideContentIn(R.layout.activity_iris);
    }

    /**
     * activity_iris.xml ----> room_join.xml
     * Button Listener in activity_iris.xml
     *
     * EditText counter enabled, and soft keyboard closes on clicking IME_ACTION_DONE.
     * TextChangedListener for EditText to make RoomJoin Button visible/invisible.
     *
     * Status: done
     */
    public void joinRoom(View view) {
        slideContentIn(R.layout.room_join);
        TextInputLayout inputLayout = (TextInputLayout)findViewById(R.id.input_key_layout);
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(6);

        mRoomJoinButton = (Button)findViewById(R.id.room_join_link);
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
    }

    /**
     * room_join.xml ----> activity_iris.xml
     * Button Listener in room_join.xml
     *
     * Simple cancel button to return to previous menu.
     *
     * Status: done
     */
    public void cancelRoomJoin(View view) {
        slideContentIn(R.layout.activity_iris);
    }

    /**
     * room_join.xml ----> room_join_success.xml
     * Button Listener in room_join.xml
     *
     * Grab joiner's room key and check if room exists in the database.
     * If room exists, check if it's full.
     * If it's not full, set local joiner values and change layout, and update database node "joiner" to true.
     *
     * Status: done
     */
    public void enterRoomJoin(View view) {
        final String inputRoomKey = mRoomJoinEditText.getText().toString(); //grab the user input key from editText
        SINGLETON.getRoomsReference().addListenerForSingleValueEvent(SINGLETON.checkRoomExistsBeforeJoinListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(inputRoomKey)) {
                    if((dataSnapshot.child(inputRoomKey).child("joiner").getValue()).equals("none") || (dataSnapshot.child(inputRoomKey).child("joiner").getValue()).equals("disconnected")) {
                        SINGLETON.setLocalJoinerValues(inputRoomKey, true, false);
                        SINGLETON.getRoomsReference().child(inputRoomKey).child("joiner").setValue("connected"); //change database
                        slideContentIn(R.layout.room_join_success);

                        //we're the joiner so listen for owner boolean value change...
                        SINGLETON.getRoomsReference().child(inputRoomKey).child("owner").addValueEventListener(SINGLETON.checkOwnerDisconnectedListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if((dataSnapshot.getValue()).equals("disconnected")) {
                                    Toast.makeText(getApplicationContext(), "Monitor disconnected!", Toast.LENGTH_SHORT).show();
                                    if (active) {
                                        slideContentIn(R.layout.activity_iris);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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

    /**
     * room_create_success.xml ----> activity_iris.xml
     * Button Listener in room_create_success.xml
     *
     * Show warning message.  On confirm, reset local owner values and notify database that owner disconnected.
     *
     * Status: incomplete... notify database to inform joiner's client
     */
    public void closeRoom(View view) { //for room owners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("The joiner hasn't started their tasks yet.\nAre you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: notify database that we're deleting the room, notify room joiner
                SINGLETON.disconnectOwnerFromRoom();
                //TODO: delete owner listeners... delete room node FROM JOINER CLIENT!!!
//                SINGLETON.resetLocalOwnerValues();

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

    /**
     * room_join_success.xml ----> activity_iris.xml
     * Button Listener in room_join_success.xml
     *
     * Show warning message.  On confirm, reset local joiner values and notify database that joiner disconnected.
     *
     * Status: incomplete... notify database that joiner left...
     */
    public void closeConnection(View view) { //for room joiners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("Disconnecting will notify the room owner.\nAre you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //TODO: notify database that joiner left the room, and notify room owner
                SINGLETON.disconnectJoinerFromRoom();
                //TODO: delete joiner listeners...
//                SINGLETON.resetLocalJoinerValues();
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

    /**
     * room_join_success.xml ----> activity_main.xml
     * Button Listener in room_join_success.xml
     *
     * Return to main activity.
     *
     * Status: done
     */
    public void returnToMain(View view) {
        finish();
    }

    /**
     * room_create_task_received.xml ----> activity_main.xml
     * Button Listener in room_create_task_received.xml
     *
     * Return to main activity.
     *
     * Status: done
     */
    public void returnToHome(View view) {
        finish();
    }

    /**
     * Inflates layout given in @param with an animation.
     * @param layout id of layout.xml to switch view to.
     *
     * Status: done
     */
    private void slideContentIn(int layout) {
        currentLayout = layout;
        LayoutInflater inflater = getLayoutInflater();
        View view2 = inflater.inflate(layout, null, false);
        view2.startAnimation(AnimationUtils.loadAnimation(IrisActivity.this, android.R.anim.slide_in_left));
        setContentView(view2);
    }

    /**
     * Checks if internet is connected.
     * @return boolean value of internet connection status.
     *
     * Status: done
     */
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

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onBackPressed() {
        if (currentLayout == R.layout.room_create) {
            cancelRoomCreate(null);
        }
        else if (currentLayout == R.layout.room_create_success) {
            closeRoom(null);
        }
//        else if (currentLayout == R.layout.room_create_task_received) {
//        }
        else {
            super.onBackPressed();
        }
    }
}