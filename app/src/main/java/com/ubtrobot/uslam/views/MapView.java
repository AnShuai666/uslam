package com.ubtrobot.uslam.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.Point;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.RobotMap;
import com.ubtrobot.uslam.views.ErasePoint.EraseType;

import java.util.ArrayList;
import java.util.List;

import static com.ubtrobot.uslam.views.MapView.ToolboxMode.ActualWallMode;
import static com.ubtrobot.uslam.views.MapView.ToolboxMode.EraseMode;
import static com.ubtrobot.uslam.views.MapView.ToolboxMode.Nothing;
import static com.ubtrobot.uslam.views.MapView.ToolboxMode.TrackerMode;
import static com.ubtrobot.uslam.views.MapView.ToolboxMode.WallMode;

public class MapView extends View {

    private Point mPoint1st = null;
    private Point mPoint2nd = null;
    private int mSecondFingerId = -1;
    private boolean mIsShaking = false;
    private boolean mShowEraseLayer = true;

    public boolean isRelocalization() {
        return mIsRelocalization;
    }

    public void setTargetSelected(int index) {
        mSelectedTargetIndex = index;
    }

    public void setShowTargets(boolean show) {
        mShowTargetList = show;
    }

    public ToolboxState getToolboxState() {
        return mToolboxState;
    }

    public enum RelocalizationMode {
        choosePoint,
        relocalizating,
        finished,
        unknonwn,
        notInRelocalizationMode
    };
    private static final String TAG = "MapView";
    private static final int ROBOT_LENGTH = 40;
    private static final boolean SHOW_FAKE_POINT_CLOUD = true;
    private static final int ROBOT_TAIL_HYPOTENUSE_LENGTH = 20;
    private static final double ROBOT_TAIL_RAD = Math.PI * 4 / 3;
    private static final int ROBOT_POSE_HEADER_COLOR_1 = 0xff001970;
    private static final int ROBOT_POSE_HEADER_COLOR_2 = 0xff666ad1;

    private static final int TARGET_POSE_HEADER_COLOR_1 = 0xff33ab9f;
    private static final int TARGET_POSE_HEADER_COLOR_2 = 0xff009688;

    private static final int CURRENT_TARGET_POSE_HEADER_COLOR_1 = 0xffffc107;
    private static final int CURRENT_TARGET_POSE_HEADER_COLOR_2 = 0xffb28704;

    private static final int POINT_CLOUD_COLOR = Color.RED;
    private static final int POINT_CLOUD_SIZE = 2;
    private static final boolean SHOW_MAP_BOUND = false;
    private static final int LINE_SELECT_DISTANCE_IN_PIXEL = 15;
    private static final int POINT_SELECT_DISTANCE_IN_PIXEL = 30;
    private static final int CURRENT_ROBOT_POSE = 0;
    private static final int CURRENT_TARGET_POSE = 1;
    private static final int TARGET_POSE = 2;
    private RobotMap mCurrentMap;
    private Matrix mBitmapMatrix = new Matrix();
    private boolean mFirstDraw = false;
    private static final int AXIS_LENGTH = 50;
    private static final int AXIS_WIDTH = 3;
    private Pose mRobotPose = new Pose(1, 0, (float) (Math.PI / 6));
    private List<Point> mPointCloud = null;
    private boolean mIsRelocalization = false;
    private RelocalizationMode mRelocalizationMode = RelocalizationMode.unknonwn;
    private Animation mAnimShake;
    private boolean mShowPointCloud = true;
    private boolean mShowRobot = true;
    private boolean mShowAxis = true;
    private boolean mShowVirtualTracker = true;
    private boolean mShowVirtualWall = true;
    private float mAxisThetaX;
    private OnVirtualItemSelectedListener mListener;
    private boolean mIsAddingNewLine = false;

    private int mFirstFingerId;
    private boolean mShowScale = true;

    private Point mScaleDrawPoint;
    private Point mFingerPointInCanvas = null;
    private boolean mIsSetTargets = false;
    private boolean mShowTargetList = true;
    private int mSelectedTargetIndex = -1;

    private List<Pose> mTargetList = new ArrayList<>();
    private Pose mCurrentTargetPose = null;
    private OnRelocalizationFinishedListener mRelocalizationFinishedListener;

    public boolean redoErase() {
        mToolboxState.mEraseRedoUndoIndex += 1;
        if (mToolboxState.mEraseRedoUndoIndex > mToolboxState.mErasePoints.size()) {
            mToolboxState.mEraseRedoUndoIndex = mToolboxState.mErasePoints.size();
            mFingerPointInCanvas = null;
        } else if (mToolboxState.mEraseRedoUndoIndex >= 0
                && mToolboxState.mEraseRedoUndoIndex < mToolboxState.mErasePoints.size()) {
            ErasePoint point = mToolboxState.mErasePoints.get(mToolboxState.mEraseRedoUndoIndex);
            Point pointInCell = mCurrentMap.getPointInCell(point);
            float[] pts = new float[]{pointInCell.x, pointInCell.y};
            mBitmapMatrix.mapPoints(pts);
            mFingerPointInCanvas = new Point(pts[0], pts[1]);
        }
        if (mListener != null)
            mListener.onRedoUndStateChanged(canRedoErase(), canUndoErase());
        invalidate();
        return canRedoErase();
    }

    public boolean undoErase() {
        mToolboxState.mEraseRedoUndoIndex -= 1;
        if (mToolboxState.mEraseRedoUndoIndex < 0) {
            mToolboxState.mEraseRedoUndoIndex = -1;
            mFingerPointInCanvas = null;
        } else if (mToolboxState.mEraseRedoUndoIndex >= 0
                && mToolboxState.mEraseRedoUndoIndex < mToolboxState.mErasePoints.size()) {
            ErasePoint point = mToolboxState.mErasePoints.get(mToolboxState.mEraseRedoUndoIndex);
            Point pointInCell = mCurrentMap.getPointInCell(point);
            float[] pts = new float[]{pointInCell.x, pointInCell.y};
            mBitmapMatrix.mapPoints(pts);
            mFingerPointInCanvas = new Point(pts[0], pts[1]);
        }
        if (mListener != null)
            mListener.onRedoUndStateChanged(canRedoErase(), canUndoErase());
        invalidate();
        return canUndoErase();
    }

    public boolean canUndoErase() {
        return mToolboxState.mEraseRedoUndoIndex >= 0 && mToolboxState.mToolboxMode == EraseMode;
    }

    public boolean canRedoErase() {
        return mToolboxState.mEraseRedoUndoIndex < mToolboxState.mErasePoints.size()
                && mToolboxState.mToolboxMode == EraseMode;
    }

    public void resetEraseTools() {
        Log.e(TAG, "resetEraseTools");
        mToolboxState.mEraseType = EraseType.Nothing;
        mFingerPointInCanvas = null;
    }

    public boolean isSetTargetMode() {
        return mIsSetTargets;
    }


    public enum ToolboxMode {
        WallMode,
        TrackerMode,
        ActualWallMode,
        EraseMode,
        Nothing,
    }

    public enum SelectedPoint {
        StartPoint,
        EndPoint,
        Nothing,
    }

    public class ToolboxState {

        public float mEraseSizeInCanvas = 20f;
        EraseType mEraseType = EraseType.Nothing;
        private ToolboxMode mToolboxMode = ToolboxMode.Nothing;
        int mSelectedLine = -1;
        SelectedPoint mSelectedPoint = SelectedPoint.Nothing;
        List<Line> mActualWallLines = new ArrayList<>();
        List<ErasePoint> mErasePoints = new ArrayList<>();
        int mEraseRedoUndoIndex = 0;

