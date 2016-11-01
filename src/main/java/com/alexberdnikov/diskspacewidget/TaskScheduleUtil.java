package com.alexberdnikov.diskspacewidget;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

public class TaskScheduleUtil {
    private static final int DEFAULT_UPDATE_INTERVAL = 60000;
    private static final int CHARGING_UPDATE_INTERVAL = 10000;

    private static boolean sIsChargeMode = false;

    private TaskScheduleUtil() {}

    public static void scheduleDefaultRepeatUpdate(Context context) {
        sIsChargeMode = false;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingResult(context);

        am.cancel(pi);
        am.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + DEFAULT_UPDATE_INTERVAL,
                DEFAULT_UPDATE_INTERVAL,
                pi
        );
    }

    public static void scheduleFastSingleUpdate(Context context) {
        sIsChargeMode = true;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = createPendingResult(context);

        am.cancel(pi);
        long alarmTime = SystemClock.elapsedRealtime() + CHARGING_UPDATE_INTERVAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(
                    AlarmManager.ELAPSED_REALTIME,
                    alarmTime,
                    pi
            );
        } else {
            am.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pi);
        }
    }

    public static boolean isChargeMode() {
        return sIsChargeMode;
    }

    /*private static void scheduleUpdate(Context context, int updateInterval) {

    }*/

    private static PendingIntent createPendingResult(Context context) {
        Intent intent = new Intent(context, DiskSpaceWidget.class);
        intent.setAction(DiskSpaceWidget.ACTION_UPDATE);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public static void clearUpdate(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(createPendingResult(context));
    }
}
