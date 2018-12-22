package com.ubtrobot.uslam.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubtrobot.uslam.MainActivity;
import com.ubtrobot.uslam.MapFile;
import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.data.MapSaver;
import com.ubtrobot.uslam.thread.HandlerThreadFactory;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.RobotMap;

import java.util.List;

/**
 * @author leo
 * @date 2018/12/17
 * @email ao.liu@ubtrobot.com
 */
public class LocalMapListFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private List<MapFile> mMaps;
    private LocalMapListAdapter mAdapter;
    private View mRoot;

    public LocalMapListFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_local_map_list, container, false);
        mRecyclerView = mRoot.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMaps = MapSaver.getLocalMapList();
        mAdapter = new LocalMapListAdapter(getActivity(), mMaps);
        mRecyclerView.setAdapter(mAdapter);
        return mRoot;
    }


    public static LocalMapListFragment newInstance() {
        LocalMapListFragment fragment = new LocalMapListFragment();
        return fragment;
    }

    private static class LocalMapListAdapter extends RecyclerView.Adapter<LocalMapListAdapter.ViewHolder> {

        private Context mContext;
        private List<MapFile> mData;

        public LocalMapListAdapter(Context context, List<MapFile> maps) {
            mContext = context;
            mData = maps;
        }

        @NonNull
        @Override
        public LocalMapListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.item_local_map, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         holder.setData(mData.get(position));
        }

        @Override
        public int getItemCount() {
            if (mData != null) {
                return mData.size();
            }
            return 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            private ImageView mMapThumbnail;
            private TextView mMapName;
            private View mView;

            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
//                mMapThumbnail = itemView.findViewById(R.id.map_thumbnail);
                mMapName = itemView.findViewById(R.id.map_name);

            }

            public void setData(MapFile map) {
//                mMapThumbnail.setImageBitmap(map.mThumbnail);
                mMapName.setText(map.fileName);
                mView.setOnClickListener(v -> {
                    MainActivity.editLocaMap(itemView.getContext(), map.fileName);
                });
            }
        }

        public void notifyDataSetChanged(List<MapFile> data) {
            mData = data;
            notifyDataSetChanged();
        }
    }


}