        void resetToolboxStatus() {
            Log.e(TAG, "resetToolboxStatus");
            mToolboxMode = ToolboxMode.Nothing;
            mSelectedLine = -1;
            mSelectedPoint = SelectedPoint.Nothing;
        }

        void setToolboxMode(ToolboxMode mode) {
            mToolboxMode = mode;
        }

        void removeSelected() {
            switch (mToolboxMode) {
                case TrackerMode:
                    if (mSelectedLine >= 0 && mSelectedLine < mCurrentMap.tracker.size())
                        mCurrentMap.tracker.remove(mSelectedLine);
                    break;
                case WallMode:
                    if (mSelectedLine >= 0 && mSelectedLine < mCurrentMap.wall.size())
                        mCurrentMap.wall.remove(mSelectedLine);
                    break;
                case ActualWallMode:
                    if (mSelectedLine >= 0 && mSelectedLine < mActualWallLines.size())
                        mActualWallLines.remove(mSelectedLine);
                    break;
                default:
                    break;
            }
            mSelectedPoint = SelectedPoint.Nothing;
            mSelectedLine = -1;
        }

        void notifySelected(int index) {
            if (mListener != null) {
                switch (mToolboxMode) {
                    case TrackerMode:
                        mListener.onSelected(TrackerMode, -1);
                        break;
                    case WallMode:
                        mListener.onSelected(TrackerMode, -1);
                        break;
                    default:
                        mListener.onSelected(ToolboxMode.Nothing, -1);
                        break;
                }
            }
        }

        public int getLineNumber(ToolboxMode mode) {
            if (mode == mToolboxMode) {
                return mSelectedLine;
            }
            return -1;
        }

        public int getLineNumber() {
            return getLineNumber(mToolboxMode);
        }

        public Line getLine() {
            List<Line> lines = getLines();
            return getLine(lines);
        }

        public Line getLine(List<Line> lines) {
            if (lines != null && mSelectedLine >= 0 && mSelectedLine < lines.size())
                return lines.get(mSelectedLine);
            return null;
        }

        private List<Line> getLines() {
            if (mToolboxMode == TrackerMode) {
                return mCurrentMap.tracker;
            }
            if (mToolboxMode == WallMode) {
                return mCurrentMap.wall;
            }
            if (mToolboxMode == ActualWallMode) {
                return mActualWallLines;
            }
            return null;
        }

        public boolean clearActualWall() {
            return mCurrentMap.clearVirtualItem(mActualWallLines);
        }

        public void addErasePoint(Point pointInCanvas) {
            Point pointInCell = convertCanvasToCell(pointInCanvas);
            Matrix invert = new Matrix();
            mBitmapMatrix.invert(invert);
            float eraseSizeInCell = invert.mapRadius(mEraseSizeInCanvas);

            if(pointInCell.x - eraseSizeInCell < 0
                    || pointInCell.y - eraseSizeInCell < 0
                    || pointInCell.x + eraseSizeInCell > mCurrentMap.getWidth()
                    || pointInCell.y + eraseSizeInCell > mCurrentMap.getHeight()) {
                return;
            }

            Point point = canvasToMeter(pointInCanvas);
            // add the point to the tail.
            if (mErasePoints.size() > mEraseRedoUndoIndex) {
                mErasePoints.subList(mEraseRedoUndoIndex, mErasePoints.size()).clear();
            }
            if (mEraseType == EraseType.Nothing || mEraseType == EraseType.MagicBrush) return;
            float eraseSizeInMeter = eraseSizeInCell * mCurrentMap.getResolution();
            mErasePoints.add(new ErasePoint(point, mEraseType, eraseSizeInMeter));
            mEraseRedoUndoIndex = mErasePoints.size();
            if (mListener != null) {
                mListener.onRedoUndStateChanged(false, true);
            }
        }
    }

    private ToolboxState mToolboxState = new ToolboxState();

    public void deleteSelectedVirtualItem() {
        mToolboxState.removeSelected();
        mToolboxState.notifySelected(-1);
        invalidate();
    }

    public void deleteSelectedTarget() {
        if(mTargetList.size() > 0) {
            if (mSelectedTargetIndex == -1 || mSelectedTargetIndex >= mTargetList.size()) {
                mTargetList.remove(mTargetList.size() - 1);
            } else {
                mTargetList.remove(mSelectedTargetIndex);
            }
            mCurrentTargetPose = null;
        }
        invalidate();
    }

    public interface OnVirtualItemSelectedListener {
        public void onSelected(ToolboxMode mode, int index);

        public void onRedoUndStateChanged(boolean canRedo, boolean canUndo);
    }


    public MapView(Context context) {
        super(context);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMap(RobotMap map) {
        mCurrentMap = map;
        mBitmapMatrix.reset();
        mFirstDraw = true;
        postInvalidate();
    }

    public void refreshMap(RobotMap map) {
        if(mCurrentMap == null || !mCurrentMap.sameSize(map)) {
            setMap(map);
        } else {
            mCurrentMap = map;
            postInvalidate();
        }
    }

    public RobotMap getCurrentMap() {
        return mCurrentMap;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAnimShake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
        mAnimShake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsShaking = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsShaking = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCurrentMap != null && mCurrentMap.mBitmap != null) {
            if (mFirstDraw) {
                // make the bitmap in the center of the canvas.
                mFirstDraw = false;
                RectF mapRect = new RectF(0, 0, mCurrentMap.mWidth, mCurrentMap.mHeight);
                RectF canvasRect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
                mBitmapMatrix.setRectToRect(mapRect, canvasRect, Matrix.ScaleToFit.CENTER);
            }
            if (mToolboxState.mToolboxMode == EraseMode && mToolboxState.mEraseType == EraseType.MagicBrush) {
                canvas.drawBitmap(mCurrentMap.getMagicBitmap(), mBitmapMatrix, null);
            } else {
                canvas.drawBitmap(mCurrentMap.mBitmap, mBitmapMatrix, null);
            }

            if (SHOW_MAP_BOUND) {
                float[] pts = getMapBoundByPoints(mBitmapMatrix);
                Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStrokeWidth(15);
                canvas.drawPoints(pts, paint);
            }

            drawRobotLayer(canvas, mShowAxis, mShowRobot, mShowVirtualTracker, mShowVirtualWall);
            if (mShowEraseLayer) {
                drawEraseLayer(canvas);
            }
            if (mShowTargetList) {
                drawTargets(canvas);
            }
            if (mShowPointCloud) {
                drawPointCloud(canvas);
            }
            if (mShowScale) {
                drawScale(canvas);
            }
        }
    }

