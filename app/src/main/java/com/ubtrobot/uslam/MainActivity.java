package com.ubtrobot.uslam;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.ubtrobot.uslam.data.MapSaver;
import com.ubtrobot.uslam.fragments.MapFragment;
import com.ubtrobot.uslam.fragments.MapListDialogFragment;
import com.ubtrobot.uslam.sdk.FakeLocalSdk;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.sdk.RobotSdkImp;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotMode;
import com.ubtrobot.uslam.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_BASE_URL = "intent_extra_base_url";
    private static final String TAG = "MainActivity";
    private static final int FILE_REQUEST_CODE = 1000;
    private IRemoteRobotSdk mSdk = SdkManager.getSdk();
    private String mBaseUrl;
    private View mProgressView;

    private Toolbar mToolbar;
    private Button mChooseMapButton;
    private List<RobotMap> mMaps;
    private RobotMap mCurrentMap;
    private MapFragment mMapFragment;
//    private Menu mMenu;
    private Spinner mModeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setNavigationIcon(R.mipmap.ic_back);
        mToolbar.setTitleMarginStart(ViewUtils.dp2px(this, 6));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(v -> {
            this.finish();
        });

        mBaseUrl = getIntent().getStringExtra(EXTRA_BASE_URL);

        String fileName = getIntent().getStringExtra(KEY_MAP_NAME);

        mProgressView = findViewById(R.id.main_activity_progress_bar);
        mMapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        mChooseMapButton= findViewById(R.id.button_choose_map);
        mMapFragment.setChooseMapButton(mChooseMapButton);
        mModeSpinner = findViewById(R.id.button_choose_mode);
        mMapFragment.setModeSpinner(mModeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.choose_mode, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mModeSpinner.setAdapter(adapter);
        mModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) view;
                if (position == 3) {
                    findViewById(R.id.control_panel).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.control_panel).setVisibility(View.VISIBLE);
                }
                textView.setTextColor(getResources().getColor(android.R.color.white));
                switch (position) {
                    case 0: // navigation
                        mMapFragment.setMode(MapFragment.Mode.Navigation);
                        break;
                    case 1:
                        mMapFragment.setMode(MapFragment.Mode.Mapping);
                        break;
                    case 2:
                        mMapFragment.setMode(MapFragment.Mode.ContinueMapping);
                        break;
                    case 3:
                        mMapFragment.setMode(MapFragment.Mode.MappingAdvanceEdit);
                        break;
                    default:
//                        Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
//                        intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
//                                .setCheckPermission(true)
//                                .setShowImages(false)
//                                .setShowVideos(false)
//                                .setSingleClickSelection(true)
//                                .enableImageCapture(false)
//                                .setMaxSelection(1)
//                                .setSkipZeroSizeFiles(true)
//                                .setSuffixes("umap")
//                                .build());
//                        startActivityForResult(intent, FILE_REQUEST_CODE);
                        List<MapFile> localMapList = MapSaver.getLocalMapList();
                        for (MapFile robotMap : localMapList) {
                            Log.i("leo", "获得本地地图列表" + robotMap.fileName);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mChooseMapButton.setOnClickListener(v -> {
            fetchMapList();
        });
        if (mSdk instanceof FakeLocalSdk) {
            mSdk.setContext(this);
        }

        if (((RobotSdkImp) mSdk).getRobots().size() > 2) {
            connectRobot();
        }

        if (!TextUtils.isEmpty(fileName)) {
            setMode(MapFragment.Mode.MappingAdvanceEdit);
            RobotMap robotMap = MapSaver.getRobotMapFromFile(fileName);
            setCurrentMap(robotMap);
        }

    }

    private void fetchMapList() {
        showProgress(true);
        mSdk.requestMapList(maps -> {
            mMaps = maps;
            runOnUiThread(this::showChooseMapDialog);
        });
    }

    public void fetchMapByName(String mapName) {
        showProgress(true);
        mSdk.requestMap(mapName, false, true, false, false, map -> {
            setCurrentMap(map);
            runOnUiThread(() -> {
                mChooseMapButton.setText(mapName);
                showProgress(false);
            });
        });
    }


    private void showChooseMapDialog() {
        showProgress(false);
        MapListDialogFragment
                .newInstance(((ArrayList<RobotMap>) mMaps))
                .show(getSupportFragmentManager(), "");
    }

    private void connectRobot() {
        showProgress(true);
        mSdk.connectRobot(mBaseUrl, (robot -> {
            if (robot.getCurrentMapName() == null
                    || robot.getCurrentMapName().isEmpty()) {
                if(robot.mode == RobotMode.Navigation) {
                    runOnUiThread(() -> {
                        mToolbar.setTitle(robot.getRobotName());
                        mChooseMapButton.callOnClick();
                        showProgress(false);
                    });
                } else {
                    runOnUiThread(() -> showProgress(false));
                }
            } else {
                runOnUiThread(() -> {
                    mToolbar.setTitle(robot.getRobotName());
//                    mChooseMapButton.setText(robot.getCurrentMapName());
                });
                if (robot.mode == RobotMode.Mapping) {
                    mMapFragment.setMode(MapFragment.Mode.Mapping);
                    mChooseMapButton.setText(R.string.doing_mapping);
                    mMapFragment.setMap(new RobotMap("temp"));

                } else {
                    mMapFragment.setMode(MapFragment.Mode.Navigation);
                    mChooseMapButton.setText(robot.getCurrentMapName());
                }
                if(robot.mode == RobotMode.Navigation) {
                    fetchMapByName(robot.getCurrentMapName());
                } else {
                    runOnUiThread(() -> showProgress(false));
                }
            }
        }));
        RobotMode currentMode = mSdk.getCurrentRobot().mode;
        switch (currentMode) {
            case Mapping:
                mModeSpinner.setSelection(1);
                break;
            default:
                mModeSpinner.setSelection(0);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_REQUEST_CODE:
                if(mModeSpinner.getSelectedItemPosition() == 3) {
                    findViewById(R.id.control_panel).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.control_panel).setVisibility(View.VISIBLE);
                }
                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                if(files.size() > 0)
                    Log.d("RobotFragment", files.get(0).toString());
                break;
        }
    }

    private void setCurrentMap(RobotMap map) {
        mCurrentMap = map;
        mMapFragment.setMap(map);
        mMapFragment.setMode(MapFragment.Mode.Navigation);
    }

    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        View mapViewGroups = mMapFragment.getView();
        if(mapViewGroups != null) {
            mapViewGroups.setVisibility(show ? View.GONE : View.VISIBLE);
            mapViewGroups.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mapViewGroups.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSdk.disconnectRobot();
    }

    public void setMode(MapFragment.Mode mode) {
        mMapFragment.setMode(mode);
        mModeSpinner.setSelection(getModeIndex(mode));
    }

    private int getModeIndex(MapFragment.Mode mode) {
        int index;
        switch (mode) {
            case Navigation:
                index = 0;
                break;
            case Mapping:
                index = 1;
                break;
            case ContinueMapping:
                index = 2;
                break;
            case MappingAdvanceEdit:
                index = 3;
                break;
            case Unknown:
            default:
                index = 4;
                break;

        }
        return index;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_map_mode_menu, menu);
        return true;
    }

    public static final String KEY_MAP_NAME = "key_map_name";
    public static void editLocaMap(Context context, String mapName) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(KEY_MAP_NAME, mapName);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }
}
