package com.ubtrobot.uslam.fragments;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.utils.MapUtils;
import com.ubtrobot.uslam.utils.RobotMap;

import java.util.Iterator;
import java.util.List;

public class MapListViewAdapter extends RecyclerSwipeAdapter<MapListViewAdapter.ViewHolder> {

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    public enum OperationType {
        NewMap,
        EditMap,
        DeleteMap,
        ExportMap,
        SelectMap
    }


    private List<RobotMap> mMaps;
    private OnMapListSelectListener mListener;
    private final String TAG = "MapListViewAdapter";

    public MapListViewAdapter(List<RobotMap> items, MapListViewAdapter.OnMapListSelectListener listener) {
        mMaps = items;
        mListener = listener;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == 0) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_new_map, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_map, parent, false);
        }
        return new ViewHolder(view, viewType == 0);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if(getItemViewType(position) == 0) {
            holder.mItem = null;
            holder.mView.setOnClickListener(v -> {
                if (null != mListener) {
                    mListener.onSelect(null, OperationType.NewMap);
                }
            });
        } else {
            holder.mItem = mMaps.get(position - 1);
            RobotMap map = mMaps.get(position - 1);
            holder.mMapNameView.setText(MapUtils.getMapInfo(map));
            holder.mMapThumbnail.setImageBitmap(map.mThumbnail);
            if (mListener != null) {
                holder.mView.findViewById(R.id.delete_map).setOnClickListener( view -> {
                    mListener.onSelect(map.mMapName, OperationType.DeleteMap);
                });
                holder.mView.findViewById(R.id.edit_map).setOnClickListener( view -> {
                    mListener.onSelect(map.mMapName, OperationType.EditMap);
                });
                holder.mView.findViewById(R.id.export_map).setOnClickListener( view -> {
                    mListener.onSelect(map.mMapName, OperationType.ExportMap);
                });
                holder.mContentView.setOnClickListener(view -> {
                    mListener.onSelect(map.mMapName, OperationType.SelectMap);
                });
            }

            mItemManger.bindView(holder.mView, position);
            holder.mSwipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
                @Override
                public void onStartOpen(SwipeLayout layout) {
                    mItemManger.closeAllExcept(layout);
                }

                @Override
                public void onOpen(SwipeLayout layout) {

                }

                @Override
                public void onStartClose(SwipeLayout layout) {

                }

                @Override
                public void onClose(SwipeLayout layout) {

                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

                }

                @Override
                public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

                }
            });
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return mMaps.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        View mContentView;
        TextView mMapNameView;
        ImageView mMapThumbnail;
        final boolean mIsNewMap;
        RobotMap mItem;
        SwipeLayout mSwipeLayout;

        ViewHolder(View view, boolean isNewMap) {
            super(view);
            mView = view;
            mIsNewMap = isNewMap;

            if(!isNewMap) {
                mContentView = view.findViewById(R.id.map_info);
                mMapNameView = view.findViewById(R.id.map_name);
                mMapThumbnail = view.findViewById(R.id.new_map);
                mSwipeLayout = (SwipeLayout) mView.findViewById(R.id.swipe_layout);
                mSwipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
                mSwipeLayout.addDrag(SwipeLayout.DragEdge.Right, mSwipeLayout.findViewById(R.id.bottom_wrapper));
            }

        }
    }

    public void removeItem(String fullMapName) {
        Iterator<RobotMap> iterator = mMaps.iterator();
        if (iterator.hasNext()) {
            RobotMap next = iterator.next();
            if (next.mMapName.equals(fullMapName)) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    public interface OnMapListSelectListener {
        public void onSelect(String mapName, OperationType type);
    }
}
