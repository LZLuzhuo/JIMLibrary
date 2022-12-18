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
package me.luzhuo.lib_map_gaode.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import me.luzhuo.lib_map_gaode.R;

/**
 * 单纯的显示地图
 */
public class MapShowFragment extends Fragment {
    private double latitude, longitude;
    private int point;

    private MapView map_mapview;
    private AMap aMap;

    public static MapShowFragment instance(double latitude, double longitude, @DrawableRes int point) {
        MapShowFragment fragment = new MapShowFragment();
        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        args.putInt("point", point);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapShowFragment instance(@DrawableRes int point) {
        return instance(0.0, 0.0, point);
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");
            point = getArguments().getInt("point");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_show, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initMap(savedInstanceState);
        initData();
    }

    private void initView() {
        map_mapview = getView().findViewById(R.id.map_mapview);
    }

    private void initMap(Bundle savedInstanceState) {
        map_mapview.onCreate(savedInstanceState);
        aMap = map_mapview.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
    }

    private void initData() {
        aMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(point)));
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude, longitude), 18, 30, 30)));
    }

    public void setPoint(double latitude, double longitude) {
        if (aMap == null) return;

        this.latitude = latitude;
        this.longitude = longitude;
        aMap.clear();
        aMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(BitmapDescriptorFactory.fromResource(point)));
    }

    /**
     * 移到中心位置
     */
    public void moveCenter() {
        if (aMap == null) return;

        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(latitude, longitude), 18, 30, 30)));
    }
}
