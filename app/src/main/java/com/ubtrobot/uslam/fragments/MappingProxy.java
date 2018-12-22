package com.ubtrobot.uslam.fragments;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotStatus;
import com.ubtrobot.uslam.utils.ToastUtils;
import com.ubtrobot.uslam.widget.InputDialog;

import java.util.List;

public class MappingProxy implements MapFragmentProxy {
    private static final String TAG = "MappingProxy";
    private Button mFinishMappingButton;
    private MapFragment mMapFragment;
    private Switch mShowPointCloudSwitch;
    private RobotMap mCurrentMap = null;

    @Override
    public void onCreateView(View rootView) {
        mFinishMappingButton = rootView.findViewById(R.id.finish_mapping);
        mShowPointCloudSwitch = rootView.findViewById(R.id.show_point_cloud);
        mFinishMappingButton.setOnClickListener(v -> {
            mMapFragment.setMode(MapFragment.Mode.MappingAdvanceEdit);
        });
    }

    @Override
    public void setMapFragment(MapFragment fragment) {
        mMapFragment = fragment;
    }

    @Override
    public boolean shouldSwitchModeTo(MapFragment.Mode mode) {
        if(mMapFragment.getMode() == MapFragment.Mode.Mapping
                && mode != MapFragment.Mode.Mapping) {
            showInputDialog(mode);
            return true;
        }
        if(mMapFragment.getMode() == MapFragment.Mode.ContinueMapping) {
            mCurrentMap = mMapFragment.getMapView().getCurrentMap();
            Log.e(TAG, "should switch mode: " + mCurrentMap  +  " " + mode);
        } else {
            mCurrentMap = null;
        }
        return false;
    }

    @Override
    public boolean afterSwitchModeTo(MapFragment.Mode mode) {
        if (mode == MapFragment.Mode.Mapping) {
            mFinishMappingButton.setVisibility(View.VISIBLE);
            mShowPointCloudSwitch.setVisibility(View.VISIBLE);

            mMapFragment.getModeSpinner().setEnabled(false);
            Log.e(TAG, "after switch mode: " + mCurrentMap +  " " + mode);
            if(mCurrentMap != null) {
                mMapFragment.setMapName(mCurrentMap.mMapName);
            } else {
                if(mMapFragment.getMapView().getCurrentMap() != null
                        && !mMapFragment.getMapView().getCurrentMap().isRealtimeMap()) {
                    mMapFragment.getMapView().setMap(null);
                }
                mMapFragment.setMapName(mMapFragment.getString(R.string.doing_mapping));
            }
            mMapFragment.setEnabledChooseMap(false);
            if(mCurrentMap == null) {
                SdkManager.getSdk().requestStartMapping(result -> {
                    if (!result.isSuccess()) {
                        ToastUtils.show(result.errMsg);
                    }
                });
            } else {
                SdkManager.getSdk().requestUpdateMapping(result -> {
                    if (!result.isSuccess()) {
                        ToastUtils.show(result.errMsg);
                    }
                });
            }
        } else {
            mMapFragment.getModeSpinner().setEnabled(true);
        }
        return false;
    }

    @Override
    public void onUpdateRobotStatus(RobotStatus status) {
        RobotMap map = status.map;
        if (mMapFragment.getMode() == MapFragment.Mode.Mapping
                && map != null) {
            if(mCurrentMap != null) {
                map.setName(mCurrentMap.mMapName);
            }
            mMapFragment.refreshMap(map);
        }
    }

    private void showInputDialog(MapFragment.Mode targetMode) {

        InputDialog.Builder builder = new InputDialog.Builder(mMapFragment.getActivity());

        builder.setTitle(R.string.input_map_name)
                .addListener((type, dialog, name) -> {
                    if (type == InputDialog.Type.START) {
                        if (name != null && name.length() > 0) {
                            List<Line> tracker = null;
                            List<Line> wall = null;
                            if(mCurrentMap != null){
                                tracker = mCurrentMap.tracker;
                                wall = mCurrentMap.wall;
                            }

                            SdkManager.getSdk().requestStopMapping(
                                    name,
                                    wall,
                                    tracker,
                                    result -> {
                                        if (result.returnCode == HttpRet.SUCCESS) {
                                            mMapFragment.getMapView().getCurrentMap().setName(name);
                                            mMapFragment.setMapName(name);
                                            mMapFragment.mMode = MapFragment.Mode.Unknown;
                                            mMapFragment.setMode(targetMode);
                                            dialog.dismiss();
                                        } else if (result.returnCode == HttpRet.COMMAND_CAN_NOT_EXECUTE) {
                                            ToastUtils.show(R.string.toast_bad_name);
                                        }
                                    });
                        } else {
                            ToastUtils.show(R.string.please_enter_map_name);
                        }
                    } else if (type == InputDialog.Type.MIDDLE) {
                        // not save
                        dialog.dismiss();
                        mMapFragment.showChooseMapList();
                        //  TODO: bill.xie
                        if(targetMode != null) {
                            mMapFragment.mMode = MapFragment.Mode.Unknown;
                        }
                    } else {
                        mMapFragment.setMode(MapFragment.Mode.Mapping);
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }
}
