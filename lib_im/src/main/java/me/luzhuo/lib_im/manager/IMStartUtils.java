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

public class IMStartUtils {
    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private Context context;

    public IMStartUtils(Context context) {
        this.context = context;

        // init match
        final String packageName = context.getPackageName();
        uriMatcher.addURI(IMCommonConfig.authority, IMCommonConfig.singlePath + "/" + packageName, 0);
        uriMatcher.addURI(IMCommonConfig.authority, IMCommonConfig.groupPath + "/" + packageName, 1);
    }

    /**
     * 打开单聊详情页
     * @param targetId 目标 username
     */
    public static void startSingleDetail(Context context, String targetId) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(IMCommonConfig.scheme)
                    .authority(IMCommonConfig.authority)
                    .appendPath(IMCommonConfig.singlePath)
                    .appendPath(context.getPackageName())
                    .appendQueryParameter("targetId", targetId)
                    .build();

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSingleDetail(String targetId) {
        startSingleDetail(context, targetId);
    }

    /**
     * 解析用户详情数据
     * @param uri Uri
     * @return IMBean
     */
    public String parseSingleDetail(Uri uri) {
        if (uri == null) return null;
        int code = uriMatcher.match(uri);
        if (code == -1) return null;

        try {
            final String targetId = uri.getQueryParameter("targetId");
            return targetId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开群详情
     * @param context Context
     * @param targetId 目标 group id
     */
    public static void startGroupDetail(Context context, long targetId) {
        try {
            Uri uri = new Uri.Builder()
                    .scheme(IMCommonConfig.scheme)
                    .authority(IMCommonConfig.authority)
                    .appendPath(IMCommonConfig.groupPath)
                    .appendPath(context.getPackageName())
                    .appendQueryParameter("targetId", String.valueOf(targetId))
                    .build();

            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startGroupDetail(long targetId) {
        startGroupDetail(context, targetId);
    }

    /**
     * 解析群详情数据
     * @param uri Uri
     * @return IMBean
     */
    public long parseGroupDetail(Uri uri) {
        if (uri == null) return 0;
        int code = uriMatcher.match(uri);
        if (code == -1) return 0;

        try {
            final String targetId = uri.getQueryParameter("targetId");
            return Long.parseLong(targetId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
