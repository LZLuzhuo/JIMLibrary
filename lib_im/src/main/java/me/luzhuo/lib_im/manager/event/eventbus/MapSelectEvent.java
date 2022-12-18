/* Copyright 2020 Luzhuo. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.luzhuo.lib_im.manager.event.eventbus;

import java.io.Serializable;

/**
 * 地图选择返回的页面
 */
public class MapSelectEvent implements Serializable {
    public int type;
    public String targetId;

    /**
     * 图片路径
     */
    public String filePath;
    /**
     * 图片的网络路径
     */
    public String fileUrl;

    public double lat;
    public double lon;

    /**
     * 地址
     */
    public String city;
    public String title;
    public String address;

    /**
     * 地图传给IM详情的Bean
     */
    public MapSelectEvent(int type, String targetId, String filePath, String fileUrl, double lat, double lon, String city, String title, String address) {
        this.type = type;
        this.targetId = targetId;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.title = title;
        this.address = address;
    }

    /**
     * 传给地图显示的Bean
     */
    public MapSelectEvent(double lat, double lon, String title, String address) {
        this.lat = lat;
        this.lon = lon;
        this.title = title;
        this.address = address;
    }
}
