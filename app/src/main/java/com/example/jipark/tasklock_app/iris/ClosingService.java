package com.example.jipark.tasklock_app.iris;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.jipark.tasklock_app.Utils;

/**
 * Created by jipark on 10/22/17.
 */

public class ClosingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        // Handle application closing
        fireClosingNotification();

        // Destroy the service
        stopSelf();
    }

    private void fireClosingNotification() {
        if (Utils.getInstance().isJoiner() && Utils.getInstance().isPaired()) {
            Utils.getInstance().getRoomsReference().child(Utils.getInstance().getMasterRoomKey()).child("owner").removeEventListener(Utils.getInstance().checkOwnerDisconnectedListener);
            Utils.getInstance().getRoomsReference().removeEventListener(Utils.getInstance().checkRoomExistsBeforeJoinListener);
            Utils.getInstance().disconnectJoinerFromRoom();
            Utils.getInstance().resetLocalJoinerValues();
        }

    }
}