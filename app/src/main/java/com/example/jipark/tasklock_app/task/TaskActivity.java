package com.example.jipark.tasklock_app.task;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
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
    private TextView mHiddenText;
    private MultiAutoCompleteTextView mMultiAutoCompleteTextView;
    private String tasksFileName = "tasks.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        taskList = (List<Task>)getIntent().getSerializableExtra("myTasks");
        //initializing views
        initAutoCompleteTextView();
        initRecyclerView();
        mHiddenText = (TextView)findViewById(R.id.hidden_text);
        mAdapter.notifyDataSetChanged();
        showHiddenText();
    }

    @Override
    public void onMethodCallback() {
        saveTasks();
        showHiddenText();
    }

    public void addTask(View view) { //grab value from EditText, create a Task object, and add it to RecyclerView.
        String taskText = mMultiAutoCompleteTextView.getText().toString();
        if (!taskText.isEmpty()) {
            mMultiAutoCompleteTextView.getText().clear();
            Task task = new Task(taskText, false);
            taskList.add(task);
            mAdapter.notifyItemInserted(taskList.size() - 1);
            saveTasks();
            showHiddenText();
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

        //TODO: add animations for focus change
        mMultiAutoCompleteTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(hasFocus) {
                    //shorten animation
                }
                else {
                    //expand animation
                }
            }
        });

        mMultiAutoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
//                    addTask(mMultiAutoCompleteTextView);
                    //hide soft keyboard
                    mMultiAutoCompleteTextView.getText().clear();
                    InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    mMultiAutoCompleteTextView.clearFocus();
                }
                return false;
            }
        });

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

//    private void loadTasks(Context context, String fileName) {
//        try {
//            FileInputStream fis = context.openFileInput(fileName);
//            InputStreamReader isr = new InputStreamReader(fis);
//            BufferedReader bufferedReader = new BufferedReader(isr);
//            StringBuilder sb = new StringBuilder();
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                sb.append(line);
//            }
//
//            JSONObject jsonTask = new JSONObject(sb.toString());
//            JSONArray jsonTasksArray = jsonTask.getJSONArray("tasks");
//
//            for (int i = 0; i < jsonTasksArray.length(); i++) {
//                taskList.add(new Task(jsonTasksArray.getString(i)));
//            }
//            mAdapter.notifyDataSetChanged();
//
//        }
//        catch (JSONException e) {
//            e.printStackTrace();
//        }
//        catch (FileNotFoundException fileNotFound) {
//            fileNotFound.printStackTrace();
//        }
//        catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//    }

    public boolean saveTasks() {
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

    private void showHiddenText() {
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(1100);

        if (taskList.isEmpty()) {
            mHiddenText.startAnimation(in);
            mHiddenText.setVisibility(View.VISIBLE);
        }
        else {
            mHiddenText.setVisibility(View.GONE);
        }
    }
}