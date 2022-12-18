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

import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;

import java.util.List;

import me.luzhuo.lib_map_gaode.life.MapViewObserver;

/**
 * 高德地图
 *
 * <p>
 * getSupportFragmentManager()
 *         .beginTransaction()
 *         .replace(R.id.frame, MapFragment.instance())
 *         .commit();
 * </p>
 */
public class MapFragment extends Fragment {

    private MapViewModel mViewModel;
    private UiSettings mUiSettings;
    private MapView mapView;
    private AMap aMap;

    public static MapFragment instance() {
        return instance(new AMapOptions());
    }

    public static MapFragment instance(AMapOptions options) {
        MapFragment fragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", options);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(MapViewModel.class);
        mapView = (MapView) getView().findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); // 此方法必须重写
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL); // 矢量地图模式
        // aMap.setOnMarkerClickListener(this); // Market 点击的回调
        // aMap.setOnInfoWindowClickListener(this); // Market 信息 点击的回调
        mUiSettings = aMap.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true); // 显示标尺 (false)
        mUiSettings.setZoomControlsEnabled(false); // 不显示缩放按钮 (true)
        mUiSettings.setScrollGesturesEnabled(true); // 手势滑动 (true)
        mUiSettings.setZoomGesturesEnabled(true); // 手势缩放 (true)
        getLifecycle().addObserver(new MapViewObserver(mapView));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 设置地图中心
     * @param longitude 纬度 116.3154950
     * @param latitude 经度 39.983456
     */
    public void setCenter(double latitude, double longitude) {
        /**
         * CameraPosition(new LatLng(latitude, longitude), 18, 30, 30))
         * new LatLng(latitude, longitude): 屏幕经纬度坐标
         * 18: 可视区域缩放级别
         * 30: 可视区域的倾斜度, 单位角度
         * 30: 可视区域志向的方向, 单位角度, 正北向顺时针方向计算, 从0-360°
         */
        if (aMap != null) aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude, longitude), 18, 30, 30)));
    }

    /**
     * 清空所有标注
     */
    public void clearMarket() {
        if (aMap != null) aMap.clear();
    }

    /**
     * 添加标注 (红色点)
     */
    public void addMarket_readPoint(double latitude, double longitude) {
        if (aMap != null) aMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    public void addMarket_readPoint(double latitude, double longitude, String title, String snippet) {
        if (aMap != null) aMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(title).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    public void screentShot(final OnScreentShotCallback callback) {
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener(){
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                if (callback != null) callback.onScreentShotCallback(bitmap);
            }
        });
        aMap.invalidate();// 刷新地图
    }

    public List<Marker> getMarkets() {
        if(aMap == null) return null;
        List<Marker> markers = aMap.getMapScreenMarkers();
        return markers;
    }

    /**
     * 设置地图缩放等级 [3, 19], 数字越大, 信息越精细
     * @param level [3, 19]
     */
    public void setZoom(int level) {
        aMap.moveCamera(CameraUpdateFactory.zoomTo(level));
    }
}