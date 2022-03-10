package com.fnp.reactnativesyncadapter;

import androidx.core.app.NotificationCompat;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import java.util.ArrayList;
import java.util.List;

public class HeadlessService extends HeadlessJsTaskService {

    private static final String TASK_ID = "TASK_SYNC_ADAPTER";
    
    private final IBinder mBinder = new LocalBinder();
    private List<Callback> mCallbacks = new ArrayList<>();
    
    public class LocalBinder extends Binder {
        HeadlessService getService() {
            return HeadlessService.this;
        }
    }
    
    public interface Callback {
        void onTaskCompletion();
    }
    
    @Override
    public @Nullable
    IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    public void notifyOnTaskCompletion(Callback cb) {
        mCallbacks.add(cb);
    }
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {        
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel chan = new NotificationChannel(
					getString(R.string.rnsb_notification_channel_id),
					getText(R.string.rnsb_notification_name),
					NotificationManager.IMPORTANCE_LOW);
			chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.rnsb_notification_channel_id));
			Notification notification = notificationBuilder.setOngoing(true)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setContentTitle(getText(R.string.rnsb_notification_text))
					.setPriority(NotificationManager.IMPORTANCE_LOW)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setChannelId(getString(R.string.rnsb_notification_channel_id))
					.build();

			startForeground(1, notification);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
    
    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
        super.onHeadlessJsTaskFinish(taskId);
        for (Callback cb : mCallbacks) {
            cb.onTaskCompletion();
        }
        mCallbacks.clear();
    }

    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        return new HeadlessJsTaskConfig(
                TASK_ID,
                extras != null ? Arguments.fromBundle(extras) : Arguments.createMap(),
                Long.valueOf(getString(R.string.rnsb_default_timeout)),
                true);
    }

    // From https://facebook.github.io/react-native/docs/headless-js-android.html
    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        assert activityManager != null;
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();

        if (appProcesses == null) {
            return false;
        }

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }
}