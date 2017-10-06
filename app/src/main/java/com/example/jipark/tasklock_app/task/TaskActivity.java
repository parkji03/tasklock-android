package com.example.jipark.tasklock_app.task;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TasksAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        recyclerView = (RecyclerView)findViewById(R.id.task_list);
        mAdapter = new TasksAdapter(taskList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        loadTasksData();
    }

    private void loadTasksData() {
        Task task = new Task("Test task 1");
        taskList.add(task);
        task = new Task("Test task 2");
        taskList.add(task);
        task = new Task("Test task 3");
        taskList.add(task);
        task = new Task("Test task 4");
        taskList.add(task);
        task = new Task("Test task 5");
        taskList.add(task);
        task = new Task("Test task 6");
        taskList.add(task);
        task = new Task("Test task 7");
        taskList.add(task);
        task = new Task("Test task 8");
        taskList.add(task);
        task = new Task("Test task 9");
        taskList.add(task);
        task = new Task("Test task 10");
        taskList.add(task);
        task = new Task("Test task 11");
        taskList.add(task);
        task = new Task("Test task 12");
        taskList.add(task);
        task = new Task("Test task 13");
        taskList.add(task);
        task = new Task("Test task 14");
        taskList.add(task);
        task = new Task("Test task 15");
        taskList.add(task);

        mAdapter.notifyDataSetChanged();
    }












    public void finishTaskActivity(View view) {
        saveTasks();
        finish();
    }

















    //reading and writing data
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

    private boolean saveTasks() {
        String FILENAME = "tasks.json";

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





//public class ListViewDemo extends ListActivity {
//    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
//    ArrayList<String> listItems=new ArrayList<String>();
//
//    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
//    ArrayAdapter<String> adapter;
//
//    //RECORDING HOW MANY TIMES THE BUTTON HAS BEEN CLICKED
//    int clickCounter=0;
//
//    @Override
//    public void onCreate(Bundle icicle) {
//        super.onCreate(icicle);
//        setContentView(R.layout.main);
//        adapter=new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1,
//                listItems);
//        setListAdapter(adapter);
//    }
//
//    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
//    public void addItems(View v) {
//        listItems.add("Clicked : "+clickCounter++);
//        adapter.notifyDataSetChanged();
//    }
//}