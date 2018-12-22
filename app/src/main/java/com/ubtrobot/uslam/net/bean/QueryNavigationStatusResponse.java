package com.ubtrobot.uslam.net.bean;

import com.ubtrobot.uslam.utils.Point3D;

import java.util.List;

/**
 * @author leo
 * @date 2018/12/5
 * @email ao.liu@ubtrobot.com
 */
public class QueryNavigationStatusResponse {

    public List<Point3D> target_list;
    public int loop_count;
    boolean end_to_start;
}
