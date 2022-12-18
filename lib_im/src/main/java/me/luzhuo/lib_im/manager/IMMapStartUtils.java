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
package me.luzhuo.lib_im.manager;

import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;

import me.luzhuo.lib_im.manager.enums.ConversationType;
import me.luzhuo.lib_im.manager.event.eventbus.MapSelectEvent;

public class IMMapStartUtils {
    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Context context;

    public IMMapStartUtils(Context context) {
        this.context = context;

        // init match
        final String packageName = context.getPackageName();
        uriMatcher.addURI(IMCommonConfig.authority, IMCommonConfig.showMapPath + "/" + packageName, 0);
        uriMatcher.addURI(IMCommonConfig.authority, IMCommonConfig.selectMapPath + "/" + packageName, 1);
    }

    /**
     * 打开用于显示的Map
     * @param targetEvent 目标 数据
     */
    public static void startShowMap(Context context, MapSelectEvent targetEvent) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(IMCommonConfig.scheme)
                    .authority(IMCommonConfig.authority)
                    .appendEncodedPath(IMCommonConfig.showMapPath)
                    .appendPath(context.getPackageName())
                    .appendQueryParameter("latitude", String.valueOf(targetEvent.lat))
                    .appendQueryParameter("longitude", String.valueOf(targetEvent.lon))
                    .appendQueryParameter("title", targetEvent.title)
                    .appendQueryParameter("address", targetEvent.address)
                    .build();

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startShowMap(MapSelectEvent targetEvent) {
        startShowMap(context, targetEvent);
    }

    /**
     * 解析 ShowMap 的数据
     * @param uri Uri
     * @return IMBean
     */
    public MapSelectEvent parseShowMap(Uri uri) {
        if (uri == null) return null;
        int code = uriMatcher.match(uri);
        if (code == -1) return null;

        try {
            final String latitude = uri.getQueryParameter("latitude");
            final String longitude = uri.getQueryParameter("longitude");
            final String title = uri.getQueryParameter("title");
            final String address = uri.getQueryParameter("address");
            return new MapSelectEvent(Double.parseDouble(latitude), Double.parseDouble(longitude), title, address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开用于选择的Map
     * @param context Context
     * @param targetId 目标 group id
     */
    public static void startSelectMap(Context context, ConversationType type, String targetId) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(IMCommonConfig.scheme)
                    .authority(IMCommonConfig.authority)
                    .appendEncodedPath(IMCommonConfig.selectMapPath)
                    .appendPath(context.getPackageName())
                    .appendQueryParameter("type", String.valueOf(type.ordinal()))
                    .appendQueryParameter("targetId", String.valueOf(targetId))
                    .build();

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSelectMap(ConversationType type, String targetId) {
        startSelectMap(context, type, targetId);
    }

    /**
     * 解析 SelectMap 的数据
     * @param uri Uri
     * @return IMBean
     */
    public IMSelectMapData parseSelectMap(Uri uri) {
        if (uri == null) return null;
        int code = uriMatcher.match(uri);
        if (code == -1) return null;

        try {
            final String type = uri.getQueryParameter("type");
            final String targetId = uri.getQueryParameter("targetId");
            return new IMSelectMapData(Integer.parseInt(type), targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class IMSelectMapData {
        public int type;
        public String targetId;

        public IMSelectMapData(int type, String targetId) {
            this.type = type;
            this.targetId = targetId;
        }
    }
}
