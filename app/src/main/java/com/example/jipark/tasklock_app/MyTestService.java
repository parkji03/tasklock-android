package com.example.jipark.tasklock_app;

import android.app.Service;
import android.content.Intent;
import android.content.*;
import android.os.*;
import android.widget.Toast;

import com.rvalerio.fgchecker.AppChecker;

/**
 * Created by Scott on 10/21/2017.
 */

public class MyTestService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                AppChecker appChecker = new AppChecker();
                appChecker.start(context);
                String packageName = appChecker.getForegroundApp(context);
                Toast.makeText(context, packageName, Toast.LENGTH_LONG).show();

                handler.postDelayed(runnable, 5);
            }
        };

        handler.postDelayed(runnable, 15000);
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }
}