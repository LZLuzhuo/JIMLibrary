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
package me.luzhuo.lib_map_gaode;

import android.content.Context;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;

/**
 * 地理编码 与 逆地理编码 服务
 */
public class GeoCoderManger {
    private GeocodeSearch geocoderSearch;
    public GeoCoderManger (Context context, GeocodeSearch.OnGeocodeSearchListener listener){
        geocoderSearch = new GeocodeSearch(context.getApplicationContext());
        geocoderSearch.setOnGeocodeSearchListener(listener);
    }

    /**
     * 		if (rCode == AMapException.CODE_AMAP_SUCCESS) {
     * 			if (result != null && result.getRegeocodeAddress() != null
     * 					&& result.getRegeocodeAddress().getFormatAddress() != null) {
     * 				addressName = result.getRegeocodeAddress().getFormatAddress()
     * 						+ "附近";
     * 				aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
     * 						AMapUtil.convertToLatLng(latLonPoint), 15));
     * 				regeoMarker.setPosition(AMapUtil.convertToLatLng(latLonPoint));
     * 				ToastUtil.show(ReGeocoderActivity.this, addressName);
     *                        } else {
     * 				ToastUtil.show(ReGeocoderActivity.this, R.string.no_result);
     *            }* 		} else {
     * 			ToastUtil.showerror(this, rCode);
     *        }
     * @param latLonPoint
     */
    public void getAddress(LatLonPoint latLonPoint){
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 600, GeocodeSearch.AMAP);
        geocoderSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }
}
