package com.ubtrobot.uslam.data;

import com.ubtrobot.uslam.MapFile;
import com.ubtrobot.uslam.control.MapControler;
import com.ubtrobot.uslam.net.bean.GetMapResponse;
import com.ubtrobot.uslam.net.bean.RobotStatusResponse;
import com.ubtrobot.uslam.sdk.IRemoteRobotSdk;
import com.ubtrobot.uslam.utils.FileUtils;
import com.ubtrobot.uslam.utils.ImageUtils;
import com.ubtrobot.uslam.utils.Line;
import com.ubtrobot.uslam.utils.Pose;
import com.ubtrobot.uslam.utils.RobotMap;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author leo
 * @date 2018/12/14
 * @email ao.liu@ubtrobot.com
 */
public class MapSaver {

    private static final String DIR_SAVE_MAP = FileUtils.getMapSavePath();

    public static void saveMap(String mapName, String map) {

        try {
            File file = new File(DIR_SAVE_MAP, mapName);
            if (file.exists()) {
                file.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(map);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GetMapResponse getMapFromFile(String fileName) {
        return ObjectWriter.read(DIR_SAVE_MAP, fileName, GetMapResponse.class);
    }

    public static List<MapFile> getLocalMapList() {
        List<MapFile> mapList = new ArrayList<>();
        File file = new File(DIR_SAVE_MAP);
        if (file.isDirectory()) {
            if (file != null) {
                File[] files = file.listFiles(new FileNameFilter());
                if (files != null
                        && files.length > 0) {
                    for (File f : files) {
                GetMapResponse map = getMapFromFile(f.getName());
//                RobotMap robotMap = parseRobotMapFromJson(map, false);
                        MapFile mapFile = new MapFile(f.getName());
                        mapList.add(mapFile);
                    }
                }
            }
        }
        return mapList;
    }

    public static RobotMap getRobotMapFromFile(String fileName) {
        GetMapResponse map = getMapFromFile(fileName);
        RobotMap robotMap = parseRobotMapFromJson(map, false);
        return robotMap;
    }

    public static RobotMap parseRobotMapFromJson(GetMapResponse body, boolean isPng) {

        RobotStatusResponse.NetBeanMap map = body.map;
        RobotMap robotMap = new RobotMap(body.map_name);
        robotMap.mCreateTime = body.create_time;

        List<RobotStatusResponse.NetBeanWall> walls = map.wall;
        List<RobotStatusResponse.NetBeanTracker> trackers = map.tracker;

        if (walls != null && walls.size() > 0) {
            List<Line> actualWall = new ArrayList<>();
            for (RobotStatusResponse.NetBeanWall wall : walls) {
                Line line = new Line(wall.x1, wall.y1, wall.x2, wall.y2);
                actualWall.add(line);
            }
            robotMap.wall = actualWall;
        }
        if (trackers != null && trackers.size() > 0) {
            List<Line> trackerWall = new ArrayList<>();
            for (RobotStatusResponse.NetBeanTracker tracker : trackers) {
                Line line = new Line(tracker.x1, tracker.y1, tracker.x2, tracker.y2);
                trackerWall.add(line);
            }
            robotMap.tracker = trackerWall;
        }

        if (isPng) {
            Pose pose = new Pose(map.info.origin.x, map.info.origin.y, map.info.origin.theta);
            robotMap.setOrigin(pose);
            robotMap.setWidth(map.info.width);
            robotMap.setHeight(map.info.height);
            robotMap.setResolution(map.info.resolution);
            robotMap.setBitmap(ImageUtils.decodeBitmapFromBase64(map.png_map));
        } else {
            try {
                if (map.umap != null) {
                    robotMap.setJson(map.umap.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return robotMap;
    }

    public static void ecportMap(String mapName, IRemoteRobotSdk.OnExportMapListener listener) {
        MapControler.g().exportMap(mapName, listener);
    }

    private static class FileNameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".umap")
                    || name.endsWith(".png")) {
                return true;
            }
            return false;
        }
    }

}
