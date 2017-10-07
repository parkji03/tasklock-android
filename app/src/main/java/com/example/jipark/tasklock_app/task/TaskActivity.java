package com.example.jipark.tasklock_app.task;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.SpaceTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private TasksAdapter mAdapter;
    private MultiAutoCompleteTextView mMultiAutoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //initializing views
        initAutoCompleteTextView();
        initRecyclerView();
    }

    public void addTask(View view) { //grab value from EditText, create a Task object, and add it to RecyclerView.
        String taskText = mMultiAutoCompleteTextView.getText().toString();
        if (!taskText.isEmpty()) {
            Task task = new Task(taskText);
            taskList.add(task);
            mAdapter.notifyItemInserted(taskList.size() - 1);
        }
        else {
            Toast.makeText(this, "Cannot create empty task!", Toast.LENGTH_SHORT).show();
        }
    }

    public void finishTaskActivity(View view) {
        saveTasks(); //TODO: update saveTask
        finish();
    }

    private boolean initAutoCompleteTextView() {
        mMultiAutoCompleteTextView = (MultiAutoCompleteTextView)findViewById(R.id.task_edit_text);
        String[] words = { "study", "math", "chemistry", "homework", "read", "history", "science", "biology", "cook", "clean", "scriptures"}; //TODO: change this to load a .txt file of keywords
        ArrayAdapter<String> wordAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, words);
        mMultiAutoCompleteTextView.setThreshold(2);
//        mMultiAutoCompleteTextView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        mMultiAutoCompleteTextView.setTokenizer(new SpaceTokenizer());
        mMultiAutoCompleteTextView.setAdapter(wordAdapter);
        return true;
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.task_list);
        mAdapter = new TasksAdapter(taskList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }

    private boolean saveTasks() {
        return false;
    }

    //reading data from assets
    private String loadJSON() {
        String jsonOutput = null;
        try {
            InputStream istream = this.getAssets().open("tasks.json");
            int size = istream.available();
            byte[] buffer = new byte[size];
            istream.read(buffer);
            istream.close();
            jsonOutput = new String(buffer, "UTF-8");
            return jsonOutput;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean toJSON() {
        return false;
    }
}


//    public String loadJSONFromAsset() {
//        String json = null;
//        try {
//            InputStream is = getActivity().getAssets().open("yourfilename.json");
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            json = new String(buffer, "UTF-8");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//        return json;
//    }
//
//    try {
//            JSONObject obj = new JSONObject(loadJSONFromAsset());
//            JSONArray m_jArry = obj.getJSONArray("formules");
//            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
//        HashMap<String, String> m_li;
//
//        for (int i = 0; i < m_jArry.length(); i++) {
//        JSONObject jo_inside = m_jArry.getJSONObject(i);
//        Log.d("Details-->", jo_inside.getString("formule"));
//        String formula_value = jo_inside.getString("formule");
//        String url_value = jo_inside.getString("url");
//
//        //Add your values in your `ArrayList` as below:
//        m_li = new HashMap<String, String>();
//        m_li.put("formule", formula_value);
//        m_li.put("url", url_value);
//
//        formList.add(m_li);
//        }
//        } catch (JSONException e) {
//        e.printStackTrace();
//        }

