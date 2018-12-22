package com.ubtrobot.uslam.net.bean;


import com.ubtrobot.uslam.utils.Point;
import com.ubtrobot.uslam.utils.Point3D;
import com.ubtrobot.uslam.utils.Pose;

import org.json.JSONObject;

import java.util.List;

/**
 * @author leo
 * @date 2018/12/5
 * @email ao.liu@ubtrobot.com
 */
public class RobotStatusResponse {

    public NetBeanRobot robot;

    public NetBeanData data;

    public class NetBeanRobot {
        public int buttery;
        public String name;
        public String status;
        public String mode;
        public String currentmapname;
    }

   public class NetBeanData {
        public int relocalization;
        public int navigation;
        public int mapping;
        public List<Point> lidar;
        public NetBeanOrigin current_pose;
        public List<Point> path;
        public NetBeanMap realtime_map;
        public String speed;
    }

   public class NetBeanMap {
        public NetBeanMapInfo info;
        // png map url
        public String map_data;
        public String png_map;
        public Object umap;
       public List<NetBeanWall> wall;
       public List<NetBeanTracker> tracker;
    }

    public class NetBeanMapInfo {
        public float resolution;
        public int width;
        public int height;
        public NetBeanOrigin origin;
    }

    public class NetBeanOrigin {
        public float theta;
        public float x;
        public float y;
    }

    public class NetBeanWall {
        public float x1;
        public float y1;
        public float x2;
        public float y2;
    }

    public class NetBeanTracker {
        public float x1;
        public float y1;
        public float x2;
        public float y2;
    }
}
