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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import androidx.core.content.ContextCompat;

/**
 * 定位服务
 * 需要权限: Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
 *
 * AndroidQ+
 * 后台定位需要权限: Manifest.permission.ACCESS_BACKGROUND_LOCATION
 */
public class LocationManager {
    /**
     * 仅定位一次
     *
     * public void onLocationChanged(AMapLocation amapLocation) {
     *     if (amapLocation != null) {
     *         if (amapLocation.getErrorCode() == 0) {
     *             //定位成功回调信息，设置相关消息
     *             amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
     *             amapLocation.getLatitude();//获取纬度
     *             amapLocation.getLongitude();//获取经度
     *             amapLocation.getAccuracy();//获取精度信息
     *             SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     *             Date date = new Date(amapLocation.getTime());
     *             df.format(date);//定位时间
     *         } else {
     *             //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
     *             Log.e("AmapError","location Error, ErrCode:"
     *                     + amapLocation.getErrorCode() + ", errInfo:"
     *                     + amapLocation.getErrorInfo());
     *         }
     *     }
     * }
     */
    public void locationOne(Context context, AMapLocationListener listener) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "请授予定位权限!", Toast.LENGTH_SHORT).show();
            return;
        }

        AMapLocationClient mlocationClient = new AMapLocationClient(context.getApplicationContext());
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mlocationClient.setLocationListener(listener);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocation(true);
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    private AMapLocationClient mlocationClient = null;
    /**
     * (连续)定位
     */
    public void startLocation(Context context, AMapLocationListener listener) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "请授予定位权限!", Toast.LENGTH_SHORT).show();
            return;
        }

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setInterval(600);

        if (mlocationClient == null)
            mlocationClient = new AMapLocationClient(context.getApplicationContext());
        mlocationClient.setLocationListener(listener);
        mlocationClient.setLocationOption(mLocationOption);
        mlocationClient.startLocation();
    }

    public void endLocation() {
        if (mlocationClient == null) return;

        mlocationClient.stopLocation();
        mlocationClient = null;
    }
}