    private void drawTargets(Canvas canvas) {
        Point lastPoint = null;
        Pose selected = null;
        List<Pose> targetList = new ArrayList<>();
        targetList.addAll(mTargetList);
        if (mCurrentTargetPose != null) {
            targetList.add(mCurrentTargetPose);
        }
        if(targetList.size() > 0) {
            if(mSelectedTargetIndex < 0 || mSelectedTargetIndex >= mTargetList.size()) {
                selected = targetList.get(targetList.size() - 1);
             } else {
                selected = targetList.get(mSelectedTargetIndex);
            }
        }
        for(Pose pose : targetList) {
            Point point = mCurrentMap.getPointInCell(pose.point);
            float[] pts = new float[]{point.x, point.y};
            mBitmapMatrix.mapPoints(pts);
            if (lastPoint != null) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(TARGET_POSE_HEADER_COLOR_1);
                paint.setStyle(Paint.Style.STROKE);
                paint.setPathEffect(new DashPathEffect(new float[] {50, 50}, 0));
                canvas.drawLine(lastPoint.x, lastPoint.y, pts[0], pts[1], paint);
            }
            if (pose == selected) {
                drawRobotPose(new Pose(pts[0], pts[1], mAxisThetaX - pose.theta), CURRENT_TARGET_POSE, canvas);
            } else {
                drawRobotPose(new Pose(pts[0], pts[1], mAxisThetaX - pose.theta), TARGET_POSE, canvas);
            }
            if(pose.poseName != null) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                int textSize = 24;
                paint.setTextSize(textSize);
                canvas.drawText(pose.poseName, pts[0] + 20, pts[1] - 10, paint);
                lastPoint = new Point(pts[0], pts[1]);
            }
        }
    }

    private void drawEraseLayer(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(mToolboxState.mEraseSizeInCanvas);
        float[] pts = new float[2];
        List<ErasePoint> erasePoints = mToolboxState.mErasePoints;
        float pointSize = 0;
        for (int index = 0; index < erasePoints.size() && index <= mToolboxState.mEraseRedoUndoIndex; index++) {
            ErasePoint erasePoint = erasePoints.get(index);
            Point point = mCurrentMap.getPointInCell(erasePoint);
            pointSize = erasePoint.mSize / mCurrentMap.getResolution();
            if (erasePoint.mType == EraseType.FreeArea)
                paint.setColor(mCurrentMap.getFreeColor());
            else if (erasePoint.mType == EraseType.UnknownArea) {
                paint.setColor(mCurrentMap.getUnknownColor());
            }
            pts[0] = point.x;
            pts[1] = point.y;
            mBitmapMatrix.mapPoints(pts);
            pointSize = mBitmapMatrix.mapRadius(pointSize);
            canvas.drawCircle(pts[0], pts[1], pointSize, paint);
        }
        if (mFingerPointInCanvas != null) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(2);
            canvas.drawCircle(mFingerPointInCanvas.x, mFingerPointInCanvas.y, pointSize, paint);
        }
    }

    private void drawScale(Canvas canvas) {
        if (mScaleDrawPoint == null) {
            mScaleDrawPoint = new Point(canvas.getWidth() - 300, canvas.getHeight() - 100);
        }
        int textSize = 30;
        int scaleLength = 100;
        float[] pts = getMapBoundByPoints(mBitmapMatrix);
        Rect drawingRect = new Rect();
        getDrawingRect(drawingRect);
        double widthInPixel = Math.sqrt((pts[0] - pts[4]) * (pts[0] - pts[4])
                + (pts[1] - pts[5]) * (pts[1] - pts[5]));

        double widthInMeter = mCurrentMap.mWidth * mCurrentMap.getResolution();
        double meterPerPixel = widthInMeter / widthInPixel;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(textSize);
        paint.setStrokeWidth(1);
        String text = getReadableScale(meterPerPixel * scaleLength);
        Rect textBound = new Rect();
        paint.getTextBounds(text, 0, text.length(), textBound);
        canvas.drawText(text, mScaleDrawPoint.x - textBound.width() / 2, mScaleDrawPoint.y, paint);
        float lineY = mScaleDrawPoint.y + textBound.height() - 10;
        float[] line = new float[]{mScaleDrawPoint.x - scaleLength / 2, lineY - 10,
                mScaleDrawPoint.x - scaleLength / 2, lineY,
                mScaleDrawPoint.x - scaleLength / 2, lineY,
                mScaleDrawPoint.x + scaleLength / 2, lineY,
                mScaleDrawPoint.x + scaleLength / 2, lineY,
                mScaleDrawPoint.x + scaleLength / 2, lineY - 10};
        canvas.drawLines(line, paint);
    }

    /**
     * get a human readable text which is 'distance' meter.
     *
     * @param distance unit is meter
     * @return text can be read by human
     */
    private String getReadableScale(double distance) {
        String text;
        if (distance > 1) {
            text = String.format("%.1f" + getResources().getString(R.string.meter), distance);
        } else if (distance > 0.1) {
            text = String.format("%.1f" + getResources().getString(R.string.centimeter), distance * 10);
        } else {
            text = String.format("%.1f" + getResources().getString(R.string.millimeter), distance * 10);
        }
        return text;
    }

    private float[] getMapBoundByPoints(Matrix matrix) {
        float[] pts = new float[]{0, 0, 0, mCurrentMap.mHeight,
                mCurrentMap.mWidth, 0, mCurrentMap.mWidth, mCurrentMap.mHeight};
        matrix.mapPoints(pts);
        return pts;
    }

    private void drawRobotLayer(Canvas canvas, boolean drawAxis, boolean drawRobot,
                                boolean drawVirtualTracker, boolean drawVirtualWall) {
        Point point = mCurrentMap.getOriginInCell();
        Point pointX = mCurrentMap.getPointInCell(new Point(1, 0));
        Point pointRobot = mCurrentMap.getPointInCell(mRobotPose.getPoint());
        float[] originVec = new float[]{point.x, point.y, pointX.x, pointX.y, pointRobot.x, pointRobot.y};
        mBitmapMatrix.mapPoints(originVec);
        mAxisThetaX = (float) Math.atan2(originVec[3] - originVec[1], originVec[2] - originVec[0]);

        if (drawAxis) {
            // draw x-axis
            Paint paint = new Paint();
            paint.setFlags(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(AXIS_WIDTH);
            canvas.drawLine(originVec[0], originVec[1],
                    (float) (originVec[0] + AXIS_LENGTH * Math.cos(mAxisThetaX)),
                    (float) (originVec[1] + AXIS_LENGTH * Math.sin(mAxisThetaX)), paint);
            // draw y-axis
            paint.setColor(Color.GREEN);
            float thetaY = (float) (mAxisThetaX + Math.PI / 2);
            canvas.drawLine(originVec[0], originVec[1], (float) (originVec[0] + AXIS_LENGTH * Math.cos(thetaY)),
                    (float) (originVec[1] + AXIS_LENGTH * Math.sin(thetaY)), paint);
        }
        if (drawVirtualTracker) {
            List<Line> lines = mCurrentMap.tracker;
            int color = Color.GREEN;
            drawLines(canvas, lines, color, mToolboxState.getLineNumber(TrackerMode), mToolboxState.mSelectedPoint);
        }
        if (drawVirtualWall) {
            List<Line> lines = mCurrentMap.wall;
            int color = Color.RED;
            drawLines(canvas, lines, color, mToolboxState.getLineNumber(WallMode), mToolboxState.mSelectedPoint);
        }
        if (mToolboxState.mToolboxMode == ActualWallMode) {
            int color = Color.BLACK;
            drawLines(canvas, mToolboxState.getLines(), color, mToolboxState.mSelectedLine, mToolboxState.mSelectedPoint);
        } else if (mToolboxState.mActualWallLines.size() > 0) {
            int color = Color.BLACK;
            drawLines(canvas, mToolboxState.mActualWallLines, color, -1, SelectedPoint.Nothing);
        }
        if (drawRobot) {
            Pose robotPoseInCanvas = new Pose(originVec[4], originVec[5], (float) (mAxisThetaX - mRobotPose.theta));
            drawRobotPose(robotPoseInCanvas, CURRENT_ROBOT_POSE, canvas);
        }
    }

    private void drawLines(Canvas canvas,
                           @Nullable List<Line> lines,
                           int color,
                           int selectedLineNumber,
                           SelectedPoint selectedPoint) {
        if (lines == null) return;
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        for (int i = 0; i < lines.size(); i++) {
            Line line = mCurrentMap.getLineInCell(lines.get(i));
            float[] pts = line.getPts();
            mBitmapMatrix.mapPoints(pts);
            paint.setColor(color);
            if (i == selectedLineNumber) {
                paint.setStrokeWidth(10);
//                paint.setMaskFilter(new EmbossMaskFilter(new float[] {0.0f, 2.0f, 0.5f}, 0.8f, 15f, 2f));
                canvas.drawLines(pts, paint);
                int mainColor = color;
                // draw start point;
                if (selectedPoint == SelectedPoint.StartPoint) {
                    mainColor = Color.BLUE;
                }
                paint.setColor(mainColor);
                canvas.drawCircle(pts[0], pts[1], 15, paint);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(pts[0], pts[1], 10, paint);
                paint.setColor(mainColor);
                canvas.drawCircle(pts[0], pts[1], 5, paint);

                // draw end point;
                if (selectedPoint == SelectedPoint.EndPoint) {
                    mainColor = Color.BLUE;
                } else {
                    mainColor = color;
                }
                paint.setColor(mainColor);
                canvas.drawCircle(pts[2], pts[3], 15, paint);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(pts[2], pts[3], 10, paint);
                paint.setColor(mainColor);
                canvas.drawCircle(pts[2], pts[3], 5, paint);
            } else {
                paint.setStrokeWidth(4);
                canvas.drawLines(pts, paint);
            }
        }
    }

    private void drawPointCloud(Canvas canvas) {
        if (mPointCloud == null && SHOW_FAKE_POINT_CLOUD) {
            mPointCloud = new ArrayList<>();
            float length = 6;
            for (double rad = -3 * Math.PI / 4; rad < 3 * Math.PI / 4; rad += 1 * Math.PI / 180) {
                Point point = new Point((float) (length * Math.cos(rad)), (float) (length * Math.sin(rad)));
                mPointCloud.add(point);
            }
            mPointCloud.add(new Point());
        }
        if (mPointCloud != null) {

            Matrix matrix = new Matrix();
            Point pointRobot = mCurrentMap.getPointInCell(mRobotPose.getPoint());
            matrix.postTranslate(pointRobot.x, pointRobot.y);
            float[] ptsRobot = new float[]{pointRobot.x, pointRobot.y};
            matrix.mapPoints(ptsRobot);
            matrix.postRotate(-mRobotPose.getThetaInDegree(), pointRobot.x, pointRobot.y);
            matrix.postConcat(mBitmapMatrix);
            float[] pts = new float[mPointCloud.size() * 2];
            for (int i = 0; i < mPointCloud.size(); ++i) {
                Point pt = mCurrentMap.getPointInCellWithoutOrigin(mPointCloud.get(i));
                pts[i * 2] = pt.x;
                pts[i * 2 + 1] = -pt.y;
            }
            matrix.mapPoints(pts);
            Paint paint = new Paint();
            paint.setColor(POINT_CLOUD_COLOR);
            paint.setStrokeWidth(POINT_CLOUD_SIZE);
            canvas.drawPoints(pts, paint);
        }
    }

    protected void drawRobotPose(Pose robotPoseInCanvas, int style, Canvas canvas) {
        // draw robot pose
        Paint paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        Path path = new Path();
        // move to robot origin.
        path.moveTo(robotPoseInCanvas.getX(), robotPoseInCanvas.getY());
        // line to the the head of the robot logo.
        Point head = new Point((float) (robotPoseInCanvas.getX() + ROBOT_LENGTH * Math.cos(robotPoseInCanvas.getTheta())),
                (float) (robotPoseInCanvas.getY() + ROBOT_LENGTH * Math.sin(robotPoseInCanvas.getTheta())));
        // line to the tail of the robot log.
        Point tailLeft = new Point(
                (float) (robotPoseInCanvas.getX() + ROBOT_TAIL_HYPOTENUSE_LENGTH * Math.cos(robotPoseInCanvas.getTheta() + ROBOT_TAIL_RAD)),
                (float) (robotPoseInCanvas.getY() + ROBOT_TAIL_HYPOTENUSE_LENGTH * Math.sin(robotPoseInCanvas.getTheta() + ROBOT_TAIL_RAD)));
        Point tailRight = new Point(
                (float) (robotPoseInCanvas.getX() + ROBOT_TAIL_HYPOTENUSE_LENGTH * Math.cos(robotPoseInCanvas.getTheta() - ROBOT_TAIL_RAD)),
                (float) (robotPoseInCanvas.getY() + ROBOT_TAIL_HYPOTENUSE_LENGTH * Math.sin(robotPoseInCanvas.getTheta() - ROBOT_TAIL_RAD)));
        path.lineTo(tailLeft.x, tailLeft.y);
        path.lineTo(head.x, head.y);
        path.lineTo(tailRight.x, tailRight.y);
        path.close();
        if(style == CURRENT_ROBOT_POSE) {
            paint.setShader(new LinearGradient(head.x, head.y, robotPoseInCanvas.getX(), robotPoseInCanvas.getY(),
                    ROBOT_POSE_HEADER_COLOR_1, ROBOT_POSE_HEADER_COLOR_2, Shader.TileMode.MIRROR));
        } else if(style == CURRENT_TARGET_POSE) {
            paint.setShader(new LinearGradient(head.x, head.y, robotPoseInCanvas.getX(), robotPoseInCanvas.getY(),
                    CURRENT_TARGET_POSE_HEADER_COLOR_1, CURRENT_TARGET_POSE_HEADER_COLOR_2, Shader.TileMode.MIRROR));
        } else if(style == TARGET_POSE) {
            paint.setShader(new LinearGradient(head.x, head.y, robotPoseInCanvas.getX(), robotPoseInCanvas.getY(),
                    TARGET_POSE_HEADER_COLOR_1, TARGET_POSE_HEADER_COLOR_2, Shader.TileMode.MIRROR));
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mCurrentMap == null) {
            return super.onTouchEvent(event);
        }

        if(event.getPointerCount() == 1) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {
                    mFirstFingerId = event.getPointerId(0);
                    Point point = new Point(event.getX(), event.getY());
                    onOneFingerDown(point);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if (event.getPointerId(0) == mFirstFingerId) {
                        Point point = new Point(event.getX(), event.getY());
                        if (mPoint1st != null) {
                            onOneFingerMove(point, mPoint1st.simpleNear(point, POINT_SELECT_DISTANCE_IN_PIXEL));
                        }
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if (event.getPointerId(0) == mFirstFingerId) {
                        Point point = new Point(event.getX(), event.getY());
                        if (mPoint1st != null
                                && !mPoint1st.simpleNear(point, POINT_SELECT_DISTANCE_IN_PIXEL)) {
                            onOneFingerUp(point);
                        }
                        mFirstFingerId = -1;
                    }
                    break;
                }
                default:
                    break;
            }
        } else if (event.getPointerCount() == 2) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mFirstFingerId = event.getPointerId(0);
                    mPoint1st = new Point(event.getX(), event.getY());
                    mSecondFingerId = event.getPointerId(1);
                    mPoint2nd = new Point(event.getX(1),
                            event.getY(1));
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if(mSecondFingerId == -1) {
                        mPoint2nd = new Point(event.getX(1),
                                event.getY(1));
                        mSecondFingerId = event.getPointerId(1);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float pt2x = event.getX(1);
                    float pt2y = event.getY(1);
                    float x = event.getX();
                    float y = event.getY();
                    if(mSecondFingerId != event.getPointerId(1)) {
                        mSecondFingerId = event.getPointerId(1);
                        mPoint2nd = new Point(event.getX(1),
                                event.getY(1));
                        break;
                    }
                    if(mFirstFingerId != event.getPointerId(0)) {
                        mFirstFingerId = event.getPointerId(0);
                        mPoint1st = new Point(event.getX(), event.getY());
                        break;
                    }
                    if(mPoint1st != null && mPoint2nd != null) {
                        float[] pts = new float[]{mPoint1st.x, mPoint1st.y,
                                mPoint2nd.x, mPoint2nd.y};
                        Matrix invertMatrix = new Matrix();
                        mBitmapMatrix.invert(invertMatrix);
                        invertMatrix.mapPoints(pts);
                        Matrix matrix = new Matrix();

                        matrix.setPolyToPoly(pts, 0,
                                new float[]{x, y, pt2x, pt2y}, 0, 2);
                        acceptMatrix(matrix);

                    } else {
                        mPoint1st = new Point();
                        mPoint2nd = new Point();
                    }
                    mPoint2nd.set(pt2x, pt2y);
                    mPoint1st.set(x, y);
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mPoint2nd = null;
                    mSecondFingerId = -1;
                    break;
                case MotionEvent.ACTION_UP:
                    mPoint1st = null;
                    mFirstFingerId = -1;
                    break;
            }
        }
