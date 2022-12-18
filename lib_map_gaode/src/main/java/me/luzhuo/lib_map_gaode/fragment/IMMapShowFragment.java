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

import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import me.luzhuo.lib_map_gaode.MapUtils;
import me.luzhuo.lib_map_gaode.R;
import me.luzhuo.lib_map_gaode.bean.LocationBean;
import me.luzhuo.lib_map_gaode.life.MapViewObserver;

public class IMMapShowFragment extends Fragment implements View.OnClickListener {
    private LocationBean data;
    private MapView map_im_mapview;
    private AMap aMap;
    private TextView map_im_title;
    private TextView map_im_address;

    private IMMapShowFragment() { }
    public static IMMapShowFragment instance(LocationBean data) {
        IMMapShowFragment fragment = new IMMapShowFragment();
        Bundle args = new Bundle();
        args.putSerializable("data", data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) data = (LocationBean) getArguments().getSerializable("data");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_im_show, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initMap(savedInstanceState);
        initData();
    }

    private void initView() {
        map_im_mapview = getView().findViewById(R.id.map_im_mapview);
        map_im_title = getView().findViewById(R.id.map_im_title);
        map_im_address = getView().findViewById(R.id.map_im_address);
        getView().findViewById(R.id.map_im_gotomap).setOnClickListener(this);
        getView().findViewById(R.id.map_im_my_location).setOnClickListener(this);
        getView().findViewById(R.id.map_im_show_zone).setOnClickListener(this);
    }

    private void initMap(Bundle savedInstanceState) {
        map_im_mapview.onCreate(savedInstanceState);
        aMap = map_im_mapview.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        aMap.setMyLocationEnabled(true); // 显示定位蓝点, 并自动定位
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
        // 逆地理编码
        getLifecycle().addObserver(new MapViewObserver(map_im_mapview));

        map_im_mapview.postDelayed(new Runnable() {
            @Override
            public void run() {
                aMap.addMarker(new MarkerOptions().position(new LatLng(data.latitude, data.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(data.latitude, data.longitude), 18, 30, 30)));
            }
        }, 1000);
    }

    private void initData() {
        if (!TextUtils.isEmpty(data.title)) {
            map_im_title.setVisibility(View.VISIBLE);
            map_im_title.setText(""+ data.title);
        } else map_im_title.setVisibility(View.GONE);
        map_im_address.setText(""+ data.address);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.map_im_gotomap) {
            if (MapUtils.isGdMapInstalled()) MapUtils.openGaoDeNavi(getContext(), 0, 0, null, data.latitude, data.longitude, data.address);
            else if (MapUtils.isBaiduMapInstalled()) MapUtils.openBaiDuNavi(getContext(), 0, 0, null, data.latitude, data.longitude, data.address);
            else if (MapUtils.isTencentMapInstalled()) MapUtils.openTencentMap(getContext(), 0, 0, null, data.latitude, data.longitude, data.address);
            else Toast.makeText(getContext(), "手机未安装地图应用", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.map_im_my_location) {
            Location myLocation = aMap.getMyLocation();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 18, 30, 0));
            aMap.animateCamera(cameraUpdate, 600, null);
        } else if (id == R.id.map_im_show_zone) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(data.latitude, data.longitude), 18, 30, 0));
            aMap.animateCamera(cameraUpdate, 600, null);
        }
    }
}
