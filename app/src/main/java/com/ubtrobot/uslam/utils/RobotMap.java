package com.ubtrobot.uslam.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.ubtrobot.uslam.views.MagicBrush;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class RobotMap implements Serializable {


    private final static String TAG = "RobotMap";
    public static final int COLOR_NOT_SURE = 0x646400; // 100
    public static final int COLOR_FREE = 0x000000; // 0
    public static final int COLOR_UNKNOWN = 0xCDCDCD; // 205
    public static final int COLOR_OBSTACLE = 0xFFFFFF; // 255

    public int mapId;
    public boolean isCurrentMap;
    public String mMapName;
    public String mCreateTime;
    public String mAccessTime;
    public String mModifyTime;

    public String mMapSize;

    public Bitmap mBitmap;
    public Bitmap mThumbnail;
    public List<Line> wall = new ArrayList<>();
    public List<Line> tracker = new ArrayList<>();

    /**
     * the mBitmap mResolution, [m/cell]
     */
    public float mResolution;

    /**
     * the mOrigin of mBitmap, [m, m, rad], this is the real-world pose of the cell(0,0) in the mBitmap
     */
    public Pose mOrigin;

    /**
     * mBitmap mWidth, [cell]
     */
    public int mWidth;

    /**
     * mBitmap mHeight [cell]
     */
    public int mHeight;

    public float mNegate;

    public float mOccupiedThresh;

    public String mMapData;

    public float mFreeThresh;
    private Bitmap mMagicMap;
    private boolean mRealtimeMap = false;

    public RobotMap(InputStream is) throws IOException, JSONException {
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String json = new String(buffer, "UTF-8");
        setJson(json);
    }

    public RobotMap() {
        mRealtimeMap = true;
    }

    public RobotMap(String name) {
        this.mMapName = name;
    }

    protected RobotMap(Parcel in) {
        mapId = in.readInt();
        isCurrentMap = in.readByte() != 0;
        mMapName = in.readString();
        mCreateTime = in.readString();
        mAccessTime = in.readString();
        mModifyTime = in.readString();
        mMapSize = in.readString();
        mBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        mThumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        mResolution = in.readFloat();
        mWidth = in.readInt();
        mHeight = in.readInt();
        mNegate = in.readFloat();
        mOccupiedThresh = in.readFloat();
        mMapData = in.readString();
        mFreeThresh = in.readFloat();
        mMagicMap = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public void setJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        mResolution = (float) obj.getJSONObject("metadata").getDouble("resolution");
        mWidth = obj.getJSONObject("metadata").getInt("width");
        mHeight = obj.getJSONObject("metadata").getInt("height");

        mNegate = (float) obj.getJSONObject("metadata").getDouble("negate");
        mOccupiedThresh = (float) obj.getJSONObject("metadata").getDouble("occupied_thresh");
        mFreeThresh = (float) obj.getJSONObject("metadata").getDouble("free_thresh");

        mOrigin = new Pose(obj.getJSONObject("metadata").getJSONArray("origin"));
        mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);

        JSONArray mapData = obj.getJSONArray("mapdata");
        for (int row = 0; row < mHeight; ++row) {
            JSONArray rowData = mapData.getJSONArray(row);
            for (int rawDataIndex = 0; rawDataIndex < rowData.length(); ++rawDataIndex) {
                int data = rowData.getInt(rawDataIndex);
                for(int i = 0; i < 16; ++i) {
                    int tmp = (data & (0x03 << ( i * 2))) >>> (i * 2);
                    int color;
                    switch (tmp) {
                        case 0: {
                            color = COLOR_FREE;
                            break;
                        }
                        case 1: {
                            color = COLOR_OBSTACLE;
                            break;
                        }
                        case 2: {
                            color = COLOR_UNKNOWN;
                            break;
                        }
                        default:
                            color = COLOR_NOT_SURE;
                    }
                    int col = i + rawDataIndex * 16;
                    if(col < mWidth) {
                        mBitmap.setPixel(col, row, color);
                    }
                }
            }
        }
        if (obj.has("virtual_wall")) {
            JSONArray wallJson = obj.getJSONArray("virtual_wall");
            refreshLineSetData(wallJson, wall);
        }
        if(obj.has("virtual_path")) {
            JSONArray pathJson = obj.getJSONArray("virtual_path");
            refreshLineSetData(pathJson, tracker);
        }
    }

    private void refreshLineSetData(JSONArray jsonArray, List<Line> lineSet) throws JSONException {
        lineSet.clear();
        for (int i = 0; i < jsonArray.length(); ++i) {
            JSONArray lines = jsonArray.getJSONArray(i);
            if(lines.length() < 4) {
                continue;
            }
            Point startPoint = new Point((float) lines.getDouble(0),
                    (float)lines.getDouble(1));
            for(int j = 2; j < lines.length(); j += 2) {
                Point endPoint = new Point((float) lines.getDouble(j),
                        (float)lines.getDouble(j + 1));
                Line line = new Line(startPoint, endPoint);
                lineSet.add(line);
                startPoint = endPoint.copy();
            }
        }
    }


    public Point getOriginInCell() {
        return getPointInCell(new Point());
    }

    /**
     * change a [meter] point to [cell].
     * @param point unit is [meter]
     * @return  the unit of the return point is [cell]
     */
    public Point getPointInCell(Point point) {
        Point cellPoint = new Point();
        cellPoint.x = (-mOrigin.getX() + point.x) / mResolution;
        cellPoint.y = mHeight - (-mOrigin.getY() + point.y) / mResolution;
        return cellPoint;
    }

    public Line getLineInCell(Line line) {
        Point startPoint = getPointInCell(line.getStartPoint());
        Point endPoint = getPointInCell(line.getEndPoint());
        return new Line(startPoint, endPoint);
    }

    public Point getPointInCellWithoutOrigin(Point point) {
        Point cellPoint = new Point();
        cellPoint.x = (point.x) / mResolution;
        cellPoint.y = (point.y) / mResolution;
        return cellPoint;
    }

    public void getPointsInMeter(float[] pts) {
        for (int i = 0; i < pts.length / 2; i += 2) {
            pts[i] = pts[i] * mResolution + mOrigin.getX();
            pts[i + 1] = (mHeight - pts[i + 1]) * mResolution + mOrigin.getY();
        }
    }

    public Line getTrackerByIndex(int index) {
        if(index > 0 && index < tracker.size()) {
            return tracker.get(index);
        }
        return null;
    }

    public Line getWallByIndex(int index){
        if(index > 0 && index < wall.size()) {
            return wall.get(index);
        }
        return null;
    }

    public boolean clearVirtualItem() {
        return  clearVirtualItem(tracker) || clearVirtualItem(wall);
    }

    public boolean clearVirtualItem(List<Line> lines) {
        boolean result = false;
        for (Iterator<Line> iterator = lines.iterator(); iterator.hasNext(); ) {
            Line line = iterator.next();
            // shorter than 5cm
            if(line.tooSmall(0.05f)) {
                iterator.remove();
                result = true;
            }
        }
        return result;
    }

    public void saveActualWall(List<Line> mActualWallLines) {
//        wall = mActualWallLines;
    }

    public int getFreeColor() {
        return ~COLOR_FREE | 0xff000000;
    }

    public int getUnknownColor() {
        return COLOR_UNKNOWN | 0xff000000;
    }

    public Bitmap getMagicBitmap() {
        if(mMagicMap == null) {
            MagicBrush brush = new MagicBrush(mBitmap, 5);
            brush.magic();
            mMagicMap = brush.getTarget();
        }
        return mMagicMap;
    }

/** Setter and getter */
    public float getResolution() {
        return mResolution;
    }

    public void setResolution(float mResolution) {
        this.mResolution = mResolution;
    }

    public Pose getOrigin() {
        return mOrigin;
    }

    public void setOrigin(Pose mOrigin) {
        this.mOrigin = mOrigin;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public String getMapData() {
        return mMapData;
    }

    public void setMapData(String mMapData) {
        this.mMapData = mMapData;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public boolean isFreeColor(int x, int y) {
        if(mBitmap != null) {
            return mBitmap.getPixel(x, y) == getFreeColor();
        }
        return  false;
    }

    public boolean sameSize(RobotMap map) {
        if(map != null) {
            return map.getWidth() == getWidth() && map.getHeight() == getHeight();
        }
        return false;
    }

    public void setName(String name) {
        this.mMapName = name;
    }

    public void setRealtimeMap(boolean realtime) {
        mRealtimeMap = realtime;
    }

    public boolean isRealtimeMap() {
        return mRealtimeMap;
    }

    public String getMapName() {
        if(mMapName != null) {
            return  mMapName.substring(mMapName.lastIndexOf("/") + 1, mMapName.length());
        }
        return null;
    }
}

