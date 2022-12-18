/* Copyright 2021 Luzhuo. All rights reserved.
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
package me.luzhuo.lib_map_gaode.bean;

import java.io.Serializable;

public class LocationBean implements Serializable {
    public double latitude;
    public double longitude;
    public String city;
    public String title;
    public String address;

    /**
     * 地图poi选择的Bean
     */
    public LocationBean(double latitude, double longitude, String city, String title, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.title = title;
        this.address = address;
    }

    /**
     * 传给地图展示的Bean
     */
    public LocationBean(double latitude, double longitude, String title, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.title = title;
        this.address = address;
    }
}
