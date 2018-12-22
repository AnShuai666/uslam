package com.ubtrobot.uslam.fragments;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.RobotStatus;
import com.ubtrobot.uslam.utils.ToastUtils;
import com.ubtrobot.uslam.views.MapView;
import com.ubtrobot.uslam.views.OnRelocalizationFinishedListener;
import com.ubtrobot.uslam.widget.TipDialog;

import java.util.ArrayList;
import java.util.List;

public class NavigationProxy implements MapFragmentProxy {

    protected Switch mShowVirtualWallSwitch;
    protected Switch mShowVirtualTrackerSwitch;
    protected Switch mShowPointCloudSwitch;

    protected Switch mRelocalizationSwitch;
    protected Switch mSetTargetSwitch;
    protected ToggleButton mStartCancelNavigationToggleButton;
    protected View mDeleteButton;


    protected MapView mMapView;
    protected List<Pose> mTargets = new ArrayList<>();
    protected int mCurrentTargetIndex = -1;

    private RobotStatus.SubStatus mRelocaliztionLastStatus = null;
    private RobotStatus.SubStatus mNavigationLastStatus = null;

    private static final String TAG = "NavigationProxy";

    private OnRelocalizationFinishedListener mOnRelocalizationStart = new OnRelocalizationFinishedListener() {
        @Override
        public void onResult(Pose pose) {
            SdkManager.getSdk().requestRelocalization(mMapView.getRobotPose(), result -> {
                Log.e(TAG, "relocalization result: " + result.toString());
            });
        }
    };

    private CompoundButton.OnCheckedChangeListener mSwitchListener = (buttonView, isChecked) -> {
        switch (buttonView.getId()) {
            case R.id.relocalization:
                mMapView.relocalization(isChecked, mOnRelocalizationStart);
                mSetTargetSwitch.setEnabled(!isChecked);
                break;
            case R.id.target:
                onSetTargetSwitchButtonChecked(isChecked);
                break;
            default:
                break;
        }
    };
    MapFragment mMapFragment;
    private IRemoteRobotSdk mSdk;
    private TipDialog mProgressDialog;

    @Override
    public void onCreateView(View rootView) {
        mShowPointCloudSwitch = rootView.findViewById(R.id.show_point_cloud);
        mShowVirtualTrackerSwitch = rootView.findViewById(R.id.show_virtual_tracker);
        mShowVirtualWallSwitch = rootView.findViewById(R.id.show_virtual_wall);
        mStartCancelNavigationToggleButton = rootView.findViewById(R.id.start_cancel_navigation);
        mDeleteButton = rootView.findViewById(R.id.delete_virtual_item);

        mRelocalizationSwitch = rootView.findViewById(R.id.relocalization);
        mRelocalizationSwitch.setOnCheckedChangeListener(mSwitchListener);
        mSetTargetSwitch = rootView.findViewById(R.id.target);
        mSetTargetSwitch.setOnCheckedChangeListener(mSwitchListener);

        mStartCancelNavigationToggleButton.setOnClickListener((v) -> {
            boolean checked = ((ToggleButton)v).isChecked();
            if(checked) {
                mSdk.cancelNavigation(result -> {
                    ToastUtils.show(R.string.toast_canceled_navigation);
                });
            } else {
                mTargets = mMapView.getTargetList();
                mCurrentTargetIndex = -1;
                if(mTargets != null && mTargets.size() > 0) {
                    navigationToNextTarget();
                    mSetTargetSwitch.setEnabled(false);
                } else {
                    ToastUtils.show(R.string.toast_select_targets_frist);
                }
            }
        });

        mProgressDialog = new TipDialog.Builder(mMapFragment.getActivity())
                .setIconType(TipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(mMapFragment.getString(R.string.doing_relocalization))
                .create(true);
        mMapView = mMapFragment.getMapView();
        mSdk = SdkManager.getSdk();
    }

    @Override
    public void setMapFragment(MapFragment fragment) {
        mMapFragment = fragment;
    }

    @Override
    public boolean shouldSwitchModeTo(MapFragment.Mode mode) {
        return false;
    }

    @Override
    public boolean afterSwitchModeTo(MapFragment.Mode mode) {
        if(mode == MapFragment.Mode.Navigation) {
            mShowVirtualWallSwitch.setVisibility(View.VISIBLE);
            mShowVirtualTrackerSwitch.setVisibility(View.VISIBLE);
            mShowPointCloudSwitch.setVisibility(View.VISIBLE);
            mRelocalizationSwitch.setVisibility(View.VISIBLE);
            mSetTargetSwitch.setVisibility(View.VISIBLE);
            mSetTargetSwitch.setEnabled(true);
            mMapView.startSettingTarget(mSetTargetSwitch.isChecked());
        } else {
            mSetTargetSwitch.setChecked(false);
            mRelocalizationSwitch.setChecked(false);
        }
        return false;
    }

    @Override
    public void onUpdateRobotStatus(RobotStatus status) {
        MapView.RelocalizationMode mode = mMapFragment.getMapView().getRelocalizationMode();
        if(mode == MapView.RelocalizationMode.choosePoint) {
            return;
        }

        // doing relocaliztion.
        if(status.isRecalization == RobotStatus.SubStatus.E_ACTION_RUNNING) {
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.setCancelable(true);
                mProgressDialog.setOnCancelListener(v ->  mSdk.cancelRelocalization(result -> {}));
                mProgressDialog.show();
                mRelocalizationSwitch.setEnabled(false);
                onSetTargetSwitchButtonEnable(false);
                mMapView.startSettingTarget(false);
            }
            return;
        } else {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                onSetTargetSwitchButtonEnable(true);
                onSetTargetSwitchButtonChecked(mSetTargetSwitch.isChecked());
                mRelocalizationSwitch.setEnabled(true);
                mRelocalizationSwitch.setChecked(false);
            }
            if(status.isRecalization == RobotStatus.SubStatus.E_ACTION_ERROR &&
                    canToastRelocalization(status)) {
                ToastUtils.show(R.string.toast_relocalization_fail);
            } else if(status.isRecalization == RobotStatus.SubStatus.E_ACTION_FINISHED && canToastRelocalization(status)) {
                ToastUtils.show(R.string.toast_relocalization_done);
            }
        }

