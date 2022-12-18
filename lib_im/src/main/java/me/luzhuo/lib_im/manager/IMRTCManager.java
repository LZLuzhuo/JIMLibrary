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

import android.util.Log;

import java.util.Arrays;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jiguang.jmrtc.api.JMSignalingMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import me.luzhuo.lib_im.manager.enums.RTCType;

/**
 * 语音通话管理
 */
public class IMRTCManager {
    private static final String TAG = IMRTCManager.class.getSimpleName();

    public static class IRTCCallback {

        public void onSuccess() {}

        public void onErr(String err) {}
    }
    /**
     * 拨打
     * @param type RTCType
     * @param targetUserName 被呼叫人的 UserName
     */
    public static void call(final RTCType type, final String targetUserName, final IRTCCallback callback) {
        JMessageClient.getUserInfo(targetUserName, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String message, UserInfo userInfo) {
                if (status != 0) {
                    if (callback != null) callback.onErr("获取用户信息失败, 或该用户不存在");
                    Log.e(TAG, "" + message);
                    return;
                }

                JMSignalingMessage.MediaType mediaType;
                if (type == RTCType.AudioPhone) mediaType = JMSignalingMessage.MediaType.AUDIO;
                else /*if (type == RTCType.VideoPhone)*/ mediaType = JMSignalingMessage.MediaType.VIDEO;

                JMRtcClient.getInstance().call(Arrays.asList(userInfo), mediaType, new BasicCallback() {
                    @Override
                    public void gotResult(int status, String message) {
                        if (status == 0) {
                            if (callback != null) callback.onSuccess();
                        } else {
                            if (callback != null) callback.onErr("发起通话失败, 请重试");
                            Log.e(TAG, "" + message);
                        }
                    }
                });
            }
        });
    }
    /**
     * 挂断电话
     */
    public static void called(final IRTCCallback callback) {
        JMRtcClient.getInstance().hangup(new BasicCallback() {
            @Override
            public void gotResult(int status, String message) {
                if (status == 0) {
                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onErr("挂断失败, 请重试");
                    Log.e(TAG, "" + message);
                }
            }
        });
    }

    /**
     * 拒绝被通话
     */
    public static void refuse_called(final IRTCCallback callback) {
        JMRtcClient.getInstance().refuse(new BasicCallback() {
            @Override
            public void gotResult(int status, String message) {
                if (status == 0) {
                    if (callback != null) callback.onSuccess();
                } else {
                    if (callback != null) callback.onErr("拒绝失败, 请重试!");
                    Log.e(TAG, "" + message);
                }
            }
        });
    }

    /**
     * 同意被通话
     */
    public static void agree_called(final IRTCCallback callback) {
        JMRtcClient.getInstance().accept(new BasicCallback() {
             @Override
             public void gotResult(int status, String message) {
                 if (status == 0) {
                     if (callback != null) callback.onSuccess();
                 } else {
                     if (callback != null) callback.onErr("接听失败, 请重试!");
                     Log.e(TAG, "" + message);
                 }
             }
         });
    }

    /**
     * 重新初始化RTC引擎
     * 一般用于第一次使用, 在获取权限后, 进行重新初始化
     */
    public static void reInit() {
        JMRtcClient.getInstance().reinitEngine();
    }
}
