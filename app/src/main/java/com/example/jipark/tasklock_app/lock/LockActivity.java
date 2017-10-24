package com.example.jipark.tasklock_app.lock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.example.jipark.tasklock_app.iris.ClosingService;
import com.example.jipark.tasklock_app.iris.IrisActivity;
import com.example.jipark.tasklock_app.task.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LockActivity extends AppCompatActivity implements LockAdapter.LockAdapterCallback {
    private Utils SINGLETON;
    private EditText mEditText;
    private LockAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().startService(new Intent(this, ClosingService.class));

        setContentView(R.layout.activity_lock);
        SINGLETON = Utils.getInstance();
        initRecyclerView();
        initQuickAddEditView();
        initFloatingActionButton();
        initDateTime();

        if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
            //status: active
            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(true);

            SINGLETON.initParentSentTaskListener = true;
            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("assigned_task").addValueEventListener(SINGLETON.parentSentTaskListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.getValue().equals("none")) {
                        SINGLETON.getTaskList().add(new Task((String)dataSnapshot.getValue(), false));
                        mAdapter.notifyItemInserted(SINGLETON.getTaskList().size() - 1);
                        SINGLETON.saveTasks(LockActivity.this);
                        SINGLETON.sendTasksToDatabase();

                        Intent intent = new Intent(getApplicationContext(), LockActivity.class);
                        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

                        b.setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setTicker("TL Ticker")
                                .setContentTitle("Notice")
                                .setContentText("The Monitor has assigned a new task.")
                                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                                .setContentIntent(contentIntent)
                                .setContentInfo("Info");

                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(1, b.build());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onMethodCallback(Task lastTaskCompleted) {
        SINGLETON.saveTasks(this);

        if (SINGLETON.checkTasksAllTrue()) {

            if (SINGLETON.isPaired() && SINGLETON.isJoiner()) {
                Map<String, Object> lastCompletedMap = new HashMap<>();
                lastCompletedMap.put("last_completed", "all_done");
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).updateChildren(lastCompletedMap);
            }

            int completedTaskCount = SINGLETON.getTaskCount();
            SINGLETON.getTaskList().clear();
            SINGLETON.saveTasks(LockActivity.this);

            if (SINGLETON.isPaired() && SINGLETON.isJoiner()) {
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("owner").removeEventListener(SINGLETON.checkOwnerDisconnectedListener);
                if (SINGLETON.initParentSentTaskListener) {
                    SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("assigned_task").removeEventListener(SINGLETON.parentSentTaskListener);
                }
                SINGLETON.getRoomsReference().removeEventListener(SINGLETON.checkRoomExistsBeforeJoinListener);
//                SINGLETON.disconnectJoinerFromRoom();
                SINGLETON.resetLocalJoinerValues();
            }

            AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);

            if(completedTaskCount == 1) {
                alertDialog.setTitle("You completed " + completedTaskCount + " task.");
            }
            else {
                alertDialog.setTitle("You completed " + completedTaskCount + " tasks.");
            }

            alertDialog.setMessage("Congrats! You finished!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();
        }
        else {
            if (lastTaskCompleted.isComplete()) {
                Map<String, Object> lastCompletedMap = new HashMap<>();
                lastCompletedMap.put("last_completed", lastTaskCompleted.getTask());
                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).updateChildren(lastCompletedMap);
            }
        }
    }

    private boolean initDateTime() {
        TextView dateTextView = (TextView)findViewById(R.id.lock_date);
        DateFormat df = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        String date = df.format(Calendar.getInstance().getTime());
        dateTextView.setText(date);
        return true;
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.lock_task_list);
        mAdapter = new LockAdapter(SINGLETON.getTaskList(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }

    private boolean initQuickAddEditView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        mEditText = new EditText(this);
        mEditText.setLayoutParams(params);
        return true;
    }

    private boolean quickAddTask(EditText inputView) {
        String taskText = inputView.getText().toString();
        if (!taskText.isEmpty()) {
            inputView.setText("");
            Task task = new Task(taskText, false);
            SINGLETON.getTaskList().add(task);
            mAdapter.notifyItemInserted(SINGLETON.getTaskList().size() - 1);
            SINGLETON.saveTasks(this);

            SINGLETON.sendTasksToDatabase();

            return true;
        }
        else {
            Toast.makeText(this, "Cannot create empty task!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    @Override
    protected void onPause() {
        // WHEN THE SCREEN IS ABOUT TO TURN OFF
//        if (ScreenReceiver.wasScreenOn) {
//            // THIS IS THE CASE WHEN ONPAUSE() IS CALLED BY THE SYSTEM DUE TO A SCREEN STATE CHANGE
//            System.out.println("SCREEN TURNED OFF");
//        } else {
//            // THIS IS WHEN ONPAUSE() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
//            System.out.println("ON PAUSE NOT BY SCREEN BUTTON");
//            System.out.println("ON PAUSE NOT BY SCREEN BUTTON");
//            System.out.println("ON PAUSE NOT BY SCREEN BUTTON");
//            System.out.println("ON PAUSE NOT BY SCREEN BUTTON");
//            System.out.println("ON PAUSE NOT BY SCREEN BUTTON");
//
//            if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
//                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(false);
//            }
//        }

        if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(false);
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        // ONLY WHEN SCREEN TURNS ON
//        if (!ScreenReceiver.wasScreenOn) {
//            // THIS IS WHEN ONRESUME() IS CALLED DUE TO A SCREEN STATE CHANGE
//            System.out.println("SCREEN TURNED ON");
//        } else {
//            System.out.println("ON RESUME NOT BY SCREEN BUTTON");
//            System.out.println("ON RESUME NOT BY SCREEN BUTTON");
//            System.out.println("ON RESUME NOT BY SCREEN BUTTON");
//            System.out.println("ON RESUME NOT BY SCREEN BUTTON");
//            System.out.println("ON RESUME NOT BY SCREEN BUTTON");
//
//            if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
//                SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(true);
//            }
//            // THIS IS WHEN ONRESUME() IS CALLED WHEN THE SCREEN STATE HAS NOT CHANGED
//        }

        if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(true);
        }

        super.onResume();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        int remainingCount = SINGLETON.getTasksRemaining();

        AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        if(remainingCount == 1) {
            alertDialog.setTitle("You have " + remainingCount + " task remaining.");
        }
        else {
            alertDialog.setTitle("You have " + remainingCount + " tasks remaining.");
        }

        if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
            alertDialog.setMessage("Your Monitor will be notified if you quit.\nAre you sure you want to stop?");
        }
        else {
            alertDialog.setMessage("Are you sure you want to stop?");
        }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (SINGLETON.isJoiner() && SINGLETON.isPaired()) {
                            //status: active
                            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").setValue(false);
                        }
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertDialog.show();
    }

    public boolean initFloatingActionButton() {
        FloatingActionButton mFAB = (FloatingActionButton)findViewById(R.id.lock_float_action);
        final AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
        alertDialog.setTitle("Quick Add");
        alertDialog.setView(mEditText, 50, 0, 50, 0);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(quickAddTask(mEditText)) {
                    dialogInterface.dismiss();
                }
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });
        return true;
    }

    @Override
    protected void onDestroy() {
//        if (SINGLETON.isOwner() && SINGLETON.isPaired()) {
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeEventListener(SINGLETON.waitForTasksListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("last_completed").removeEventListener(SINGLETON.lastTaskCompletedListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").removeEventListener(SINGLETON.waitForJoinerActiveListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner").removeEventListener(SINGLETON.waitForJoinerListener);
//            SINGLETON.disconnectOwnerFromRoom();
//            SINGLETON.resetLocalOwnerValues();
//        }
//        else if (SINGLETON.isOwner()) {
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("last_completed").removeEventListener(SINGLETON.lastTaskCompletedListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("active").removeEventListener(SINGLETON.waitForJoinerActiveListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("joiner").removeEventListener(SINGLETON.waitForJoinerListener);
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).removeValue(); //need to remove listener when removing values...
//            SINGLETON.resetLocalOwnerValues();
//        }
//        else if (SINGLETON.isJoiner()) {
//            SINGLETON.getRoomsReference().child(SINGLETON.getMasterRoomKey()).child("owner").removeEventListener(SINGLETON.checkOwnerDisconnectedListener);
//            SINGLETON.getRoomsReference().removeEventListener(SINGLETON.checkRoomExistsBeforeJoinListener);
//            SINGLETON.disconnectJoinerFromRoom();
//            SINGLETON.resetLocalJoinerValues();
//        }
        super.onDestroy();
    }
}
