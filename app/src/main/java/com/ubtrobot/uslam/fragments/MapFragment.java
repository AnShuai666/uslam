package com.ubtrobot.uslam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.control.MotionControler;
import com.ubtrobot.uslam.control.MotionParser;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.task.StatusUpdateTask;
import com.ubtrobot.uslam.utils.MapUtils;
import com.ubtrobot.uslam.utils.Point;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotStatus;
import com.ubtrobot.uslam.utils.ToastUtils;
import com.ubtrobot.uslam.views.GsMoveControlButtons;
import com.ubtrobot.uslam.views.MapView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private final static String TAG = "MapFragment";
    private MotionControler.Speed mSpeed = MotionControler.Speed.NORMAL;

    Mode mMode = Mode.Unknown;

    private Switch mShowVirtualWallSwitch;
    private Switch mShowVirtualTrackerSwitch;
    private Switch mShowPointCloudSwitch;
    private ViewGroup mToolbox;

    private CompoundButton.OnCheckedChangeListener mOnSwitchChangedListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            switch (compoundButton.getId()) {
                case R.id.show_point_cloud:
                    mMapView.showPointCloud(checked);
                    mMapView.invalidate();
                    break;
                case R.id.show_virtual_tracker:
                    mMapView.showVirtualTracker(checked);
                    mMapView.invalidate();
                    break;
                case R.id.show_virtual_wall:
                    mMapView.showVirtualWall(checked);
                    mMapView.invalidate();
                    break;
                default:
                    break;
            }
        }
    };

    private Spinner mModeSpinner = null;
    private Button mChooseMapButton = null;

    private View mDeleteButton;
    private ArrayList<MapFragmentProxy> mProxyList;

    private void stopMapping() {
        SdkManager.getSdk().requestStopMapping(
                MapUtils.generateMapName(),
                null,
                null,
                result -> {
                    if (result.returnCode == HttpRet.SUCCESS) {

                    } else if (result.returnCode == HttpRet.COMMAND_CAN_NOT_EXECUTE) {
                        ToastUtils.show("command can't execute!");
                    }

                });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopMapping();
        if (mStatusUpdateTask != null) {
            mStatusUpdateTask.cancel();
        }
    }

    void setEnabledChooseMap(boolean enable) {
        if(mChooseMapButton != null) {
            mChooseMapButton.setEnabled(enable);
        }
    }

    private IRemoteRobotSdk mSdk;
    private StatusUpdateTask mStatusUpdateTask;
    private MapView mMapView;

    public Mode getMode() {
        return mMode;
    }

    public void setModeSpinner(Spinner modeSpinner) {
        mModeSpinner = modeSpinner;
    }

    public void setChooseMapButton(Button chooseMapButton) {
        this.mChooseMapButton = chooseMapButton;
    }

    public void setMapName(String name) {
        if(name != null) {
            name = name.substring(name.lastIndexOf("/") + 1, name.length());
            mChooseMapButton.setText(name);
        } else {
            mChooseMapButton.setText(R.string.doing_mapping);
        }
    }

    public void showChooseMapList() {
        if(mChooseMapButton != null) {
            mChooseMapButton.callOnClick();
        }
    }

    public Spinner getModeSpinner() {
        return mModeSpinner;
    }

    public enum Mode {
        Navigation,
        ContinueMapping,
        Mapping,
        MappingAdvanceEdit,
        Unknown
    }

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSdk = SdkManager.getSdk();

        if (mStatusUpdateTask == null) {
            mStatusUpdateTask = new StatusUpdateTask();
            mStatusUpdateTask.setEnable(true, true, true, true,
                    true, true, true, true, true, true);
            mStatusUpdateTask.setRobotStatusListener(status -> {
                updateRobotStatus(status);
            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mShowPointCloudSwitch = rootView.findViewById(R.id.show_point_cloud);
        mShowVirtualTrackerSwitch = rootView.findViewById(R.id.show_virtual_tracker);
        mShowVirtualWallSwitch = rootView.findViewById(R.id.show_virtual_wall);


        mMapView = rootView.findViewById(R.id.map_view);

        GsMoveControlButtons mDirectionPad = rootView.findViewById(R.id.direction_keyboard);
        mDirectionPad.setOnLongPressedListener(new GsMoveControlButtons.OnLongPressedListener() {

            @Override
            public void onLongPressed(int direction) {
                MotionControler.Direction directionCmd = MotionParser.parseKeyDirection(direction);
                mSdk.simpleMove(mSpeed,
                        directionCmd,
                        result -> {

                        });
            }

            @Override
            public void onRelease(int direction) {
                if (mDirectionPad.isMoving()) {
                    mSdk.stopMove(result -> {

                    });
                }
            }
        });

        mToolbox = rootView.findViewById(R.id.toolbox);

        mShowPointCloudSwitch = rootView.findViewById(R.id.show_point_cloud);
        mShowPointCloudSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        mShowVirtualTrackerSwitch = rootView.findViewById(R.id.show_virtual_tracker);
        mShowVirtualTrackerSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        mShowVirtualWallSwitch = rootView.findViewById(R.id.show_virtual_wall);
        mShowVirtualWallSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        mDeleteButton = rootView.findViewById(R.id.delete_virtual_item);
        mDeleteButton.setOnClickListener(v -> {
            if(mMapView.isSetTargetMode()) {
                mMapView.deleteSelectedTarget();
            } else {
                mMapView.deleteSelectedVirtualItem();
            }
        });
        RadioGroup speedPanel = rootView.findViewById(R.id.speed_panel);
        speedPanel.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.speed_low:
                    mSpeed = MotionControler.Speed.LOW;
                    break;
                case R.id.speed_normal:
                    mSpeed = MotionControler.Speed.NORMAL;
                    break;
                case R.id.speed_high:
                    mSpeed = MotionControler.Speed.HIGH;
                    break;
            }
        }));
        mProxyList = new ArrayList<>();
        mProxyList.add(new NavigationProxy());
        mProxyList.add(new MappingProxy());
        mProxyList.add(new ContinueMappingProxy());
        mProxyList.add(new EditMapProxy());
        for(MapFragmentProxy proxy : mProxyList) {
            proxy.setMapFragment(this);
            proxy.onCreateView(rootView);
        }
        return rootView;
    }


    private void updateRobotStatus(RobotStatus status) {
        if (status != null) {
            for(MapFragmentProxy proxy : mProxyList) {
                proxy.onUpdateRobotStatus(status);
            }

            Pose pose = status.currentPose;
            List<Point> lidar = status.lidar;

            mMapView.setRobotPose(pose);
            mMapView.setPointCloud(lidar);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (mStatusUpdateTask == null) {
            mStatusUpdateTask = new StatusUpdateTask();
            mStatusUpdateTask.setEnable(true, true, true, true,
                    true, true, true, true, true, true);
            mStatusUpdateTask.setRobotStatusListener(status -> {
                updateRobotStatus(status);
            });
        }
        mStatusUpdateTask.start(true, 1000);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStatusUpdateTask != null) {
            mStatusUpdateTask.start(true, 1000);
        }
    }

    public void setMode(Mode mode) {
        if(mode == mMode) {
            // forbidden the same mode.
            return;
        }
        Log.e(TAG, "set mode " + mode);
        for (int i = 0; i < mToolbox.getChildCount(); ++i) {
            mToolbox.getChildAt(i).setVisibility(View.GONE);
        }
        for(MapFragmentProxy proxy : mProxyList) {
            if(proxy.shouldSwitchModeTo(mode)){
                return;
            }
        }
        mMode = mode;
        setEnabledChooseMap(true);
        for(MapFragmentProxy proxy : mProxyList) {
            if(proxy.afterSwitchModeTo(mode)){
                return;
            }
        }

        if (mode == Mode.Mapping) {
            mStatusUpdateTask.setEnable(true, true, false, true, true, false, false, true, false, false);
        } else {
            mStatusUpdateTask.setEnable(true, true, false, false, false, true, true, false, false, false);
        }
        if(mModeSpinner != null) {

            switch (mMode) {

                case Navigation:
                    mModeSpinner.setSelection(0);
                    break;
                case ContinueMapping:
                    mModeSpinner.setSelection(2);
                    break;
                case Mapping:
                    mModeSpinner.setSelection(1);
                    break;
                case MappingAdvanceEdit:
                    mModeSpinner.setSelection(3);
                    break;
                case Unknown:
                    break;
            }
        }
    }

    public void setMap(RobotMap map) {
        mMapView.setMap(map);
    }

    public void refreshMap(RobotMap map) {
        mMapView.refreshMap(map);
    }

    public MapView getMapView() {
        return mMapView;
    }

}
