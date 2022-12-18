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
import android.text.TextUtils;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jpush.im.android.api.JMessageClient;
import me.luzhuo.lib_core.app.appinfo.AppManager;
import me.luzhuo.lib_im.manager.event.IMRTCEvent;
import me.luzhuo.lib_im.manager.event.IMReceiverEvent;

public class IMManager {
    /**
     * 请在Application里初始化
     */
    public static void init(Context context) {
        // debug message
        if(new AppManager().isDebug(context)) JMessageClient.setDebugMode(true);



        // 开启消息记录漫游
        JMessageClient.init(context.getApplicationContext(), true);



        // 开启语音视频通话
        JMRtcClient.getInstance().setVideoProfile(JMRtcClient.VideoProfile.Profile_720P);
        JMRtcClient.getInstance().initEngine(new IMRTCEvent());



        // 注册事件接收
        JMessageClient.registerEventReceiver(new IMReceiverEvent(context.getApplicationContext()));



        /* Notification
         * 设置Notification模式: 声音 + 呼吸灯 + 震动
         *
         * 其他参数:
         * FLAG_NOTIFY_DEFAULT 展示通知栏通知，所有设置均默认打开。
         * FLAG_NOTIFY_SILENCE 展示通知栏通知,其他设置均为关闭。
         * FLAG_NOTIFY_DISABLE 不展示通知栏通知
         * FLAG_NOTIFY_WITH_SOUND 收到通知时，发出声音
         * FLAG_NOTIFY_WITH_VIBRATE 收到通知时，产生震动
         * FLAG_NOTIFY_WITH_LED 收到通知时，点亮呼吸灯
         * */
        JMessageClient.setNotificationFlag(JMessageClient.FLAG_NOTIFY_DEFAULT);
    }
}
