package com.example.jipark.tasklock_app.app_manager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.jipark.tasklock_app.R;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends AppCompatActivity {
    private PackageManager pm;
    private List<ApplicationInfo> apps;
    private List<ApplicationInfo> userApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

//        pm = getPackageManager();
//        apps = pm.getInstalledApplications(0);

    }

//    public void loadUserApplications(){
//        for(ApplicationInfo app : apps) {
//            //checks for flags; if flagged, check if updated system app
//            if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
//                userApps.add(app);
//                //it's a system app, not interested
//            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
//                //Discard this one
//                //in this case, it should be a user-installed app
//            } else {
//                userApps.add(app);
//            }
//        }

//        String label = (String)pm.getApplicationLabel(app);
//        Drawable icon = pm.getApplicationIcon(app);
//    }


    //TODO: https://stackoverflow.com/questions/36261909/how-to-make-app-lock-app-in-android
    //TODO: https://stackoverflow.com/questions/45829540/how-to-code-an-app-that-blocks-other-apps-from-launching-for-a-certain-time-usin
    //TODO: https://developer.android.com/reference/android/Manifest.permission.html#SYSTEM_ALERT_WINDOW
    //TODO: https://stackoverflow.com/questions/7623767/how-do-android-app-lock-applications-work




}
