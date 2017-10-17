package com.example.jipark.tasklock_app;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.jipark.tasklock_app.task.Task;
import com.example.jipark.tasklock_app.app_manager.App;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jipark on 10/13/17.
 */

public class Utils {
    private static final Utils ourInstance = new Utils();
    private List<Task> taskList;
    private String tasksFileName;
    private List<App> appList;
    private String appsFileName;

    public static Utils getInstance() {
        return ourInstance;
    }

    private Utils() {
        taskList = new ArrayList<>();
        tasksFileName = "tasks.json";
        appList = new ArrayList<>();
        appsFileName = "apps.json";
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

    public void loadApps(Context context) {
        try {
            FileInputStream fis = context.openFileInput(appsFileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonAppListHolder = new JSONObject(sb.toString());
            JSONArray jsonAppList = jsonAppListHolder.getJSONArray("apps");
            JSONObject jsonApp;
            String appText;
            boolean isDisabled;

            for (int i = 0; i < jsonAppList.length(); i++) {
                jsonApp = jsonAppList.getJSONObject(i);
                appText = jsonApp.getString("app");
                isDisabled = jsonApp.getBoolean("disabled");

                appList.add(new App(appText, isDisabled));
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

    public boolean saveApps(Activity activity) {
        JSONObject jsonAppListHolder = new JSONObject();
        JSONArray jsonAppList = new JSONArray();
        JSONObject jsonApp;
        String jsonString;

        try {
            for (int i = 0; i < appList.size(); i++) {
                jsonApp = new JSONObject();
                jsonApp.put("app", appList.get(i).getName());
                jsonApp.put("disabled", appList.get(i).isDisabled());
                jsonAppList.put(jsonApp);
            }
            jsonAppListHolder.put("apps", jsonAppList);
            jsonString = jsonAppListHolder.toString();

            FileOutputStream fos = activity.getApplicationContext().openFileOutput(appsFileName, Context.MODE_PRIVATE);
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

    public void addAppToHead(App app) {
        appList.add(0, app);
    }

    public void resetTaskList() {
        taskList.clear();
    }

    public void resetAppList() { appList.clear(); }

    public List<Task> getTaskList() {
        return taskList;
    }

    public List<App> getAppList() {
        return appList;
    }

    public int getTaskCount() {
        return taskList.size();
    }

    public int getAppCount() {
        return appList.size();
    }

    public boolean checkTasksAllTrue() {
        for (int i = 0; i < taskList.size(); i++) {
            if(!taskList.get(i).isComplete()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkAppsAllTrue() {
        for (int i = 0; i < appList.size(); i++) {
            if(!appList.get(i).isDisabled()) {
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

    public int getAppsRemaining() {
        int remaining = 0;
        for (int i = 0; i < appList.size(); i++) {
            if(!appList.get(i).isDisabled())
                remaining++;
        }
        return remaining;
    }

    public boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }
}
