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
package me.luzhuo.lib_im.main.detail.rtc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import me.luzhuo.lib_core.app.base.CoreBaseActivity;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.date.Timer;
import me.luzhuo.lib_core.date.callback.ITimerCallback;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.manager.IMRTCManager;
import me.luzhuo.lib_im.manager.IMSendMessager;
import me.luzhuo.lib_im.manager.IMUserManager;
import me.luzhuo.lib_im.manager.enums.ConversationType;
import me.luzhuo.lib_im.manager.enums.RTCType;
import me.luzhuo.lib_im.manager.event.eventbus.RTCEvent;
import me.luzhuo.lib_im.manager.event.eventbus.RTCSendMessageEvent;
import me.luzhuo.lib_im.utils.MediaNotificationUtils;

public class VoicePhoneActivity extends CoreBaseActivity implements View.OnClickListener {
    // 发起人
    private boolean callinger;
    // 目标人的极光username
    private String targetUserName;

    private ImageView im_voice_phone_header;
    private TextView im_voice_phone_name, im_voice_phone_calling_time;
    private View im_voice_phone_called_tip, im_voice_phone_called_iv, im_voice_phone_called_tv, im_voice_phone_call_refuse_iv, im_voice_phone_call_refuse_tv, im_voice_phone_call_agree_iv, im_voice_phone_call_agree_tv;

    private MediaNotificationUtils mediaNotification;
    private Timer timer = new Timer();
    // 是否为被对方挂断电话
    private boolean isCalled = false;

