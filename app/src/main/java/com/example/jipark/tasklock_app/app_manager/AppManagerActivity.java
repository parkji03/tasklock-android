package com.example.jipark.tasklock_app.app_manager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.jipark.tasklock_app.R;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class AppManagerActivity extends AppCompatActivity {
    private PackageManager pm;
    private List<ApplicationInfo> apps;
    private List<ApplicationInfo> userApps = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> labelList = new ArrayList();
    private List<Drawable> iconList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        pm = getPackageManager();
        apps = pm.getInstalledApplications(0);

        loadUserApplications();
    }

    public void loadUserApplications(){

        //create a recycler view for the labels and icons
        recyclerView = (RecyclerView)findViewById(R.id.app_manager_list);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        for(ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                userApps.add(app);
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else {
                userApps.add(app);
            }

            String label = (String) pm.getApplicationLabel(app);
            Drawable icon = pm.getApplicationIcon(app);
            labelList.add(label);
            iconList.add(icon);
        }

        adapter = new AppManagerAdapter(labelList, iconList);
        recyclerView.setAdapter(adapter);
    }


    //TODO: https://stackoverflow.com/questions/36261909/how-to-make-app-lock-app-in-android
    //TODO: https://stackoverflow.com/questions/45829540/how-to-code-an-app-that-blocks-other-apps-from-launching-for-a-certain-time-usin
    //TODO: https://developer.android.com/reference/android/Manifest.permission.html#SYSTEM_ALERT_WINDOW
    //TODO: https://stackoverflow.com/questions/7623767/how-do-android-app-lock-applications-work




}
