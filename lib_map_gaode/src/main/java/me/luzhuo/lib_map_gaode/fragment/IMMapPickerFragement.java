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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import me.luzhuo.lib_map_gaode.GeoCoderManger;
import me.luzhuo.lib_map_gaode.LocationManager;
import me.luzhuo.lib_map_gaode.OnScreentShotCallback;
import me.luzhuo.lib_map_gaode.bean.LocationBean;
import me.luzhuo.lib_map_gaode.life.MapViewObserver;
import me.luzhuo.lib_map_gaode.R;
import me.luzhuo.lib_map_gaode.adapter.IMMapPickerAdapter;

public class IMMapPickerFragement extends Fragment implements View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener, AMapLocationListener, PoiSearch.OnPoiSearchListener, AMap.OnMapTouchListener {
    private MapView map_im_mapview;
    private RecyclerView map_im_list;
    private AMap aMap;

    private List<LocationBean> posItems = new ArrayList<>();
    private IMMapPickerAdapter adapter;
    private GeoCoderManger geoCoderManger;
    private LocationManager locationManager = new LocationManager();
    private LocationBean currentLocation;
    // 首次定位获得的位置, 用于下次的抚慰
    private LocationBean firstLocation;

    private static final String TAG = IMMapPickerFragement.class.getSimpleName();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_im_select, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initMap(savedInstanceState);
        initRecyclerView();
    }

    private void initView() {
        map_im_mapview = getView().findViewById(R.id.map_im_mapview);
        map_im_list = getView().findViewById(R.id.map_im_list);
        getView().findViewById(R.id.map_im_my_location).setOnClickListener(this);
    }

    private void initMap(Bundle savedInstanceState) {
        map_im_mapview.onCreate(savedInstanceState);
        aMap = map_im_mapview.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        aMap.setMyLocationEnabled(true);
        aMap.setOnMapTouchListener(this);
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setScaleControlsEnabled(true);
        mUiSettings.setZoomControlsEnabled(false);
        // 逆地理编码
        geoCoderManger = new GeoCoderManger(getContext(), this);
        getLifecycle().addObserver(new MapViewObserver(map_im_mapview));

        locationManager.locationOne(getContext(), this);
    }

    private void initRecyclerView() {
        map_im_list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        adapter = new IMMapPickerAdapter(posItems);
        adapter.setOnMapPickerCallback(new IMMapPickerAdapter.OnMapPickerCallback() {
            @Override
            public void onMapPicked(int position, LocationBean item) {
                moveMap(item);
            }
        });
        map_im_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.map_im_my_location) {
            this.currentLocation = new LocationBean(firstLocation.latitude, firstLocation.longitude, firstLocation.city, firstLocation.title, firstLocation.address);
            moveMap(true);
        }
    }

    /**
     * 定位成功后移动地图
     * @param isAnimation 是否是动画
     */
    private void moveMap(boolean isAnimation) {
        if (currentLocation == null) return;

        geoCoderManger.getAddress(new LatLonPoint(currentLocation.latitude, currentLocation.longitude));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(currentLocation.latitude, currentLocation.longitude), 18, 30, 0));
        if (isAnimation) aMap.animateCamera(cameraUpdate, 600, null);
        else aMap.moveCamera(cameraUpdate);

        // poi搜索
        searchPoi("", currentLocation.city, new LatLonPoint(currentLocation.latitude, currentLocation.longitude));
    }

    /**
     * 用户选择的移动
     * @param poiItem
     */
    private void moveMap(LocationBean poiItem) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(poiItem.latitude, poiItem.longitude), 18, 30, 0));
        aMap.animateCamera(cameraUpdate, 100, null);
    }

    private PoiSearch.Query mQuery;
    private void searchPoi(String keyword, String city, LatLonPoint latLonPoint) {
        // //第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        mQuery = new PoiSearch.Query(keyword, "", city);
        mQuery.setPageSize(30);// 设置每页最多返回多少条poiitem
        mQuery.setPageNum(0);

        PoiSearch mPoiSearch = new PoiSearch(getContext(), mQuery);
        mPoiSearch.setOnPoiSearchListener(this);
        //该范围的中心点-----半径，单位：米-----是否按照距离排序
        mPoiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 100, true));
        mPoiSearch.searchPOIAsyn();// 异步搜索
    }

    public void screentShot(final OnScreentShotCallback callback) {
        if (adapter.currentIndex >= posItems.size()) return;

        LocationBean bean = posItems.get(adapter.currentIndex);
        aMap.addMarker(new MarkerOptions().position(new LatLng(bean.latitude, bean.longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        aMap.getMapScreenShot(new AMap.OnMapScreenShotListener(){
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                aMap.clear();
                if (callback != null) callback.onScreentShotCallback(bitmap);
            }
        });
        aMap.invalidate();// 刷新地图
    }

    public LocationBean getCurrentPicker(){
        if (adapter.currentIndex < posItems.size()) return posItems.get(adapter.currentIndex);
        else return null;
    }

    // ====================== Map 生命周期处理 =======================

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        map_im_mapview.onSaveInstanceState(outState);
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {

            posItems.clear();
            adapter.currentIndex = 0;
            List<PoiItem> pois = result.getRegeocodeAddress().getPois();
            for (PoiItem poiItem : pois) {
                LatLonPoint latLonPoint = poiItem.getLatLonPoint();
                posItems.add(new LocationBean(latLonPoint.getLatitude(), latLonPoint.getLongitude(), poiItem.getCityName(), poiItem.getTitle(), poiItem.getSnippet()));
            }
            adapter.notifyDataSetChanged();

        } else Toast.makeText(getContext(), "err code: " + rCode, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) { }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        Log.e(TAG, "onLocationChanged");
        // 地图定位的回调
        if (aMapLocation == null) return;

        if (aMapLocation.getErrorCode() == 0) {
            this.currentLocation = new LocationBean(aMapLocation.getLatitude(), aMapLocation.getLongitude(), aMapLocation.getCity(), aMapLocation.getPoiName(), aMapLocation.getAddress());
            this.firstLocation = new LocationBean(aMapLocation.getLatitude(), aMapLocation.getLongitude(), aMapLocation.getCity(), aMapLocation.getPoiName(), aMapLocation.getAddress());
            moveMap(false);
        }
        else Toast.makeText(getContext(), "" + aMapLocation.getErrorCode() + " : " + aMapLocation.getErrorInfo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        if (i != 1000) return;
        posItems.clear();
        for (PoiItem pois : poiResult.getPois()) {
            LatLonPoint latLonPoint = pois.getLatLonPoint();
            posItems.add(new LocationBean(latLonPoint.getLatitude(), latLonPoint.getLongitude(), pois.getCityName(), pois.getTitle(), pois.getSnippet()));
        }
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) { }

    @Override
    public void onTouch(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            LatLng latLng = aMap.getCameraPosition().target;
            this.currentLocation.latitude = latLng.latitude;
            this.currentLocation.longitude = latLng.longitude;
            moveMap(true);
        }
    }
}
