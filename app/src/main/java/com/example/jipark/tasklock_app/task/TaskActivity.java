package com.example.jipark.tasklock_app.task;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.SpaceTokenizer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity implements TasksAdapter.AdapterCallback {
    private List<Task> taskList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private MultiAutoCompleteTextView mMultiAutoCompleteTextView;
    private String tasksFileName = "tasks.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //initializing views
        initAutoCompleteTextView();
        initRecyclerView();

        //readTasks
        if (isFilePresent(this, tasksFileName)) {
            loadTasks(this, tasksFileName);
        }
        else {
            //file doesn't exist
        }
    }

    @Override
    public void onMethodCallback() {
        saveTasks();
    }

    public void addTask(View view) { //grab value from EditText, create a Task object, and add it to RecyclerView.
        String taskText = mMultiAutoCompleteTextView.getText().toString();
        if (!taskText.isEmpty()) {
            mMultiAutoCompleteTextView.getText().clear();
            //hide soft keyboard
            InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            Task task = new Task(taskText);
            taskList.add(task);
            mAdapter.notifyItemInserted(taskList.size() - 1);
            saveTasks();
        }
        else {
            Toast.makeText(this, "Cannot create empty task!", Toast.LENGTH_SHORT).show();
        }
    }

    public void finishTaskActivity(View view) {
        saveTasks();
        finish();
    }

    private boolean initAutoCompleteTextView() {
        mMultiAutoCompleteTextView = (MultiAutoCompleteTextView)findViewById(R.id.task_edit_text);

        //load in words from keywords.txt from assets
        BufferedReader reader;
        String line = "";
        List<String> wordList = new ArrayList<>();
        try {
            final InputStream file = getAssets().open("keywords.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            while(line != null) {
                line = reader.readLine();
                if (line != null) {
                    wordList.add(line);
                }
            }
        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
        String[] words = new String[wordList.size()];
        words = wordList.toArray(words);
        ArrayAdapter<String> wordAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, words);
        mMultiAutoCompleteTextView.setThreshold(2); //how many letters before it suggests a word
        mMultiAutoCompleteTextView.setTokenizer(new SpaceTokenizer()); //each token is separated with a space
        mMultiAutoCompleteTextView.setAdapter(wordAdapter);
        return true;
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.task_list);
        mAdapter = new TasksAdapter(taskList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }

    private void loadTasks(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonTask = new JSONObject(sb.toString());
            JSONArray jsonTasksArray = jsonTask.getJSONArray("tasks");

            for (int i = 0; i < jsonTasksArray.length(); i++) {
                taskList.add(new Task(jsonTasksArray.getString(i)));
            }
            mAdapter.notifyDataSetChanged();

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

    public boolean saveTasks() {
        JSONArray jsonTasksArray = new JSONArray();
        JSONObject jsonTasks = new JSONObject();
        String jsonString;

        try {
            for (int i = 0; i < taskList.size(); i++) {
                jsonTasksArray.put(taskList.get(i).getTask());
            }
            jsonTasks.put("tasks", jsonTasksArray);
            jsonString = jsonTasks.toString();

            FileOutputStream fos = openFileOutput(tasksFileName, Context.MODE_PRIVATE);
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

    private boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }
}