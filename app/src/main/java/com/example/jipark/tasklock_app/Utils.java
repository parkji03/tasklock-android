package com.example.jipark.tasklock_app;

import android.app.Activity;
import android.content.Context;

import com.example.jipark.tasklock_app.task.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
import java.util.List;

/**
 * Created by jipark on 10/13/17.
 */

public class Utils {
    private static final Utils ourInstance = new Utils();
    private final String tasksFileName = "tasks.json";
    private final String AB = "0123456789ABCDEFGHJKLMNOPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"; //doesn't contain I or l for avoiding ambiguity
    private final int roomKeyLength = 6;
    private final int ownerIDLength = 4;

    private List<Task> taskList;
    private DatabaseReference roomsReference;
    private String masterRoomKey;
    private boolean owner;
    private boolean joiner;
    private boolean paired;

    private SecureRandom rnd;

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
    }

    public String generateRandomString(int len){
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

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isJoiner() {
        return joiner;
    }

    public void setJoiner(boolean joiner) {
        this.joiner = joiner;
    }

    public String getMasterRoomKey() {
        return masterRoomKey;
    }

    public void setMasterRoomKey(String masterRoomKey) {
        this.masterRoomKey = masterRoomKey;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }
}
