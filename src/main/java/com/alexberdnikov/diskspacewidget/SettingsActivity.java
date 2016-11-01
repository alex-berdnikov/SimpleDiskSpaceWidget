package com.alexberdnikov.diskspacewidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;


public class SettingsActivity extends Activity {
    private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                   AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultData = new Intent();
        resultData.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        setResult(RESULT_CANCELED, resultData);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.settings_area, new SettingsFragment()).commit();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.inflateMenu(R.menu.settings);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.buyer_toolbar_menu_save_button) {
                    Intent resultData = new Intent();
                    resultData.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

                    if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        setResult(RESULT_OK, resultData);
                    }

                    finish();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(getApplicationContext(), DiskSpaceWidget.class);
        intent.setAction(DiskSpaceWidget.ACTION_UPDATE);
        sendBroadcast(intent);
        super.onStop();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
