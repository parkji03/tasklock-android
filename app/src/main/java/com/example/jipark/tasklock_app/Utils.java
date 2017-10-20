package com.example.jipark.tasklock_app;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import com.example.jipark.tasklock_app.task.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jipark on 10/13/17.
 */

public class Utils {
    private static final Utils ourInstance = new Utils();
    private final String tasksFileName = "tasks.json";
//    private final String AB = "0123456789ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"; //doesn't contain I or l for avoiding ambiguity
    private final String AB = "0123456789abcdefghijklmnopqrstuvwxyz"; //got rid of capital letters for ease of use
    private final int roomKeyLength = 6;
    private final int ownerIDLength = 4;

    private List<Task> taskList;
    private DatabaseReference roomsReference;
    private String masterRoomKey;
    private boolean owner;
    private boolean joiner;
    private boolean paired;
    private boolean receivedTasks;
    private boolean sentTasks;
    private SecureRandom rnd;

    //for owners
    public ValueEventListener waitForJoinerListener;
    public ValueEventListener waitForTasksListener;

    //for joiners
    public ValueEventListener checkRoomExistsBeforeJoinListener;
    public ValueEventListener checkOwnerDisconnectedListener;

    public static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {
        taskList = new ArrayList<>();
        roomsReference = FirebaseDatabase.getInstance().getReference("Rooms");
        rnd = new SecureRandom();
        masterRoomKey = "";
        owner = false;
        joiner = false;
        paired = false;
        receivedTasks = false;
        sentTasks = false;
    }

    private String generateRandomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void loadTasks(Context context) {
        try {
            FileInputStream fis = context.openFileInput(tasksFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonTaskListHolder = new JSONObject(sb.toString());
            JSONArray jsonTaskList = jsonTaskListHolder.getJSONArray("tasks");
            JSONObject jsonTask;
            String taskText;
            boolean isComplete;

            for (int i = 0; i < jsonTaskList.length(); i++) {
                jsonTask = jsonTaskList.getJSONObject(i);
                taskText = jsonTask.getString("task");
                isComplete = jsonTask.getBoolean("complete");

                taskList.add(new Task(taskText, isComplete));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException fileNotFound) {
            fileNotFound.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public boolean saveTasks(Activity activity) {
        JSONObject jsonTaskListHolder = new JSONObject();
        JSONArray jsonTaskList = new JSONArray();
        JSONObject jsonTask;
        String jsonString;

        try {
            for (int i = 0; i < taskList.size(); i++) {
                jsonTask = new JSONObject();
                jsonTask.put("task", taskList.get(i).getTask());
                jsonTask.put("complete", taskList.get(i).isComplete());
                jsonTaskList.put(jsonTask);
            }
            jsonTaskListHolder.put("tasks", jsonTaskList);
            jsonString = jsonTaskListHolder.toString();

            FileOutputStream fos = activity.getApplicationContext().openFileOutput(tasksFileName, Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        catch (FileNotFoundException fileNotFound) {
            return false;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    public void addTaskToHead(Task task) {
        taskList.add(0, task);
    }

    public void resetTaskList() {
        taskList.clear();
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public int getTaskCount() {
        return taskList.size();
    }

    public boolean checkTasksAllTrue() {
        for (int i = 0; i < taskList.size(); i++) {
            if(!taskList.get(i).isComplete()) {
                return false;
            }
        }
        return true;
    }

    public int getTasksRemaining() {
        int remaining = 0;
        for (int i = 0; i < taskList.size(); i++) {
            if(!taskList.get(i).isComplete())
                remaining++;
        }
        return remaining;
    }

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    public DatabaseReference getRoomsReference() {
        return roomsReference;
    }

    public String generateRoomKey() {
        return generateRandomString(roomKeyLength);
    }

    public String generateOwnerID() {
        return "owner-" + generateRandomString(ownerIDLength);
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isJoiner() {
        return joiner;
    }

    public String getMasterRoomKey() {
        return masterRoomKey;
    }

    public boolean hasReceivedTasks() {
        return receivedTasks;
    }

    public boolean hasSentTasks(){
        return sentTasks;
    }

    public void setReceivedTasks(boolean receivedTasks) {
        this.receivedTasks = receivedTasks;
    }

    public void setSentTasks(boolean sentTasks) {
        this.sentTasks = sentTasks;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    public void setLocalJoinerValues(String key, boolean paired, boolean sentTasks) {
        this.paired = paired;
        this.sentTasks = sentTasks;
        this.joiner = true;
        this.masterRoomKey = key;
    }

    public void setLocalOwnerValues(String key, boolean paired, boolean receivedTasks) {
        this.paired = paired;
        this.receivedTasks = receivedTasks;
        this.owner = true;
        this.masterRoomKey = key;
    }

    public void resetLocalJoinerValues() {
        this.paired = false;
        this.joiner = false;
        this.sentTasks = false;
        this.masterRoomKey = "";
        this.receivedTasks = false;
    }

    public void resetLocalOwnerValues() {
        this.paired = false;
        this.owner = false;
        this.receivedTasks = false;
        this.masterRoomKey = "";
        this.receivedTasks = false;
    }

    public void disconnectOwnerFromRoom() {
        roomsReference.child(masterRoomKey).child("owner").setValue("disconnected");
//        roomsReference.child(masterRoomKey).removeEventListener();
//        roomsReference.child(masterRoomKey).removeEventListener();

        resetLocalOwnerValues();
    }

    public void disconnectJoinerFromRoom() {
        roomsReference.child(masterRoomKey).child("joiner").setValue("disconnected");
//        roomsReference.child(masterRoomKey).removeEventListener();
//        roomsReference.child(masterRoomKey).removeEventListener();

        resetLocalJoinerValues();
    }

    public void sendTasksToDatabase() {
        if (isJoiner() && isPaired()) { //make sure we're connected to database...
            setSentTasks(true);

            //add tasks holder to database
            Map<String, Object> tasks = new HashMap<>();
            tasks.put("tasks", "");
            getRoomsReference().child(getMasterRoomKey()).updateChildren(tasks);

            DatabaseReference tasksRoot = getRoomsReference().child(getMasterRoomKey()).child("tasks");

            int iter = 0;
            for (Task taskIter : getTaskList()) {
                String iterString = String.valueOf(iter); //id for tasks

                //create objects to put into database
                Map<String, Object> tasksID = new HashMap<>();
                Map<String, Object> taskString = new HashMap<>();
                Map<String, Object> taskDone = new HashMap<>();

                tasksID.put(iterString, "");
                taskString.put("task", taskIter.getTask());
                taskDone.put("complete", taskIter.isComplete());

                //update database
                tasksRoot.updateChildren(tasksID);
                tasksRoot.child(iterString).updateChildren(taskString);
                tasksRoot.child(iterString).updateChildren(taskDone);

                iter++;
            }
        }
    }
}
