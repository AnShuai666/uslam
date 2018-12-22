package com.ubtrobot.uslam.net.bean;

import java.util.List;

/**
 * @author leo
 * @date 2018/12/5
 * @email ao.liu@ubtrobot.com
 */
public class MapListResponse {
    // TODO: 2018/12/5 接口文档里的json字符串缺少key

    public List<NetBeanThumnailMapInfo> map_list;

    public class NetBeanThumnailMapInfo {
        public String access_time;
        public String create_time;
        public String map_name;
        public String modify_time;
        public String size;
        public String thumbnail;
    }
}
