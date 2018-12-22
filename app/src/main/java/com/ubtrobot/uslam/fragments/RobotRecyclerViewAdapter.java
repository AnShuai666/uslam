package com.ubtrobot.uslam.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.fragments.RobotFragment.OnListFragmentInteractionListener;
import com.ubtrobot.uslam.utils.AppUtils;
import com.ubtrobot.uslam.utils.OnLineStatus;
import com.ubtrobot.uslam.utils.Robot;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Robot} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class RobotRecyclerViewAdapter extends RecyclerView.Adapter<RobotRecyclerViewAdapter.ViewHolder> {

    enum ViewType {
        OnLine,
        AddRobot,
        OffLine,
        Busy
    }

    private List<Robot> mValues;
    private final OnListFragmentInteractionListener mListener;

    public RobotRecyclerViewAdapter(List<Robot> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == ViewType.AddRobot.ordinal()) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_robot, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_robot, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.setData(mValues.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        Robot robot = mValues.get(position);
        if (robot.onLineStatus == OnLineStatus.Offline) {
            return ViewType.OffLine.ordinal();
        } else if (robot.onLineStatus == OnLineStatus.Online) {
            return ViewType.OnLine.ordinal();
        } else if (robot.onLineStatus == OnLineStatus.Busy) {
            return ViewType.Busy.ordinal();
        } else {
            return ViewType.AddRobot.ordinal();
        }
    }

    public void notifyDataSetChanged(List<Robot> robots) {
        mValues = robots;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mRobotName;
        public final ImageView mRobotStatus;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mRobotName = (TextView) view.findViewById(R.id.robot_name);
            mRobotStatus = (ImageView) view.findViewById(R.id.robot_status);

        }

        public void setData(Robot robot) {
            if (getItemViewType() != ViewType.AddRobot.ordinal()) {
                mRobotName.setText(robot.getRobotName());

                if (robot.onLineStatus == OnLineStatus.Busy) {
                    mRobotStatus.setImageResource(R.mipmap.ic_busy);
                } else if (robot.onLineStatus == OnLineStatus.Online) {
                    mRobotStatus.setImageResource(R.mipmap.ic_online);
                } else {
                    mRobotName.setTextColor(AppUtils.getColor(R.color.color_text_robot_offline));
                    mRobotStatus.setImageResource(0);
                }
            }

            mView.setOnClickListener(v -> {
                if (null != mListener) {

                    mListener.onListFragmentInteraction(getItemViewType(), robot);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mRobotName.getText() + "'";
        }
    }
}
