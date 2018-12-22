package com.ubtrobot.uslam;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.ubtrobot.uslam.utils.NetUtils;

public class ChooseRobotActivity extends BaseActivity {

    private TextView mButtonChangeWifi;
    private TextView mWifiName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_robot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        mButtonChangeWifi = findViewById(R.id.button_choose_wifi);
        mWifiName = findViewById(R.id.wifi_name);
        mButtonChangeWifi.setOnClickListener(view -> {
            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String wifiName = NetUtils.getConnectWifiSsid();
        if (!TextUtils.isEmpty(wifiName)) {
            mWifiName.setText(wifiName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.choose_robot_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            SharedPreferences sharedPref = getSharedPreferences(
                    getString(R.string.preference_file_key),
                    MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(
                    getString(R.string.remember_password_key),
                    false);
            editor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.offline_edit) {
            LocalMapListActivity.launch(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
