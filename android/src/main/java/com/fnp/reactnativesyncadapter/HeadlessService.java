package com.fnp.reactnativesyncadapter;

import androidx.core.app.NotificationCompat;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import java.util.List;

public class HeadlessService extends HeadlessJsTaskService {

    private static final String TASK_ID = "TASK_SYNC_ADAPTER";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel chan = new NotificationChannel(
					"ForegroundNotifChannelId",
					"SyncAdapter Foreground Service",
					NotificationManager.IMPORTANCE_LOW);
			chan.setLightColor(Color.BLUE);
			chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "ForegroundNotifChannelId");
			Notification notification = notificationBuilder.setOngoing(true)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setContentTitle(getText(R.string.rnsb_notification_text))
					.setPriority(NotificationManager.IMPORTANCE_LOW)
					.setCategory(Notification.CATEGORY_SERVICE)
					.setChannelId("ForegroundNotifChannelId")
					.build();

			startForeground(1, notification);
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        boolean allowForeground = Boolean.parseBoolean(getString(R.string.rnsb_allow_foreground));

        if(allowForeground || !isAppOnForeground(this)) {
            Bundle extras = intent.getExtras();
            WritableMap data = extras != null ? Arguments.fromBundle(extras) : Arguments.createMap();
            return new HeadlessJsTaskConfig(
                    TASK_ID,
                    data,
                    Long.valueOf(getString(R.string.rnsb_default_timeout)),
                    allowForeground);
        }

        stopSelf();
        return null;
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