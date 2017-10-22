package com.example.jipark.tasklock_app.task;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
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
import com.example.jipark.tasklock_app.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity implements TasksAdapter.TasksAdapterCallback {
    private Utils SINGLETON;
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private TextView mHiddenText;
    private MultiAutoCompleteTextView mMultiAutoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        SINGLETON = Utils.getInstance();

        //initializing views
        initAutoCompleteTextView();
        initRecyclerView();
        mHiddenText = (TextView)findViewById(R.id.hidden_text);
        mAdapter.notifyDataSetChanged();
        showHiddenText();
    }

    @Override
    public void onMethodCallback() {
        SINGLETON.saveTasks(this);
        //TODO: send database that we completed a task...
        showHiddenText();
    }

    public void addTask(View view) { //grab value from EditText, create a Task object, and add it to RecyclerView.
        String taskText = mMultiAutoCompleteTextView.getText().toString();
        if (!taskText.isEmpty()) {
            mMultiAutoCompleteTextView.setText("");
            Task task = new Task(taskText, false);
            SINGLETON.addTaskToHead(task);
            mAdapter.notifyItemInserted(0);
            mRecyclerView.smoothScrollToPosition(0);
            mAdapter.notifyDataSetChanged();
            SINGLETON.saveTasks(this);
            showHiddenText();
        }
        else {
            Toast.makeText(this, "Cannot create empty task!", Toast.LENGTH_SHORT).show();
        }
    }

    public void finishTaskActivity(View view) {
        SINGLETON.saveTasks(this);
        finish();
    }

    private boolean initAutoCompleteTextView() {
        mMultiAutoCompleteTextView = (MultiAutoCompleteTextView)findViewById(R.id.task_edit_text);

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
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //hide soft keyboard
                    mMultiAutoCompleteTextView.setText("");
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
        mAdapter = new TasksAdapter(SINGLETON.getTaskList(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }

    private void showHiddenText() {
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(1100);

        if (SINGLETON.getTaskList().isEmpty()) {
            mRecyclerView.setVisibility(View.GONE);
            mHiddenText.startAnimation(in);
            mHiddenText.setVisibility(View.VISIBLE);
        }
        else {
            mHiddenText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}