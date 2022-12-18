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
package me.luzhuo.lib_im.manager.event;

import android.util.Log;
import android.view.SurfaceView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.jiguang.jmrtc.api.JMRtcClient;
import cn.jiguang.jmrtc.api.JMRtcListener;
import cn.jiguang.jmrtc.api.JMRtcSession;
import cn.jpush.im.android.api.callback.RequestCallback;
import cn.jpush.im.android.api.model.UserInfo;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_im.main.detail.rtc.VoicePhoneActivity;
import me.luzhuo.lib_im.manager.event.eventbus.RTCEvent;

/**
 * RTC事件:
 * // ================= 未接通 =================
 * 接到通话邀请
 * E/IMRTCEvent: callSession
 *
 * 未接通, 对方挂断
 * E/IMRTCEvent: onCallMemberOffline (无法再次呼叫, 显示对方挂断了电话)
 * E/IMRTCEvent: onCallDisconnected (主动挂断电话, 解决上述问题)
 *
 * 未通话, 主动拒绝
 * E/IMRTCEvent: onCallDisconnected
 *
 * 未通话, 同意接通
 * E/IMRTCEvent: onCallConnected
 * E/IMRTCEvent: onCallMemberJoin
 *
 * // ================= 被呼叫 =================
 *
 * 通话中, 主动挂断
 * E/IMRTCEvent: onCallDisconnected
 *
 * 通话中, 对方挂断
 * E/IMRTCEvent: onCallMemberOffline (无法再次呼叫, 显示对方挂断了电话)
 * E/IMRTCEvent: onCallDisconnected (主动挂断电话, 解决上述问题)
 *
 * // ================= 主动呼叫 =================
 *
 * 对外呼叫
 * E/IMRTCEvent: onCallOutgoing
 *
 * 未通话, 对方拒接
 * E/IMRTCEvent: onCallMemberOffline (无法再次呼叫, 显示对方挂断了电话)
 * E/IMRTCEvent: onCallDisconnected (主动挂断电话, 解决上述问题)
 *
 * 未通话, 对方接听
 * E/IMRTCEvent: onCallConnected
 * E/IMRTCEvent: onCallMemberJoin
 *
 * 通话中, 对方挂断
 * E/IMRTCEvent: onCallMemberOffline (无法再次呼叫, 显示对方挂断了电话)
 * E/IMRTCEvent: onCallDisconnected (主动挂断电话, 解决上述问题)
 *
 * 通话中, 主动挂断
 * E/IMRTCEvent: onCallDisconnected
 */
public class IMRTCEvent extends JMRtcListener {
    private static final String TAG = IMRTCEvent.class.getSimpleName();

    public void onEngineInitComplete(int i, String s) {
        if(i == 0) Log.e(TAG, "语音通话引擎初始化完成");
        else Log.e(TAG, "语音通话引擎未完成初始化");
    }

    public void onCallOutgoing(JMRtcSession var1) {
        // 主动对外呼叫
        Log.e(TAG, "onCallOutgoing");
    }

    public void onCallInviteReceived(JMRtcSession callSession) {
        // 收到通话邀请
        Log.e(TAG, "callSession");
        callSession.getInviterUserInfo(new RequestCallback<UserInfo>() {
            @Override
            public void gotResult(int status, String message, UserInfo userInfo) {
                VoicePhoneActivity.receive(CoreBaseApplication.context, userInfo.getUserName());
            }
        });
    }

    public void onCallOtherUserInvited(UserInfo var1, List<UserInfo> var2, JMRtcSession var3) {
        // 通话中邀请其他用户
        Log.e(TAG, "onCallOtherUserInvited");
    }

    public void onCallConnected(JMRtcSession jmRtcSession, SurfaceView surfaceView) {
        // 同意接通通话
        Log.e(TAG, "onCallConnected");
        EventBus.getDefault().post(new RTCEvent(RTCEvent.Calling));
    }

    public void onCallMemberJoin(UserInfo userInfo, SurfaceView surfaceView) {
        // 加入通话
        Log.e(TAG, "onCallMemberJoin");
    }

    public void onCallMemberOffline(UserInfo userInfo, JMRtcClient.DisconnectReason disconnectReason) {
        // 被对方 挂断 / 拒接 电话
        Log.e(TAG, "onCallMemberOffline");
        EventBus.getDefault().post(new RTCEvent(RTCEvent.Called));
    }

    public void onCallDisconnected(JMRtcClient.DisconnectReason var1) {
        // 主动 挂断 / 拒接 电话
        Log.e(TAG, "onCallDisconnected");
        EventBus.getDefault().post(new RTCEvent(RTCEvent.Calleding));
    }

    public void onCallError(int i, String s) {
        // 通话错误
        Log.e(TAG, "onCallError");
    }

    public void onPermissionNotGranted(String[] ss) {
        // 需要权限
        // 需要这些权限 Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.MODIFY_AUDIO_SETTINGS
        Log.e(TAG, "onPermissionNotGranted");
    }

    public void onRemoteVideoMuted(UserInfo userInfo, boolean isRemote) {
        // 视频通话静音
        Log.e(TAG, "onRemoteVideoMuted");
    }
}