//
//        switch (event.getActionMasked()) {
//            case MotionEvent.ACTION_DOWN:
//                mPt1X = event.getX();
//                mPt1Y = event.getY();
//                mFirstFingerId = event.getPointerId(0);
//                if (event.getPointerCount() == 1
//                        && (mIsRelocalization
//                        || mIsSetTargets
//                        || mToolboxState.mToolboxMode != Nothing)) {
//                    Matrix invert = new Matrix();
//                    mBitmapMatrix.invert(invert);
//                    float[] pts = new float[]{mPt1X, mPt1Y};
//                    invert.mapPoints(pts);
//                    Log.e(TAG, String.format("x: %.2f, y: %.2f", mPt1X, mPt1Y));
//                    if(getSelectedTarget(mPt1X, mPt1Y)) {
//                        Log.d(TAG, "getSelectedTarget");
//                        mSelectEnable = false;
//                        break;
//                    }
//                    Log.e(TAG, String.format("x: %.2f, y: %.2f", mPt1X, mPt1Y));
//                    // pts is the position in cells now.
//                    if (0 < pts[0] && pts[0] < mCurrentMap.mWidth
//                            && 0 < pts[1] && pts[1] < mCurrentMap.mHeight
//                            && mCurrentMap.isFreeColor((int)pts[0], (int)pts[1])) {
//                        mSelectEnable = true;
//                    } else {
//                        mSelectEnable = false;
//                        startAnimation(mAnimShake);
//                    }
//                    if (mToolboxState.mToolboxMode == ToolboxMode.EraseMode) {
//                        if (!mSelectEnable) break;
//                        break;
//                    }
//                    if (mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
//                            || mToolboxState.mToolboxMode == ToolboxMode.WallMode
//                            || mToolboxState.mToolboxMode == ToolboxMode.ActualWallMode) {
//                        if (!mSelectEnable) break;
//                        List<Line> lines = mToolboxState.getLines();
//                        Line line = mToolboxState.getLine(lines);
//                        if (line != null) {
//                            mToolboxState.mSelectedPoint = selectPoint(line, mPt1X, mPt1Y);
//                        }
//                        if (mToolboxState.mSelectedPoint == SelectedPoint.Nothing) {
//                            mToolboxState.mSelectedLine = selectLine(lines, mPt1X, mPt1Y);
//                        }
//                        if (mToolboxState.mSelectedLine < 0) {
//                            mIsAddingNewLine = true;
//                        }
//                        if (mListener != null) {
//                            mListener.onSelected(mToolboxState.mToolboxMode, mToolboxState.mSelectedLine);
//                        }
//                        break;
//                    }
//                    break;
//                }
//                if (event.getPointerCount() == 2) {
//                    mPt2X = event.getX(1);
//                    mPt2Y = event.getY(1);
//                    mIsFirstActionAfterScaling = true;
//                } else {
//                    mIsFirstActionAfterScaling = false;
//                }
////                mIsAddingNewLine = false;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                float x = event.getX();
//                float y = event.getY();
//                if (mIsRelocalization
//                        && mFirstFingerId == event.getPointerId(0)) {
//                    if (!mSelectEnable) break;
//                    if(new Point(mPt1X, mPt1Y).simpleNear(new Point(x, y), POINT_SELECT_DISTANCE_IN_PIXEL)) {
//                        mTooNear = true;
//                        break;
//                    }
//                    mTooNear = false;
//                    float[] pts = new float[]{mPt1X, mPt1Y, x, y};
//                    canvasToMeter(pts);
//                    float theta = (float) (-Math.atan2((mPt1Y - y), (mPt1X - x)) + mAxisThetaX + Math.PI);
//                    mRobotPose.set(pts[0], pts[1], theta);
//                    break;
//                } else if (mIsSetTargets
//                        && mFirstFingerId == event.getPointerId(0)) {
//                    if (!mSelectEnable) break;
//                    if(new Point(mPt1X, mPt1Y).simpleNear(new Point(x, y), POINT_SELECT_DISTANCE_IN_PIXEL)) {
//                        mTooNear = true;
//                        break;
//                    }
//                    mTooNear = false;
//                    float[] pts = new float[]{mPt1X, mPt1Y, x, y};
//                    canvasToMeter(pts);
//                    float theta = (float) (-Math.atan2((mPt1Y - y), (mPt1X - x)) + mAxisThetaX + Math.PI);
//                    if(mCurrentTargetPose == null) {
//                        mCurrentTargetPose = new Pose(pts[0], pts[1], theta);
//                    } else {
//                        mCurrentTargetPose.set(pts[0], pts[1], theta);
//                    }
//                    mSelectedTargetIndex = -1;
//                    break;
//                } else if ((mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
//                        || mToolboxState.mToolboxMode == ToolboxMode.WallMode
//                        || mToolboxState.mToolboxMode == ToolboxMode.ActualWallMode)
//                        && event.getPointerCount() == 1) {
//                    if (!mSelectEnable) break;
//                    if (mToolboxState.mSelectedPoint != SelectedPoint.Nothing) {
//                        float[] pts = new float[]{x, y};
//                        Matrix invert = new Matrix();
//                        mBitmapMatrix.invert(invert);
//                        invert.mapPoints(pts);
//                        float cellX = pts[0];
//                        float cellY = pts[1];
//                        if (cellX < 0 || cellX > mCurrentMap.mWidth || cellY < 0 || cellY > mCurrentMap.mHeight) {
//                            // move to the outside of map.
//                            break;
//                        }
//                        Line line = getSelectedLine();
//                        if (line != null) {
//                            Point pointInCanvas = new Point(x, y);
//                            if (mToolboxState.mSelectedPoint == SelectedPoint.StartPoint) {
//                                magnetic(pointInCanvas);
//                                Point ptInMeter = canvasToMeter(pointInCanvas);
//                                line.getStartPoint().set(ptInMeter);
//                            }
//                            if (mToolboxState.mSelectedPoint == SelectedPoint.EndPoint) {
//                                magnetic(pointInCanvas);
//                                Point ptInMeter = canvasToMeter(pointInCanvas);
//                                line.getEndPoint().set(ptInMeter);
//                            }
//                        }
//                    }
//                    // add a new line
//                    if (mToolboxState.mSelectedLine < 0
//                            && !new Point(mPt1X, mPt1Y).simpleNear(new Point(x, y), 30f)
//                            && mIsAddingNewLine
//                            && event.getPointerId(0) == mFirstFingerId) {
//                        List<Line> lines = mToolboxState.getLines();
//                        Point pointInMeter = canvasToMeter(new Point(mPt1X, mPt1Y));
//                        Line newLine = new Line(pointInMeter, pointInMeter.copy());
//                        lines.add(newLine);
//                        mToolboxState.mSelectedPoint = SelectedPoint.EndPoint;
//                        mToolboxState.mSelectedLine = lines.size() - 1;
//                        mIsAddingNewLine = false;
//                        if (mListener != null) {
//                            mListener.onSelected(mToolboxState.mToolboxMode, mToolboxState.mSelectedLine);
//                        }
//                    }
//                    break;
//                } else if (event.getPointerCount() == 1 && mToolboxState.mToolboxMode == EraseMode) {
//                    Point fingerPoint = new Point(x, y);
//                    if (!new Point(mPt1X, mPt1Y).simpleNear(fingerPoint, mToolboxState.mEraseSizeInCanvas + 2)) {
//                        mToolboxState.addErasePoint(canvasToMeter(new Point(x, y)));
//                        mFingerPointInCanvas = fingerPoint;
//                    }
//                    break;
//                }
//
//                if (event.getPointerCount() == 1) {
//                    if (mIsFirstActionAfterScaling) {
//                        mIsFirstActionAfterScaling = false;
//                    } else {
//                        Matrix matrix = new Matrix(mBitmapMatrix);
//                        matrix.postTranslate(x - mPt1X, y - mPt1Y);
//                        acceptMatrix(matrix);
//                    }
//                } else if (event.getPointerCount() == 2) {
//                    float pt2x = event.getX(1);
//                    float pt2y = event.getY(1);
//                    float[] pts = new float[]{mPt1X, mPt1Y, mPt2X, mPt2Y};
//                    Matrix invertMatrix = new Matrix();
//                    mBitmapMatrix.invert(invertMatrix);
//                    invertMatrix.mapPoints(pts);
//                    Matrix matrix = new Matrix();
//                    matrix.setPolyToPoly(pts, 0,
//                            new float[]{x, y, pt2x, pt2y}, 0, 2);
//                    acceptMatrix(matrix);
//                    mPt2X = pt2x;
//                    mPt2Y = pt2y;
//                    mIsFirstActionAfterScaling = true;
//                }
//                mPt1X = x;
//                mPt1Y = y;
//                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                if (event.getPointerCount() == 2) {
//                    mPt2X = event.getX(1);
//                    mPt2Y = event.getY(1);
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
//                        || mToolboxState.mToolboxMode == ToolboxMode.WallMode) {
//                    if (mCurrentMap.clearVirtualItem()
//                            && mToolboxState.clearActualWall()
//                            && mListener != null) {
//                        mToolboxState.notifySelected(-1);
//                    }
//                    mIsAddingNewLine = false;
//                }
//                if (mIsSetTargets
//                        && mSelectEnable
//                        && !mTooNear
//                        && mFirstFingerId == event.getPointerId(0)) {
//                    if(mCurrentTargetPose != null)
//                        mCurrentTargetPose.generateName();
//                    mTargetList.add(mCurrentTargetPose);
//                    mCurrentTargetPose = null;
//                }
//                if(mIsRelocalization) {
//                    if(mRelocalizationFinishedListener != null)
//                        mRelocalizationFinishedListener.onResult(mRobotPose);
//                    mRelocalizationMode = RelocalizationMode.relocalizating;
//                }
//
//                mFingerPointInCanvas = null;
//                break;
//            case MotionEvent.ACTION_POINTER_UP:
//                break;
//        }
        invalidate();
        return true;
    }

    private void onOneFingerDown(Point point) {
        if(!shouldSelectSomething(point)) {
            mPoint1st = point;
        } else {
            Log.e(TAG, "nothing happen at one finger down.");
            mPoint1st = point;
            // disable move and up action.
        }
    }

    private Point convertCanvasToCell(Point point) {
        Matrix invert = new Matrix();
        mBitmapMatrix.invert(invert);
        float[] pts = new float[]{point.x, point.y};
        invert.mapPoints(pts);
        return new Point(pts[0], pts[1]);
    }

    /**
     *
     * @param point point in cell.
     * @return
     */
    private boolean isOneFingerMoveOutOfBound(Point point) {
        if (mIsRelocalization || mIsSetTargets) {
            return !(0 < point.x && point.x < mCurrentMap.mWidth
                    && 0 < point.y && point.y < mCurrentMap.mHeight
                    && mCurrentMap.isFreeColor((int)point.x, (int)point.y));
        } else if (mToolboxState.mToolboxMode == EraseMode
                || mToolboxState.mToolboxMode == WallMode
                || mToolboxState.mToolboxMode == TrackerMode
                || (mToolboxState.mToolboxMode == ActualWallMode
                    && mToolboxState.mSelectedPoint != SelectedPoint.Nothing)){
            return !(0 < point.x && point.x < mCurrentMap.mWidth
                    && 0 < point.y && point.y < mCurrentMap.mHeight);
        }
        return false;
    }

    private boolean shouldSelectSomething(Point point) {
        if(mIsSetTargets) {
            return getSelectedTarget(point.x, point.y);
        } else if (mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
                || mToolboxState.mToolboxMode == ToolboxMode.WallMode
                || mToolboxState.mToolboxMode == ToolboxMode.ActualWallMode) {
            mIsAddingNewLine = false;
            List<Line> lines = mToolboxState.getLines();
            Line line = mToolboxState.getLine(lines);
            if (line != null) {
                mToolboxState.mSelectedPoint = selectPoint(line, point.x, point.y);
            }
            if (mToolboxState.mSelectedPoint == SelectedPoint.Nothing) {
                mToolboxState.mSelectedLine = selectLine(lines,  point.x, point.y);
            }
            if (mListener != null) {
                mListener.onSelected(mToolboxState.mToolboxMode,
                        mToolboxState.mSelectedLine);
            }
            if (mToolboxState.mSelectedLine < 0) {
                mIsAddingNewLine = true;
            } else {
                return true;
            }
        }
        return false;
    }

    private Pose getFingerPose(Point p1, Point p2) {
        float[] pts = new float[]{p1.x, p1.y, p2.x, p2.y};
        canvasToMeter(pts);
        float theta = (float) (-Math.atan2((p1.y - p2.y), (p1.x - p2.x))
                + mAxisThetaX + Math.PI);
        return new Pose(pts[0], pts[1], theta);
    }

    private void onOneFingerMove(Point point, boolean isNear) {
        Point pointInCell = convertCanvasToCell(mPoint1st);
        if(!isNear && isOneFingerMoveOutOfBound(pointInCell)) {
            if(!mIsShaking)
                startAnimation(mAnimShake);
            mFirstFingerId = -1;
            return;
        }
        if (mIsRelocalization ) {
            if(isNear) return;
            Pose fingerPose = getFingerPose(mPoint1st, point);
            mRobotPose.set(fingerPose.getX(), fingerPose.getY(), fingerPose.getTheta());
        } else if (mIsSetTargets) {
            if(isNear) return;
            Pose fingerPose = getFingerPose(mPoint1st, point);
            if(mCurrentTargetPose == null) {
                mCurrentTargetPose = fingerPose;
            } else {
                mCurrentTargetPose.set(fingerPose.getX(), fingerPose.getY(), fingerPose.getTheta());
            }
            mSelectedTargetIndex = -1;
        }else if (mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
                || mToolboxState.mToolboxMode == ToolboxMode.WallMode
                || mToolboxState.mToolboxMode == ToolboxMode.ActualWallMode) {
            if(isNear) return;
            pointInCell = convertCanvasToCell(point);
            if (mToolboxState.mSelectedPoint != SelectedPoint.Nothing) {
                if (pointInCell.x < 0 || pointInCell.x > mCurrentMap.mWidth
                        || pointInCell.y < 0 || pointInCell.y> mCurrentMap.mHeight) {
                    // move to the outside of map.
                    return;
                }
                Line line = getSelectedLine();
                if (line != null) {
                    if (mToolboxState.mSelectedPoint == SelectedPoint.StartPoint) {
                        magnetic(point);
                        Point ptInMeter = canvasToMeter(point);
                        line.getStartPoint().set(ptInMeter);
                    }
                    if (mToolboxState.mSelectedPoint == SelectedPoint.EndPoint) {
                        magnetic(point);
                        Point ptInMeter = canvasToMeter(point);
                        line.getEndPoint().set(ptInMeter);
                    }
                }
            }
            Log.e(TAG, "adding new line " + mToolboxState.mSelectedLine + " " +  mIsAddingNewLine);
            // add a new line
            if (mToolboxState.mSelectedLine < 0
                    && mIsAddingNewLine) {
                List<Line> lines = mToolboxState.getLines();
                Point pointInMeter = canvasToMeter(mPoint1st);
                Line newLine = new Line(pointInMeter, pointInMeter.copy());
                lines.add(newLine);
                mToolboxState.mSelectedPoint = SelectedPoint.EndPoint;
                mToolboxState.mSelectedLine = lines.size() - 1;
                mIsAddingNewLine = false;
                if (mListener != null) {
                    mListener.onSelected(mToolboxState.mToolboxMode, mToolboxState.mSelectedLine);
                }
            }
        } else if (mToolboxState.mToolboxMode == EraseMode) {
            if(isNear) return;
            pointInCell = convertCanvasToCell(point);
            if (pointInCell.x < 0 || pointInCell.x > mCurrentMap.mWidth
                    || pointInCell.y < 0 || pointInCell.y> mCurrentMap.mHeight) {
                // move to the outside of map.
                return;
            }
            mToolboxState.addErasePoint(point);
            mFingerPointInCanvas = point;
        }else {
            // just move.
            Matrix matrix = new Matrix(mBitmapMatrix);
            matrix.postTranslate(point.x - mPoint1st.x,
                    point.y - mPoint1st.y);
            acceptMatrix(matrix);
            mPoint1st = point;
        }
    }

    private void onOneFingerUp(Point point) {
        if (mToolboxState.mToolboxMode == ToolboxMode.TrackerMode
                || mToolboxState.mToolboxMode == ToolboxMode.WallMode) {
//            if (mCurrentMap.clearVirtualItem()
//                    && mToolboxState.clearActualWall()
//                    && mListener != null) {
//                mToolboxState.notifySelected(-1);
//            }
            mIsAddingNewLine = false;
        }
        if (mIsSetTargets) {
            if(mCurrentTargetPose != null)
                mCurrentTargetPose.generateName();
            mTargetList.add(mCurrentTargetPose);
            mCurrentTargetPose = null;
        }
        if(mIsRelocalization) {
            if(mRelocalizationFinishedListener != null)
                mRelocalizationFinishedListener.onResult(mRobotPose);
            mRelocalizationMode = RelocalizationMode.relocalizating;
        }
        mFingerPointInCanvas = null;
    }

    private boolean getSelectedTarget(float x, float y) {
        Point current = new Point(x, y);
        for (int i = 0; i < mTargetList.size(); i++) {
            Pose pose = mTargetList.get(i);
            Point target = mCurrentMap.getPointInCell(pose.point);
            float[] pts = new float[]{target.x , target.y};
            mBitmapMatrix.mapPoints(pts);
            target.set(pts[0], pts[1]);
            Log.e(TAG, String.format("target list: %s -- current %s", target.toString(), current.toString()));
            if (current.simpleNear(target, POINT_SELECT_DISTANCE_IN_PIXEL)) {
                mSelectedTargetIndex = i;
                return true;
            }
        }
        return false;
    }

    /**
     * magnetic the selected point to the points of line.
     *
     * @param point is point in canvas, the unit is pixel.
     *              if success, the value of point will be changed.
     */
    private void magnetic(Point point) {
        List<Line> lines = mToolboxState.getLines();
        int selectedIndex = mToolboxState.mSelectedLine;
        if (lines == null) return;

        for (int i = 0; i < lines.size(); i++) {
            if (i == selectedIndex) continue;
            Line line = lines.get(i);
            Line lineInCell = mCurrentMap.getLineInCell(line);
            float[] pts = lineInCell.getPts();
            mBitmapMatrix.mapPoints(pts);
            if (point.simpleNear(new Point(pts[0], pts[1]), POINT_SELECT_DISTANCE_IN_PIXEL)) {
                point.set(pts[0], pts[1]);
                return;
            }
            if (point.simpleNear(new Point(pts[2], pts[3]), POINT_SELECT_DISTANCE_IN_PIXEL)) {
                point.set(pts[2], pts[3]);
                return;
            }
        }
    }

    private void canvasToMeter(float[] pts) {
        Matrix invert = new Matrix();
        mBitmapMatrix.invert(invert);
        invert.mapPoints(pts);
        mCurrentMap.getPointsInMeter(pts);
    }

    private Point canvasToMeter(Point pointInCanvas) {
        float[] pts = new float[]{pointInCanvas.x, pointInCanvas.y};
        canvasToMeter(pts);
        return new Point(pts[0], pts[1]);
    }

    private Line getSelectedLine() {
        return mToolboxState.getLine();
    }

    private SelectedPoint selectPoint(Line line, float x, float y) {
        if (line == null) return SelectedPoint.Nothing;
        Line lineInCanvas = mCurrentMap.getLineInCell(line);
        float[] pts = lineInCanvas.getPts();
        mBitmapMatrix.mapPoints(pts);
        double squaLimitation = POINT_SELECT_DISTANCE_IN_PIXEL * POINT_SELECT_DISTANCE_IN_PIXEL;
        double squaDistanceStartPoint = (pts[0] - x) * (pts[0] - x)
                + (pts[1] - y) * (pts[1] - y);
        double squaDistanceEndPoint = (pts[2] - x) * (pts[2] - x)
                + (pts[3] - y) * (pts[3] - y);
        if (squaDistanceStartPoint < squaLimitation)
            return SelectedPoint.StartPoint;
        if (squaDistanceEndPoint < squaLimitation)
            return SelectedPoint.EndPoint;
        return SelectedPoint.Nothing;
    }

    private int selectLine(List<Line> lines, float x, float y) {
        for (int i = 0; i < lines.size(); ++i) {
            Line line = mCurrentMap.getLineInCell(lines.get(i));
            float[] pts = line.getPts();
            mBitmapMatrix.mapPoints(pts);
            double distance = 0;
            // get the distance from line(pts) to point(x, y). the unit is pixel.
            // the formula comes from https://math.stackexchange.com/questions/2248617/shortest-distance-between-a-point-and-a-line-segment
            double temp = (pts[3] - pts[1]) * (pts[3] - pts[1]) + (pts[2] - pts[0]) * (pts[2] - pts[0]);
            double t = -((pts[0] - x) * (pts[2] - pts[0]) + (pts[1] - y) * (pts[3] - pts[1])) / temp;
            if (t >= 0 && t <= 1) {
                distance = Math.abs((pts[3] - pts[1]) * x - (pts[2] - pts[0]) * y + pts[2] * pts[1] - pts[3] * pts[0])
                        / Math.sqrt(temp);

            } else {
                double distanceP2 = Math.sqrt((pts[3] - y) * (pts[3] - y) + (pts[2] - x) * (pts[2] - x));
                double distanceP1 = Math.sqrt((pts[1] - y) * (pts[1] - y) + (pts[0] - x) * (pts[0] - x));
                distance = distanceP1 < distanceP2 ? distanceP1 : distanceP2;
            }
            if (distance < LINE_SELECT_DISTANCE_IN_PIXEL) {
                return i;
            }
        }
        return -1;
    }

    /**
     * if the matrix can be used in the canvas draw bitmap.
     * this function will keep the map in the bound.
     *
     * @param matrix the matrix should used in map drawing.
     * @return if accept
     */
    private boolean acceptMatrix(Matrix matrix) {
        float[] pts = getMapBoundByPoints(matrix);
        Rect drawingRect = new Rect();
        getDrawingRect(drawingRect);
        float maxX, minX, maxY, minY;
        minX = maxX = pts[0];
        minY = maxY = pts[1];
        for (int i = 2; i < pts.length; i += 2) {
            maxX = maxX < pts[i] ? pts[i] : maxX;
            minX = minX > pts[i] ? pts[i] : minX;
            maxY = maxY < pts[i + 1] ? pts[i + 1] : maxY;
            minY = minY > pts[i + 1] ? pts[i + 1] : minY;
        }
        if (maxX < drawingRect.width() / 2)
            return false;
        if (minX > drawingRect.width() / 2)
            return false;
        if (maxY < drawingRect.height() / 2)
            return false;
        if (minY > drawingRect.height() / 2)
            return false;

        double widthInPixel = Math.sqrt((pts[0] - pts[4]) * (pts[0] - pts[4])
                + (pts[1] - pts[5]) * (pts[1] - pts[5]));
        // too small
        if (widthInPixel < drawingRect.width() / 2) {
            return false;
        }
        double widthInMeter = mCurrentMap.mWidth * mCurrentMap.getResolution();
        double pixelPerMeter = widthInPixel / widthInMeter;
        // too big
        if (pixelPerMeter > 100) {
            return false;
        }
        mBitmapMatrix.set(matrix);
        return true;
    }


    public void relocalization(boolean start, OnRelocalizationFinishedListener listener) {
        mIsRelocalization = start;
        mIsSetTargets = false;
        mShowTargetList = false;
        if(start) {
            mToolboxState.resetToolboxStatus();
            this.mRelocalizationFinishedListener = listener;
            mRelocalizationMode = RelocalizationMode.choosePoint;
        } else {
            this.mRelocalizationFinishedListener = null;
            mRelocalizationMode = RelocalizationMode.notInRelocalizationMode;
        }
    }

    public RelocalizationMode getRelocalizationMode() {
        return mRelocalizationMode;
    }

    public void showPointCloud(boolean show) {
        mShowPointCloud = show;
    }

    public boolean getShowPointCloud() {
        return mShowPointCloud;
    }

    public void showVirtualTracker(boolean show) {
        mShowVirtualTracker = show;
    }

    public boolean getShowVirtualTracker() {
        return mShowVirtualTracker;
    }

    public void showAxis(boolean show) {
        mShowAxis = show;
    }

    public boolean getShowAxis() {
        return mShowAxis;
    }

    public void showRobot(boolean show) {
        mShowRobot = show;
    }

    public boolean getShowRobot() {
        return mShowRobot;
    }

    public void showVirtualWall(boolean show) {
        mShowVirtualWall = show;
    }

    public boolean getShowVirutalWall() {
        return mShowVirtualWall;
    }

    public void startSettingTarget(boolean start) {
        mIsSetTargets = start;
        mIsRelocalization = false;
        mShowTargetList = start;
        if(mTargetList.size() == 0)
            mSelectedTargetIndex = - 1;
        if(start) {
            mToolboxState.resetToolboxStatus();
        }
    }

    public void showTargets(boolean show) {
        mShowTargetList = show;
    }

    public List<Pose> getTargetList() {
        return mTargetList;
    }

    public void clearTargetList() {
        mTargetList.clear();
    }

    public void startMapping(boolean start) {
    }

    public void virtualTracker(boolean start) {
        mToolboxState.resetToolboxStatus();
        mIsRelocalization = false;
        mShowVirtualTracker = true;
        mShowVirtualWall = false;
        if (start)
            mToolboxState.mToolboxMode = TrackerMode;
        else
            mToolboxState.mToolboxMode = Nothing;
        Log.e(TAG, "vt:" + mToolboxState.mToolboxMode);
    }

    public void virtualWall(boolean start) {
        mToolboxState.resetToolboxStatus();
        mIsRelocalization = false;
        mShowVirtualTracker = false;
        mShowVirtualWall = true;
        if (start)
            mToolboxState.mToolboxMode = WallMode;
        else
            mToolboxState.mToolboxMode = Nothing;
        Log.e(TAG, "vw:" +  mToolboxState + " " + mToolboxState.mToolboxMode);
    }

    public void actualWall(boolean start) {
        mToolboxState.resetToolboxStatus();
        mIsRelocalization = false;
        mShowVirtualTracker = false;
        mShowVirtualWall = false;
        if (start)
            mToolboxState.mToolboxMode = ActualWallMode;
        else {
            mToolboxState.mToolboxMode = Nothing;
        }
        Log.e(TAG, "aw:" + mToolboxState.mToolboxMode);
    }

    public void startErase(boolean start, EraseType type) {
        mToolboxState.resetToolboxStatus();
        mIsRelocalization = false;
        mShowVirtualTracker = false;
        mShowVirtualWall = false;
        mToolboxState.mEraseType = type;
        if (start)
            mToolboxState.mToolboxMode = EraseMode;
        else {
            mToolboxState.mToolboxMode = Nothing;
            mToolboxState.mErasePoints.clear();
        }
        if (type == EraseType.MagicBrush) {
            invalidate();
        }
        Log.e(TAG, "e:" + mToolboxState.mToolboxMode);
    }

    public void setRobotPose(Pose pose) {
        if(mRelocalizationMode != RelocalizationMode.choosePoint) {
            mRobotPose = pose;
        }
        postInvalidate();
    }

    public void setPointCloud(List<Point> scan) {
        mPointCloud = scan;
        postInvalidate();
    }

    public void setShowScale(boolean show) {
        mShowScale = show;
    }

    public boolean getShowScale() {
        return mShowScale;
    }

    public void setScaleDrawPoint(Point point) {
        mScaleDrawPoint = point;
    }

    public void setOnVirtualItemSelectedListener(OnVirtualItemSelectedListener listener) {
        mListener = listener;
    }

    public Pose getRobotPose() {
        return mRobotPose;
    }
}

