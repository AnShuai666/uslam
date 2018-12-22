package com.ubtrobot.uslam.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.daimajia.swipe.util.Attributes;
import com.ubtrobot.uslam.MainActivity;
import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.data.MapSaver;
import com.ubtrobot.uslam.net.HttpRet;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.MapUtils;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.utils.ToastUtils;
import com.ubtrobot.uslam.widget.TipDialog;

import java.util.ArrayList;

/**
 * @author leo
 * @date 2018/11/27
 * @email ao.liu@ubtrobot.com
 */
public class MapListDialogFragment extends DialogFragment {

    private static final String TAG = "MapListDialogFragment";

    private ArrayList<RobotMap> mMaps;
    private MapListViewAdapter mAdapter;
    private TipDialog mProgress;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.Dialog_FullScreen);
        dialog.setContentView(R.layout.dialog_map_list);
        RecyclerView mapList = dialog.findViewById(R.id.map_list);
        TextView emptyView = dialog.findViewById(R.id.empty_view);
        TextView cancel = dialog.findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        mapList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MapListViewAdapter(mMaps, (fullMapName, type) -> {

            if (type == MapListViewAdapter.OperationType.SelectMap) {
                FragmentActivity activity = getActivity();
                if (activity instanceof MainActivity) {
                    String mapName = MapUtils.removeMapNameSuffix(fullMapName);
                    ((MainActivity) activity).fetchMapByName(mapName);
                    dialog.dismiss();
                }
            } else if (type == MapListViewAdapter.OperationType.DeleteMap) {
                SdkManager.getSdk().deleteMap(fullMapName, result -> {
                    if (result.returnCode == HttpRet.SUCCESS) {
                        // remove item
                        mAdapter.removeItem(fullMapName);
                    }
                });
            } else if (type == MapListViewAdapter.OperationType.ExportMap) {
                mProgress.show();
                String mapName = MapUtils.removeMapNameSuffix(fullMapName);
                MapSaver.ecportMap(mapName, new IRemoteRobotSdk.OnExportMapListener() {
                    @Override
                    public void onSuccess(String map) {
                        MapSaver.saveMap(fullMapName, map);
                        ToastUtils.show("导出成功");
                        mProgress.dismiss();
                    }

                    @Override
                    public void onFailure() {
                        ToastUtils.show("导出失败");
                        mProgress.dismiss();
                    }
                });
                dialog.dismiss();

            } else if (type == MapListViewAdapter.OperationType.EditMap) {
                FragmentActivity activity = getActivity();
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setMode(MapFragment.Mode.MappingAdvanceEdit);
                    String mapName = MapUtils.removeMapNameSuffix(fullMapName);
                    ((MainActivity) activity).fetchMapByName(mapName);
                    dialog.dismiss();
                }
            } else if (type == MapListViewAdapter.OperationType.NewMap) {
                FragmentActivity activity = getActivity();
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setMode(MapFragment.Mode.Mapping);
                    dialog.dismiss();
                }
            }
        });
        mAdapter.setMode(Attributes.Mode.Single);
        mapList.setAdapter(mAdapter);
        if (mMaps == null
                || mMaps.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        if (mProgress == null) {
            mProgress = new TipDialog.Builder(getActivity())
                    .setIconType(TipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("导出中...")
                    .create();
        }

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMaps = (ArrayList<RobotMap>) getArguments().get("key");

    }

    public static MapListDialogFragment newInstance(ArrayList<RobotMap> maps) {
        MapListDialogFragment fragment = new MapListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("key", maps);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        getDialog().getWindow().getAttributes().height = metrics.heightPixels;
        getDialog().getWindow().setLayout(metrics.widthPixels, getDialog().getWindow().getAttributes().height);
    }

}
