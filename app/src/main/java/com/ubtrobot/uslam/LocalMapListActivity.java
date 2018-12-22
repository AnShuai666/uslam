package com.ubtrobot.uslam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.ubtrobot.uslam.fragments.LocalMapListFragment;

/**
 * @author leo
 * @date 2018/12/17
 * @email ao.liu@ubtrobot.com
 */
public class LocalMapListActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_map_list);
        Toolbar toolBar = findViewById(R.id.tool_bar);
        toolBar.setTitle("地图列表");
        setSupportActionBar(toolBar);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, LocalMapListFragment.newInstance()).commit();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, LocalMapListActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
