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
package me.luzhuo.lib_map_gaode.life;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.amap.api.maps2d.MapView;

public class MapViewObserver implements LifecycleObserver {

    private MapView mapView;

    public MapViewObserver(MapView mapView) {
        this.mapView = mapView;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void create(){
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void start(){
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void pause(){
        mapView.onPause();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void resume(){
        mapView.onResume();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void stop(){
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void destroy(){
        mapView.onDestroy();
    }
}