        if(status.isNavigation == RobotStatus.SubStatus.E_ACTION_RUNNING) {
            onSetTargetSwitchButtonEnable(false);
            mMapView.startSettingTarget(false);
            mMapView.setShowTargets(true);
            mRelocalizationSwitch.setEnabled(false);
            mStartCancelNavigationToggleButton.setChecked(false);
        } else {

            if(status.isNavigation == RobotStatus.SubStatus.E_ACTION_ERROR &&
                    canToastNavigation(status)) {
                ToastUtils.show(R.string.toast_navigation_fail);
                onSetTargetSwitchButtonEnable(true);
            }
            if(status.isNavigation == RobotStatus.SubStatus.E_ACTION_FINISHED) {
                navigationToNextTarget();
            }
            if(status.isNavigation == RobotStatus.SubStatus.E_ACTION_FINISHED
                    && mTargets.size() == mCurrentTargetIndex - 1
                    && canToastNavigation(status)) {
                ToastUtils.show(R.string.toast_finished_navigation);
            }
            if(status.isNavigation == RobotStatus.SubStatus.E_ACTION_IDLE
                    && canToastNavigation(status)) {
                ToastUtils.show(R.string.toast_canceled_navigation);
            }
            onSetTargetSwitchButtonEnable(true);
            onSetTargetSwitchButtonChecked(mSetTargetSwitch.isChecked());
        }
        mNavigationLastStatus = status.isNavigation;
        mRelocaliztionLastStatus = status.isRecalization;
    }

    private boolean canToastRelocalization(RobotStatus status) {
        return status.isRecalization != mRelocaliztionLastStatus
                && mRelocaliztionLastStatus != null;
    }

    private boolean canToastNavigation(RobotStatus status) {
        return (mNavigationLastStatus != null
                && status.isNavigation != mNavigationLastStatus );
    }

    private void onSetTargetSwitchButtonChecked(boolean checked) {
        mMapView.startSettingTarget(checked);
        if (checked) {
            mDeleteButton.setVisibility(View.VISIBLE);
            mStartCancelNavigationToggleButton.setVisibility(View.VISIBLE);
            mRelocalizationSwitch.setEnabled(false);
        } else {
            mTargets.clear();
            if(mMapFragment.getMapView().isSetTargetMode()) {
                mDeleteButton.setVisibility(View.GONE);
            }
            mRelocalizationSwitch.setEnabled(true);
            mStartCancelNavigationToggleButton.setVisibility(View.GONE);
        }
    }

    private void onSetTargetSwitchButtonEnable(boolean enable) {
        mMapView.setShowTargets(mSetTargetSwitch.isChecked());
        mDeleteButton.setEnabled(enable);
        mStartCancelNavigationToggleButton.setChecked(enable);
        mSetTargetSwitch.setEnabled(enable);
    }

    private boolean navigationToNextTarget() {
        if(mTargets.size() > 0) {
            List<Pose> target = new ArrayList<>();
            mCurrentTargetIndex++;
            if(mCurrentTargetIndex >= 0 && mCurrentTargetIndex < mTargets.size()) {
                target.add(mTargets.get(mCurrentTargetIndex));
                SdkManager.getSdk().setTarget(target, result -> {
                    String toastStr = mMapFragment.getString(R.string.toast_start_navigation, target.get(0).poseName);
                    mMapFragment.getActivity().runOnUiThread(() -> ToastUtils.show(toastStr));
                    mMapView.setTargetSelected(mCurrentTargetIndex);
                });
            } else {
                return false;
            }
            return true;
        }
        return false;
    }
}
