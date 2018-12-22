package com.ubtrobot.uslam.fragments;

import android.view.View;
import android.widget.Button;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.ToastUtils;

public class ContinueMappingProxy extends NavigationProxy {
    private Button mStartMappingButton;

    @Override
    public void onCreateView(View rootView) {
        super.onCreateView(rootView);
        mStartMappingButton = rootView.findViewById(R.id.start_mapping);
        mStartMappingButton.setOnClickListener(v -> SdkManager.getSdk().requestUpdateMapping(result -> {
            if (result.returnCode == HttpRet.SUCCESS) {
                mMapFragment.setMode(MapFragment.Mode.Mapping);

            } else if (result.returnCode == HttpRet.COMMAND_CAN_NOT_EXECUTE) {
                ToastUtils.show("robot is mapping, please try after finish!");
            }
        }));
    }

    @Override
    public void setMapFragment(MapFragment fragment) {
        super.setMapFragment(fragment);
    }

    @Override
    public boolean shouldSwitchModeTo(MapFragment.Mode mode) {
        return false;
    }

    @Override
    public boolean afterSwitchModeTo(MapFragment.Mode mode) {
        if(mode == MapFragment.Mode.ContinueMapping) {
            super.afterSwitchModeTo(MapFragment.Mode.Navigation);
            mStartMappingButton.setVisibility(View.VISIBLE);
            mSetTargetSwitch.setVisibility(View.GONE);
            mMapFragment.setEnabledChooseMap(false);
        } else {
            mStartMappingButton.setVisibility(View.GONE);
        }
        return false;
    }
}
