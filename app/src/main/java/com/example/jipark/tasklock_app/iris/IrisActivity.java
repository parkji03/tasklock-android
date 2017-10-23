package com.example.jipark.tasklock_app.iris;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.jipark.tasklock_app.task.Task;
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

    //room_create_task_received
    private RoomOwnerAdapter mAdapter;
    private RecyclerView mRecyclerView;


    private int currentLayout;

    /**
     * Display specific layout depending on the SINGLETON role (owner vs joiner).
     * <p>
     * Status: done
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SINGLETON = Utils.getInstance();

        if (SINGLETON.isJoiner()) {
            currentLayout = R.layout.room_join_success;
            setContentView(R.layout.room_join_success);
        } else if (SINGLETON.isOwner()) {
            if (SINGLETON.hasReceivedTasks() && SINGLETON.isPaired()) {
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeEventListener(SINGLETON.waitForTasksListener);

                currentLayout = R.layout.room_create_task_received;
                setContentView(R.layout.room_create_task_received);
                initRecyclerView();

                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).addValueEventListener(SINGLETON.waitForTasksListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("tasks")) {
                            SINGLETON.setReceivedTasks(true);

                            currentLayout = R.layout.room_create_task_received;
                            setContentView(R.layout.room_create_task_received);
                            initRecyclerView();

                            SINGLETON.getReceivedTaskList().clear();
                            for (DataSnapshot tasksIterator: dataSnapshot.child("tasks").getChildren()) {
                                String taskText = (String)tasksIterator.child("task").getValue();
                                boolean taskCompleted = false;
                                if(tasksIterator.hasChild("complete")) {
                                    taskCompleted = (Boolean)tasksIterator.child("complete").getValue();
                                }
                                Task task = new Task(taskText, taskCompleted);
                                SINGLETON.getReceivedTaskList().add(task);
                            }
                            mAdapter.notifyItemInserted(SINGLETON.getReceivedTaskList().size() - 1);
                            mRecyclerView.invalidate();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else if (!SINGLETON.hasReceivedTasks() && SINGLETON.isPaired()) {
                currentLayout = R.layout.room_create_success;
                setContentView(R.layout.room_create_success);
            } else {
                currentLayout = R.layout.room_create;
                setContentView(R.layout.room_create);
                mRoomCreateKeyDisplay = (TextView) findViewById(R.id.iris_room_key);
                String displayKey = "Room Key: " + SINGLETON.getMasterRoomKey();
                mRoomCreateKeyDisplay.setText(displayKey);
            }
        } else {
            currentLayout = R.layout.activity_iris;
            setContentView(R.layout.activity_iris);
        }
    }

    /**
     * activity_iris.xml ----> room_create.xml
     * Button Listener in activity_iris.xml
     * <p>
     * First check if internet is connected, then create new room in database.
     * Create a listener for a person to join the room, change layout if they join.
     * If the joiner leaves after joining, send a push notification and wait for a person to join again.
     * <p>
     * Status: done
     */
    public void createRoom(View view) {
        //first check if internet is connected
        if (isInternetConnected()) {
            Map<String, Object> newRoom = new HashMap<>();
            Map<String, Object> newRoomVar = new HashMap<>();

            String roomKey = SINGLETON.generateRoomKey();

            newRoom.put(roomKey, "");
            newRoomVar.put("owner", "connected");
            newRoomVar.put("joiner", "none");
            newRoomVar.put("active", false);
            newRoomVar.put("last_completed", "none");

            SINGLETON.getRoomsReference().updateChildren(newRoom);
            DatabaseReference roomRoot = SINGLETON.getRoomsReference().child(roomKey);
            roomRoot.updateChildren(newRoomVar);

            //set client ownership
            SINGLETON.setLocalOwnerValues(roomKey, false, false);

            //change layout
            slideContentIn(R.layout.room_create);
            mRoomCreateKeyDisplay = (TextView) findViewById(R.id.iris_room_key);
            String displayKey = "Room Key: " + roomKey;
            mRoomCreateKeyDisplay.setText(displayKey);

            //listen on change
            final DatabaseReference joinerRoot = SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner");

            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("last_completed").addValueEventListener(SINGLETON.lastTaskCompletedListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!SINGLETON.first) {
                        Intent intent = new Intent(getApplicationContext(), IrisActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                        if (dataSnapshot.getValue().equals("done")) {
                            b.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_stat_name)
                                    .setTicker("TL Ticker")
                                    .setContentTitle("Task complete!")
                                    .setContentText("The joiner has completed all of their tasks.")
                                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                    .setContentIntent(contentIntent)
                                    .setContentInfo("Info");
                        }
                        else {
                            String completedTask = "Joiner last completed: " + dataSnapshot.getValue();

                            b.setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setWhen(System.currentTimeMillis())
                                    .setSmallIcon(R.drawable.ic_stat_name)
                                    .setTicker("TL Ticker")
                                    .setContentTitle("Task complete!")
                                    .setContentText(completedTask)
                                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                    .setContentIntent(contentIntent)
                                    .setContentInfo("Info");
                        }

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").addValueEventListener(SINGLETON.waitForJoinerActiveListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((Boolean)dataSnapshot.getValue() && !SINGLETON.first) {
                        Intent intent = new Intent(getApplicationContext(), IrisActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                        b.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setTicker("TL Ticker")
                                .setContentTitle("Notice")
                                .setContentText("The joiner has started their tasks.")
                                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                .setContentIntent(contentIntent)
                                .setContentInfo("Info");

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());
                    }
                    else if (!(Boolean)dataSnapshot.getValue() && !SINGLETON.first){
                        Intent intent = new Intent(getApplicationContext(), IrisActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                        b.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setTicker("TL Ticker")
                                .setContentTitle("Notice")
                                .setContentText("The joiner has stopped doing their tasks.")
                                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                .setContentIntent(contentIntent)
                                .setContentInfo("Info");

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());
                    }
                    SINGLETON.first = false;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            joinerRoot.addValueEventListener(SINGLETON.waitForJoinerListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ((dataSnapshot.getValue()).equals("connected")) {
                        SINGLETON.setPaired(true);
                        slideContentIn(R.layout.room_create_success);

                        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).addValueEventListener(SINGLETON.waitForTasksListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {


                                if (dataSnapshot.hasChild("tasks")) {
                                    SINGLETON.setReceivedTasks(true);

                                    currentLayout = R.layout.room_create_task_received;
                                    setContentView(R.layout.room_create_task_received);
                                    initRecyclerView();

                                    SINGLETON.getReceivedTaskList().clear();
                                    for (DataSnapshot tasksIterator: dataSnapshot.child("tasks").getChildren()) {
                                        String taskText = (String)tasksIterator.child("task").getValue();
                                        boolean taskCompleted = false;
                                        if(tasksIterator.hasChild("complete")) {
                                            taskCompleted = (Boolean)tasksIterator.child("complete").getValue();
                                        }
                                        Task task = new Task(taskText, taskCompleted);
                                        SINGLETON.getReceivedTaskList().add(task);
                                    }
                                    mAdapter.notifyItemInserted(SINGLETON.getReceivedTaskList().size() - 1);
                                    mRecyclerView.invalidate();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    } else if ((dataSnapshot.getValue()).equals("disconnected")) {
                        SINGLETON.setPaired(false);

                        Intent intent = new Intent(getApplicationContext(), IrisActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                        b.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setTicker("TL Ticker")
                                .setContentTitle("Notice")
                                .setContentText("The joiner has disconnected.")
                                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                .setContentIntent(contentIntent)
                                .setContentInfo("Info");

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());

                        if (active) {
                            slideContentIn(R.layout.room_create);
                            mRoomCreateKeyDisplay = (TextView) findViewById(R.id.iris_room_key);
                            String displayKey = "Room Key: " + SINGLETON.getMasterRoomKey();
                            mRoomCreateKeyDisplay.setText(displayKey);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            Toast.makeText(this, "Could not establish connection with the server.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * room_create.xml ----> activity_iris.xml
     * Button Listener in room_create.xml
     * <p>
     * Remove the listener, then remove the room created in the database.
     * Reset local values in SINGLETON.
     * <p>
     * Status: done
     */
    public void cancelRoomCreate(View view) {
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("last_completed").removeEventListener(SINGLETON.lastTaskCompletedListener);
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").removeEventListener(SINGLETON.waitForJoinerActiveListener);
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner").removeEventListener(SINGLETON.waitForJoinerListener);
        SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue(); //need to remove listener when removing values...
        SINGLETON.resetLocalOwnerValues();
        slideContentIn(R.layout.activity_iris);
    }

    /**
     * activity_iris.xml ----> room_join.xml
     * Button Listener in activity_iris.xml
     * <p>
     * EditText counter enabled, and soft keyboard closes on clicking IME_ACTION_DONE.
     * TextChangedListener for EditText to make RoomJoin Button visible/invisible.
     * <p>
     * Status: done
     */
    public void joinRoom(View view) {
        slideContentIn(R.layout.room_join);
        TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.input_key_layout);
        inputLayout.setCounterEnabled(true);
        inputLayout.setCounterMaxLength(6);

        mRoomJoinButton = (Button) findViewById(R.id.room_join_link);
        mRoomJoinEditText = (EditText) findViewById(R.id.join_room_key_input);
        mRoomJoinEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
                if (charSequence.toString().trim().length() == 0) {
                    mRoomJoinButton.setVisibility(View.INVISIBLE);
                } else {
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
     * <p>
     * Simple cancel button to return to previous menu.
     * <p>
     * Status: done
     */
    public void cancelRoomJoin(View view) {
        slideContentIn(R.layout.activity_iris);
    }

    /**
     * room_join.xml ----> room_join_success.xml
     * Button Listener in room_join.xml
     * <p>
     * Grab joiner's room key and check if room exists in the database.
     * If room exists, check if it's full.
     * If it's not full, set local joiner values and change layout, and update database node "joiner" to true.
     * <p>
     * Status: done
     */
    public void enterRoomJoin(View view) {
        final String inputRoomKey = mRoomJoinEditText.getText().toString(); //grab the user input key from editText
        SINGLETON.getRoomsReference().addListenerForSingleValueEvent(SINGLETON.checkRoomExistsBeforeJoinListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(inputRoomKey)) {
                    if ((dataSnapshot.child(inputRoomKey).child("joiner").getValue()).equals("none") || (dataSnapshot.child(inputRoomKey).child("joiner").getValue()).equals("disconnected")) {
                        SINGLETON.setLocalJoinerValues(inputRoomKey, true, false);
                        SINGLETON.getRoomsReference().child(inputRoomKey).child("joiner").setValue("connected"); //change database
                        slideContentIn(R.layout.room_join_success);

                        //we're the joiner so listen for owner boolean value change...
                        SINGLETON.getRoomsReference().child(inputRoomKey).child("owner").addValueEventListener(SINGLETON.checkOwnerDisconnectedListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((dataSnapshot.getValue()).equals("disconnected")) {
                                    SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("owner").removeEventListener(SINGLETON.checkOwnerDisconnectedListener);
                                    SINGLETON.getRoomsReference().removeEventListener(SINGLETON.checkRoomExistsBeforeJoinListener);
                                    SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue();
                                    SINGLETON.resetLocalJoinerValues();

                                    Intent intent = new Intent(getApplicationContext(), IrisActivity.class);
                                    PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                                    b.setAutoCancel(true)
                                            .setDefaults(Notification.DEFAULT_ALL)
                                            .setWhen(System.currentTimeMillis())
                                            .setSmallIcon(R.drawable.ic_stat_name)
                                            .setTicker("TL Ticker")
                                            .setContentTitle("Notice")
                                            .setContentText("The Monitor has disconnected.")
                                            .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                            .setContentIntent(contentIntent)
                                            .setContentInfo("Info");

                                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(1, b.build());

                                    if (active) {
                                        slideContentIn(R.layout.activity_iris);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "This room is full!", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
     * <p>
     * Show warning message.  On confirm, reset local owner values and notify database that owner disconnected.
     * <p>
     * Status: done
     */
    public void closeRoom(View view) { //for room owners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("The joiner will lose their connection.\nAre you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeEventListener(SINGLETON.waitForTasksListener);
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("last_completed").removeEventListener(SINGLETON.lastTaskCompletedListener);
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").removeEventListener(SINGLETON.waitForJoinerActiveListener);
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner").removeEventListener(SINGLETON.waitForJoinerListener);
                SINGLETON.disconnectOwnerFromRoom();
                SINGLETON.resetLocalOwnerValues();
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
     * <p>
     * Show warning message.  On confirm, reset local joiner values and notify database that joiner disconnected.
     * <p>
     * Status: done
     */
    public void closeConnection(View view) { //for room joiners only
        AlertDialog alertDialog = new AlertDialog.Builder(IrisActivity.this).create();
        alertDialog.setTitle("Warning!");
        alertDialog.setMessage("Disconnecting will notify the room owner.\nAre you sure?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("owner").removeEventListener(SINGLETON.checkOwnerDisconnectedListener);
                SINGLETON.getRoomsReference().removeEventListener(SINGLETON.checkRoomExistsBeforeJoinListener);
                SINGLETON.disconnectJoinerFromRoom();
                SINGLETON.resetLocalJoinerValues();
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
     * <p>
     * Return to main activity.
     * <p>
     * Status: done
     */
    public void returnToMain(View view) {
        finish();
    }

    /**
     * room_create_task_received.xml ----> activity_main.xml
     * Button Listener in room_create_task_received.xml
     * <p>
     * Return to main activity.
     * <p>
     * Status: done
     */
    public void returnToHome(View view) {
        finish();
    }

    /**
     * Inflates layout given in @param with an animation.
     *
     * @param layout id of layout.xml to switch view to.
     *               <p>
     *               Status: done
     */
    private void slideContentIn(int layout) {
        currentLayout = layout;
        SINGLETON.currentLayoutNum = layout;
        LayoutInflater inflater = getLayoutInflater();
        View view2 = inflater.inflate(layout, null, false);
        view2.startAnimation(AnimationUtils.loadAnimation(IrisActivity.this, android.R.anim.slide_in_left));
        setContentView(view2);
    }

    /**
     * Checks if internet is connected.
     *
     * @return boolean value of internet connection status.
     * <p>
     * Status: done
     */
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        } else
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
        } else if (currentLayout == R.layout.room_create_success) {
            closeRoom(null);
        } else {
            super.onBackPressed();
        }
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.received_task_list);
        mAdapter = new RoomOwnerAdapter(SINGLETON.getReceivedTaskList());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }
}