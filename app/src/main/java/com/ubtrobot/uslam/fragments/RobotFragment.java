package com.ubtrobot.uslam.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.model.MediaFile;
import com.ubtrobot.uslam.AutoFitGridLayoutManager;
import com.ubtrobot.uslam.MainActivity;
import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.sdk.RobotSdkImp;
import com.ubtrobot.uslam.sdk.SdkManager;
import com.ubtrobot.uslam.utils.Robot;
import com.ubtrobot.uslam.utils.Settings;
import com.ubtrobot.uslam.utils.ViewUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RobotFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final int FILE_REQUEST_CODE = 1001;
    private static final String TAG = "RobotFragment";
    // TODO: Customize parameters
    private OnListFragmentInteractionListener mListener = (viewType, item) -> {
        if (viewType == RobotRecyclerViewAdapter.ViewType.AddRobot.ordinal()) {
            selectAddRobot();
        } else if (viewType == RobotRecyclerViewAdapter.ViewType.OnLine.ordinal()
                || viewType == RobotRecyclerViewAdapter.ViewType.OffLine.ordinal()) {
            startMainActivity(item.baseURL);
        } else {
            Log.w(TAG, "else warning." + viewType);
        }
    };
    private View mContentView;
    private View mGridView;
    private View mProgressView;
    private RobotRecyclerViewAdapter mAdapter;
    private IRemoteRobotSdk mSdk;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RobotFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RobotFragment newInstance(int columnCount) {
        RobotFragment fragment = new RobotFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSdk = SdkManager.getSdk();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_robot_list, container, false);
        mGridView = mContentView.findViewById(R.id.robot_list);
        mProgressView = mContentView.findViewById(R.id.choose_robot_progress);
        // Set the adapter
        if (mGridView instanceof RecyclerView) {
            Context context = mContentView.getContext();
            RecyclerView recyclerView = (RecyclerView) mGridView;
            recyclerView.setLayoutManager(new AutoFitGridLayoutManager(getActivity(), ViewUtils.dp2px(getActivity(), 220)));
            mAdapter = new RobotRecyclerViewAdapter(((RobotSdkImp) mSdk).getRobots(), mListener);
            recyclerView.setAdapter(mAdapter);
            SdkManager.getSdk().requestRobotList((robots) -> {
                Log.e(TAG, "robots length: " + robots.size());
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    getActivity().runOnUiThread(() -> {
                        mAdapter.notifyDataSetChanged(robots);
                    });
                }
            });
        } else {
            Log.w(TAG, "else error." );
        }
        return mContentView;
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mGridView.setVisibility(show ? View.GONE : View.VISIBLE);
        mGridView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mGridView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILE_REQUEST_CODE:
                ArrayList<MediaFile> files = data.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES);
                if (files != null
                        && files.size() > 0) {
                    Log.d("RobotFragment", files.get(0).toString());
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void selectAddRobot() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.input_ip_address);
        // Set up the input
        final EditText input = new EditText(this.getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String ip = input.getText().toString();
            if(ipCheck(ip)) {
                startMainActivity(Settings.getInstance().getBaseUrlByIp(ip));
                dialog.cancel();
            }
            input.selectAll();
            input.setError(getString(R.string.error_invalid_ip_address));
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();

    }

    private void startMainActivity(String baseUrl) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_BASE_URL, baseUrl);
        startActivity(intent);
    }

    public boolean ipCheck(String text) {
        if (text != null && text.length() > 0) {
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            Pattern ipAndPortPattern = Pattern.compile(regex);
            Matcher matcher = ipAndPortPattern.matcher(text);
            matcher.reset();
            return matcher.matches();
        }
        return false;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(int viewType, Robot item);
    }
}
