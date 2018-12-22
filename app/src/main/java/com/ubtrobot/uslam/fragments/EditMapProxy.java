package com.ubtrobot.uslam.fragments;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.RobotStatus;
import com.ubtrobot.uslam.views.ErasePoint;
import com.ubtrobot.uslam.views.MapView;

public class EditMapProxy implements MapFragmentProxy {

    private ViewGroup mEraseViewGroup;
    private Button mFinishEditButton;
    private Switch mVirtualWallSwitch;
    private Switch mVirtualTrackerSwitch;
    private Switch mActualWallSwitch;
    private ViewGroup mRedoUndoViewGroup;
    private ImageButton mRedoButton;
    private ImageButton mUndoButton;
    private Switch mEraseWhiteToolSwitch;
    private Switch mEraseGrayToolSwitch;
    private Switch mEraseMagicToolSwitch;
    private Switch mShowVirtualWallSwitch;
    private Switch mShowVirtualTrackerSwitch;
    private MapView mMapView;
    private View mDeleteButton;
    private View mControlPanel = null;

    private static final String TAG = "EditMapProxy";

    private CompoundButton.OnCheckedChangeListener mOnSwitchChangedListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            Log.e(TAG, "onCheckedChanged: " + compoundButton.getText() + " "+ checked);
            mShowVirtualTrackerSwitch.setEnabled(true);
            mShowVirtualWallSwitch.setEnabled(true);
            switch (compoundButton.getId()) {
                case R.id.virtual_tracker:
                    if (checked) {
                        mActualWallSwitch.setChecked(false);
                        mVirtualWallSwitch.setChecked(false);
                        resetEraseTool();
                        mShowVirtualTrackerSwitch.setChecked(true);
                        mShowVirtualWallSwitch.setChecked(false);
                        mShowVirtualTrackerSwitch.setEnabled(false);
                    }
                    mMapView.virtualTracker(checked);
                    mMapView.invalidate();
                    Log.e(TAG, "virtual_tracker");
                    break;
                case R.id.virtual_wall:
                    if (checked) {
                        mActualWallSwitch.setChecked(false);
                        mVirtualTrackerSwitch.setChecked(false);
                        mMapView.virtualWall(false);
                        resetEraseTool();
                        mShowVirtualWallSwitch.setChecked(true);
                        mShowVirtualWallSwitch.setEnabled(false);
                    }
                    mMapView.virtualWall(checked);
                    mMapView.invalidate();
                    break;
                case R.id.actual_wall:
                    if (checked) {
                        mVirtualWallSwitch.setChecked(false);
                        mVirtualTrackerSwitch.setChecked(false);
                        mMapView.actualWall(false);
                        resetEraseTool();
                    }
                    mMapView.actualWall(checked);
                    mMapView.invalidate();
                    break;
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

    @Override
    public void onCreateView(View rootView) {
        mEraseViewGroup = rootView.findViewById(R.id.erase_group);
        for (int i = 0; i < mEraseViewGroup.getChildCount(); ++i) {
            Switch button = (Switch) mEraseViewGroup.getChildAt(i);
            button.setOnCheckedChangeListener(mOnEraseChangedListener);
        }
        mFinishEditButton = rootView.findViewById(R.id.finish_edit);
        mFinishEditButton.setOnClickListener(v -> {
            RobotMap currentMap = mMapView.getCurrentMap();
//            SdkManager.getSdk().updateMap();
        });

        mVirtualWallSwitch = rootView.findViewById(R.id.virtual_wall);
        mVirtualWallSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        mVirtualTrackerSwitch = rootView.findViewById(R.id.virtual_tracker);
        mVirtualTrackerSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        mActualWallSwitch = rootView.findViewById(R.id.actual_wall);
        mActualWallSwitch.setOnCheckedChangeListener(mOnSwitchChangedListener);
        Log.e(TAG, "onRootView");

        mRedoButton = rootView.findViewById(R.id.redo_button);
        mRedoButton.setOnClickListener(v -> mMapView.redoErase());
        mRedoButton.setLongClickable(true);
//        mRedoButton.setOnLongClickListener(v -> {
//            mMapView.redoErase();
//            return true;
//        });
        mUndoButton = rootView.findViewById(R.id.undo_button);
        mUndoButton.setOnClickListener(v -> mMapView.undoErase());
//        mUndoButton.setLongClickable(true);
//        mUndoButton.setOnLongClickListener(v -> {
//            mMapView.undoErase();
//            return true;
//        });
        mRedoUndoViewGroup = rootView.findViewById(R.id.view_group_redo_undo);

        mEraseWhiteToolSwitch = rootView.findViewById(R.id.erase_free);
        mEraseGrayToolSwitch = rootView.findViewById(R.id.erase_unknown);
        mEraseMagicToolSwitch = rootView.findViewById(R.id.erase_magic);

        mControlPanel = rootView.findViewById(R.id.control_panel);

        mShowVirtualTrackerSwitch = rootView.findViewById(R.id.show_virtual_tracker);
        mShowVirtualWallSwitch = rootView.findViewById(R.id.show_virtual_wall);

        mDeleteButton = rootView.findViewById(R.id.delete_virtual_item);
        mMapView.setOnVirtualItemSelectedListener(new MapView.OnVirtualItemSelectedListener() {
            @Override
            public void onSelected(MapView.ToolboxMode mode, int index) {
                mDeleteButton.setVisibility(index >= 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onRedoUndStateChanged(boolean canRedo, boolean canUndo) {
                refreshEraseRedoUndoState();
            }
        });
    }

    @Override
    public void setMapFragment(MapFragment fragment) {
        mMapView = fragment.getMapView();
    }

    @Override
    public boolean shouldSwitchModeTo(MapFragment.Mode mode) {
        return false;
    }

    private void resetEraseTool() {
        mEraseGrayToolSwitch.setChecked(false);
        mEraseWhiteToolSwitch.setChecked(false);
        mEraseMagicToolSwitch.setChecked(false);
        mMapView.resetEraseTools();
        mRedoUndoViewGroup.setVisibility(View.GONE);
    }

    @Override
    public boolean afterSwitchModeTo(MapFragment.Mode mode) {
        if(mode == MapFragment.Mode.MappingAdvanceEdit) {
            mFinishEditButton.setVisibility(View.VISIBLE);
            mVirtualWallSwitch.setVisibility(View.VISIBLE);
            mShowVirtualWallSwitch.setVisibility(View.VISIBLE);
            mShowVirtualTrackerSwitch.setVisibility(View.VISIBLE);
            mVirtualTrackerSwitch.setVisibility(View.VISIBLE);
            mActualWallSwitch.setVisibility(View.VISIBLE);
            mEraseViewGroup.setVisibility(View.VISIBLE);
            mControlPanel.setVisibility(View.GONE);
            mMapView.showRobot(false);
            mMapView.showPointCloud(false);
        } else {
            mVirtualTrackerSwitch.setChecked(false);
            mVirtualWallSwitch.setChecked(false);
            mActualWallSwitch.setChecked(false);
            mEraseWhiteToolSwitch.setChecked(false);
            mEraseGrayToolSwitch.setChecked(false);
            mEraseMagicToolSwitch.setChecked(false);
            mControlPanel.setVisibility(View.VISIBLE);
            mMapView.showRobot(true);
            mMapView.showPointCloud(true);
        }
        return false;
    }

    @Override
    public void onUpdateRobotStatus(RobotStatus status) {

    }

    private void refreshEraseRedoUndoState() {
        mRedoButton.setEnabled(mMapView.canRedoErase());
        mUndoButton.setEnabled(mMapView.canUndoErase());
    }


    private CompoundButton.OnCheckedChangeListener mOnEraseChangedListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton button, boolean checked) {
            Log.e(TAG, "mOnEraseChangedListener: " + button.getText() + " "+ checked);
            if (!checked) {
                return;
            }
            mVirtualTrackerSwitch.setChecked(false);
            mVirtualWallSwitch.setChecked(false);
            mActualWallSwitch.setChecked(false);
            mDeleteButton.setVisibility(View.GONE);
            for (int i = 0; i < mEraseViewGroup.getChildCount(); ++i) {
                if (button != mEraseViewGroup.getChildAt(i)) {
                    Switch otherButton = (Switch) mEraseViewGroup.getChildAt(i);
                    otherButton.setChecked(false);
                }
            }
            mRedoUndoViewGroup.setVisibility(View.VISIBLE);
            refreshEraseRedoUndoState();
            switch (button.getId()) {
                case R.id.erase_free:
                    mMapView.startErase(true, ErasePoint.EraseType.FreeArea);
                    break;
                case R.id.erase_unknown:
                    mMapView.startErase(true, ErasePoint.EraseType.UnknownArea);
                    break;
                case R.id.erase_magic:
                    mMapView.startErase(true, ErasePoint.EraseType.MagicBrush);
                    break;
                default:
            }
        }
    };
}
