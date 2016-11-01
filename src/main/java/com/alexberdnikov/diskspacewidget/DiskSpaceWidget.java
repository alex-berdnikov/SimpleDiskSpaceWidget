package com.alexberdnikov.diskspacewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;


public class DiskSpaceWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE = "com.alexberdnikov.diskspacewidget.UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
        ComponentName componentName = new ComponentName(context, DiskSpaceWidget.class);
        mgr.updateAppWidget(componentName, buildUpdate(context, appWidgetIds));

        // In charge mode single updates are scheduled one after another, not repeatedly.
        if (TaskScheduleUtil.isChargeMode()) {
            TaskScheduleUtil.scheduleFastSingleUpdate(context);
        }
    }

    private void onUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName = new ComponentName(context.getPackageName(),getClass().getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        TaskScheduleUtil.scheduleDefaultRepeatUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
        TaskScheduleUtil.clearUpdate(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_UPDATE)) {
            onUpdate(context);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
            // When the device is charging we might not save the battery and
            // update widget more often.
            TaskScheduleUtil.scheduleFastSingleUpdate(context);
        } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
            TaskScheduleUtil.scheduleDefaultRepeatUpdate(context);
        } else {
            super.onReceive(context, intent);
        }
    }

    private RemoteViews buildUpdate(Context context, int[] appWidgetIds) {
        String[] storageDirs = StorageUtil.getStorageDirectories();

        long primaryDiskAvailableMegs = StorageUtil.getAvailableMegabytes(storageDirs[0]);
        long primaryDiskTotalMegs = StorageUtil.getTotalMegabytes(storageDirs[0]);
        boolean hasSdCard = (storageDirs.length > 1
                && 0 < StorageUtil.getAvailableMegabytes(storageDirs[1]));

        RemoteViews updateViews = new RemoteViews(
                context.getPackageName(),
                hasSdCard ? R.layout.two_storages : R.layout.primary_storage
        );

        // Get widget settings
        SharedPrefs prefsManager = SharedPrefs.getsInstance(context);
        SharedPreferences prefs = prefsManager.getDefaultPreferences();
        int indicatorsColor = prefs.getInt(context.getString(
                R.string.prefs_key_space_indicators_colors), R.color.defaultGreen);
        String hintFormat = prefs.getString(context.getString(
                R.string.prefs_key_hint_format), context.getString(R.string.hint_amount));

        float primaryUsedPercentage = calcUsedSpacePercentage(
                primaryDiskAvailableMegs, primaryDiskTotalMegs);
        Bitmap primaryIndicatorBitmap = SpaceIndicatorImageHelper.drawIndicator(
                Math.round(primaryUsedPercentage),
                indicatorsColor
        );
        updateViews.setImageViewBitmap(R.id.primary_storage_space_indicator, primaryIndicatorBitmap);

        String primaryHint;
        if (hintFormat.equals(context.getString(R.string.hint_amount))) {
            primaryHint = getPrimaryFreeSpaceText(
                    context, primaryDiskAvailableMegs, primaryDiskTotalMegs);
        } else {
            primaryHint = getPrimarySpacePercentageText(context, primaryUsedPercentage);
        }

        updateViews.setTextViewText(
                R.id.primary_storage_free_space_hint,
                primaryHint
        );

        if (hasSdCard) {
            long secondaryDiskAvailableMegs = StorageUtil.getAvailableMegabytes(storageDirs[1]);
            long secondaryDiskTotalMegs = StorageUtil.getTotalMegabytes(storageDirs[1]);

            float secondaryUsedPercentage = calcUsedSpacePercentage(
                    secondaryDiskAvailableMegs, secondaryDiskTotalMegs);
            Bitmap secondaryIndicatorBitmap = SpaceIndicatorImageHelper.drawIndicator(
                    Math.round(secondaryUsedPercentage),
                    indicatorsColor
            );
            updateViews.setImageViewBitmap(
                    R.id.secondary_storage_space_indicator, secondaryIndicatorBitmap
            );

            String secondaryHint;
            if (hintFormat.equals(context.getString(R.string.hint_amount))) {
                secondaryHint = getSecondaryFreeSpaceText(
                        context, secondaryDiskAvailableMegs, secondaryDiskTotalMegs);
            } else {
                secondaryHint = getSecondarySpacePercentageText(context, secondaryUsedPercentage);
            }

            updateViews.setTextViewText(
                    R.id.secondary_storage_free_space_hint,
                    secondaryHint
            );
        }

        Intent configIntent = new Intent(context, SettingsActivity.class);
        PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget_content, configPendingIntent);

        return updateViews;
    }

    private String getPrimaryFreeSpaceText(Context context, long freeMegs, long totalMegs) {
        return getFreeSpaceText(
                context, freeMegs, totalMegs, R.string.label_primary_space_amount_hint);
    }

    private String getSecondaryFreeSpaceText(Context context, long freeMegs, long totalMegs) {
        return getFreeSpaceText(
                context, freeMegs, totalMegs, R.string.label_secondary_space_amount_hint);
    }

    private String getPrimarySpacePercentageText(Context context, float usedPercents) {
        return String.format(context.getString(
                R.string.label_primary_space_percentage_hint), 100 - usedPercents);
    }

    private String getSecondarySpacePercentageText(Context context, float usedPercents) {
        return String.format(context.getString(
                R.string.label_secondary_space_percentage_hint), 100 - usedPercents);
    }

    private String getFreeSpaceText(Context context, long freeMegs, long totalMegs, int stringRes) {
        float freeGbs = (float) freeMegs / 1024;
        float totalGbs = (float) totalMegs / 1024;

        return String.format(
                context.getString(stringRes),
                freeGbs, totalGbs
        );
    }

    private float calcUsedSpacePercentage(long freeMegs, long totalMegs) {
        float usedMegs = totalMegs - freeMegs;
        return (usedMegs / totalMegs) * 100;
    }
}