    /**
     * 拨打
     */
    public static void call(Context context, String targetUserName) {
        Intent intent = new Intent(context, VoicePhoneActivity.class);
        intent.putExtra("callinger", true);
        intent.putExtra("targetUserName", targetUserName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 接听
     */
    public static void receive(Context context, String targetUserName) {
        Intent intent = new Intent(context, VoicePhoneActivity.class);
        intent.putExtra("callinger", false);
        intent.putExtra("targetUserName", targetUserName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.im_activity_voice_phone);
        if(!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this);
        getIntentData();
        initView();
        initUserView();

        if(callinger) callPhone();
        else calledPhone();
    }

    @Override
    protected void onDestroy() {
        try {
            mediaNotification.stop();
        } catch (IOException e) { e.printStackTrace(); }
        finally {
            isCalled = false;
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    private void getIntentData() {
        final Intent intent = getIntent();
        callinger = intent.getBooleanExtra("callinger", false);
        targetUserName = intent.getStringExtra("targetUserName");

        try {
            mediaNotification = new MediaNotificationUtils(this);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void initView() {
        im_voice_phone_calling_time = findViewById(R.id.im_voice_phone_calling_time);
        im_voice_phone_called_tip = findViewById(R.id.im_voice_phone_called_tip);
        im_voice_phone_called_iv = findViewById(R.id.im_voice_phone_called_iv);
        im_voice_phone_called_tv = findViewById(R.id.im_voice_phone_called_tv);
        im_voice_phone_call_refuse_iv = findViewById(R.id.im_voice_phone_call_refuse_iv);
        im_voice_phone_call_refuse_tv = findViewById(R.id.im_voice_phone_call_refuse_tv);
        im_voice_phone_call_agree_iv = findViewById(R.id.im_voice_phone_call_agree_iv);
        im_voice_phone_call_agree_tv = findViewById(R.id.im_voice_phone_call_agree_tv);

        im_voice_phone_header = findViewById(R.id.im_voice_phone_header);
        im_voice_phone_name = findViewById(R.id.im_voice_phone_name);

        im_voice_phone_calling_time.setVisibility(View.INVISIBLE);
        if (callinger) {
            im_voice_phone_called_tip.setVisibility(View.INVISIBLE);

            im_voice_phone_called_iv.setVisibility(View.VISIBLE);
            im_voice_phone_called_tv.setVisibility(View.VISIBLE);

            im_voice_phone_call_refuse_iv.setVisibility(View.INVISIBLE);
            im_voice_phone_call_refuse_tv.setVisibility(View.INVISIBLE);
            im_voice_phone_call_agree_iv.setVisibility(View.INVISIBLE);
            im_voice_phone_call_agree_tv.setVisibility(View.INVISIBLE);
        } else {
            im_voice_phone_called_tip.setVisibility(View.VISIBLE);

            im_voice_phone_called_iv.setVisibility(View.INVISIBLE);
            im_voice_phone_called_tv.setVisibility(View.INVISIBLE);

            im_voice_phone_call_refuse_iv.setVisibility(View.VISIBLE);
            im_voice_phone_call_refuse_tv.setVisibility(View.VISIBLE);
            im_voice_phone_call_agree_iv.setVisibility(View.VISIBLE);
            im_voice_phone_call_agree_tv.setVisibility(View.VISIBLE);
        }

        im_voice_phone_called_iv.setOnClickListener(this);
        im_voice_phone_call_refuse_iv.setOnClickListener(this);
        im_voice_phone_call_agree_iv.setOnClickListener(this);
    }

    private void initUserView() {
        // 根据 username 获取用户信息
        IMUserManager.getUserInfo(targetUserName, new IMUserManager.UserInfoCallback() {
            @Override
            public void onUserInfo(UserInfo userInfo) {
                Glide.with(VoicePhoneActivity.this).load(userInfo.getAvatarFile()).error(R.drawable.im_default).into(im_voice_phone_header);
                im_voice_phone_name.setText(userInfo.getNickname());
            }
            @Override
            public void onError(String err) {
                ToastManager.show(CoreBaseApplication.context, err);
            }
        });
    }

    /**
     * 拨打电话的界面
     */
    private void callPhone() {
        IMRTCManager.call(RTCType.AudioPhone, targetUserName, new IMRTCManager.IRTCCallback(){
            @Override
            public void onErr(String err) {
                ToastManager.show(VoicePhoneActivity.this, err);
                finish();
            }
        });
    }

    /**
     * 被呼叫的界面
     */
    private void calledPhone() {
        mediaNotification.start();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.im_voice_phone_called_iv) {
            // 被呼叫
            IMRTCManager.called(new IMRTCManager.IRTCCallback() {
                @Override
                public void onErr(String err) {
                    ToastManager.show(CoreBaseApplication.context, err);
                }
            });
        } else if (id == R.id.im_voice_phone_call_refuse_iv) {
            // 拒绝接听
            IMRTCManager.refuse_called(new IMRTCManager.IRTCCallback() {
                @Override
                public void onSuccess() {
                    try {
                        mediaNotification.stop();
                    } catch (IOException e) { e.printStackTrace(); }
                    finally {
                        finish();
                    }
                }
                @Override
                public void onErr(String err) {
                    ToastManager.show(CoreBaseApplication.context, err);
                }
            });
        } else if (id == R.id.im_voice_phone_call_agree_iv) {
            // 同意接听
            IMRTCManager.agree_called(new IMRTCManager.IRTCCallback() {
                @Override
                public void onSuccess() {
                    try {
                        mediaNotification.stop();
                    } catch (IOException e) { e.printStackTrace(); }
                }
                @Override
                public void onErr(String err) {
                    ToastManager.show(CoreBaseApplication.context, err);
                }
            });
        }
    }

    @Override
    public void onBackPressed() { }

    // ======================================== 事件接收 ========================================

    private static final String TAG = VoicePhoneActivity.class.getSimpleName();
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(RTCEvent rtcEvent) {
        if (rtcEvent.type == RTCEvent.Calling) {
            callingView();
        } else if (rtcEvent.type == RTCEvent.Calleding) {
            long endTimer = timer.endTimer();

            sendCustomMessage(endTimer);

            finish();
        } else if (rtcEvent.type == RTCEvent.Called) {
            isCalled = true;
            IMRTCManager.called(null);

            ToastManager.show(CoreBaseApplication.context, "对方挂断了电话!");
            finish();
        }
    }

    private void callingView() {
        im_voice_phone_called_tip.setVisibility(View.VISIBLE);
        im_voice_phone_calling_time.setVisibility(View.VISIBLE);

        im_voice_phone_called_iv.setVisibility(View.VISIBLE);
        im_voice_phone_called_tv.setVisibility(View.VISIBLE);
        im_voice_phone_call_refuse_iv.setVisibility(View.INVISIBLE);
        im_voice_phone_call_refuse_tv.setVisibility(View.INVISIBLE);
        im_voice_phone_call_agree_iv.setVisibility(View.INVISIBLE);
        im_voice_phone_call_agree_tv.setVisibility(View.INVISIBLE);

        // 开始计时
        timer.startTimer(new ITimerCallback(){
            @Override
            public void onTask(String excuteTime) {
                im_voice_phone_calling_time.setText(excuteTime);
            }
        });
    }

    private Map<String, String> strings = new HashMap<>();
    private Map<String, Integer> ints = new HashMap<>();
    /**
     * 发送自定义消息, 告知对方通话结果
     */
    private void sendCustomMessage(final long endTimer) {
        strings.clear();
        ints.clear();
        String timeFormat = String.format(" %02d:%02d", endTimer / 60, endTimer % 60);

        if (callinger) { // 主动呼叫
            if (endTimer > 0) { // 有进行过通话
                if (isCalled) { // 被对方挂断电话
                    Log.e(TAG, "主动呼叫 - 有进行过通话 - 被对方挂断电话");
                    strings.put("ws_voiceCallTime", timeFormat);
                    strings.put("content_text", "通话时长 ".concat(timeFormat));
                    ints.put("ws_type", 2);
                    ints.put("ws_voiceCallType", 3);
                } else { // 主动挂断电话
                    Log.e(TAG, "主动呼叫 - 有进行过通话 - 主动挂断电话");
                    strings.put("ws_voiceCallTime", timeFormat);
                    strings.put("content_text", "通话时长 ".concat(timeFormat));
                    ints.put("ws_type", 2);
                    ints.put("ws_voiceCallType", 3);
                }
            } else { // 未进行过通话
                if (isCalled) { // 被对方拒接电话
                    Log.e(TAG, "主动呼叫 - 未进行过通话 - 被对方拒接电话");
                } else { // 主动取消通话
                    Log.e(TAG, "主动呼叫 - 未进行过通话 - 主动取消通话");
                    strings.put("ws_voiceCallTime", "");
                    strings.put("content_text", "已取消");
                    ints.put("ws_type", 2);
                    ints.put("ws_voiceCallType", 2);
                }
            }
        } else { // 被呼叫
            if (endTimer > 0) {
                // 有进行过通话
                if (isCalled) { // 被对方挂断电话
                    Log.e(TAG, "被动呼叫 - 有进行过通话 - 被对方挂断电话");
                } else { // 主动挂断电话
                    Log.e(TAG, "被动呼叫 - 有进行过通话 - 主动挂断电话");
                }
            } else { // 未进行过通话
                if (isCalled) { // 被对方取消通话
                    Log.e(TAG, "被动呼叫 - 未进行过通话 - 被对方取消通话");
                } else { // 主动拒接电话
                    Log.e(TAG, "被动呼叫 - 未进行过通话 - 主动拒接电话");
                    strings.put("ws_voiceCallTime", "timeFormat");
                    strings.put("content_text", "已拒绝");
                    ints.put("ws_type", 2);
                    ints.put("ws_voiceCallType", 1);
                }
            }
        }

        if (strings.size() <= 0 && ints.size() <= 0) return;
        IMSendMessager.sendCustom(ConversationType.Single, targetUserName, strings, ints, null, new IMSendMessager.IMessageCallback() {
            @Override
            public void onMessage(Message message) {
                EventBus.getDefault().post(new RTCSendMessageEvent(callinger, isCalled, endTimer, message));
            }
        }, null);
    }
}
