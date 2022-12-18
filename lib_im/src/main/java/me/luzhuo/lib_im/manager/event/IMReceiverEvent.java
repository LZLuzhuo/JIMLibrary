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

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ChatRoomMessageEvent;
import cn.jpush.im.android.api.event.ChatRoomNotificationEvent;
import cn.jpush.im.android.api.event.CommandNotificationEvent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.GroupAnnouncementChangedEvent;
import cn.jpush.im.android.api.event.GroupApprovalEvent;
import cn.jpush.im.android.api.event.GroupApprovalRefuseEvent;
import cn.jpush.im.android.api.event.GroupApprovedNotificationEvent;
import cn.jpush.im.android.api.event.GroupBlackListChangedEvent;
import cn.jpush.im.android.api.event.GroupMemNicknameChangedEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.event.MyInfoUpdatedEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import me.luzhuo.lib_im.manager.event.eventbus.MainEvent;
import me.luzhuo.lib_im.manager.event.eventbus.ReadedEvent;
import me.luzhuo.lib_im.manager.event.eventbus.ReceivedMessageEvent;
import me.luzhuo.lib_im.manager.IMStartUtils;

import static me.luzhuo.lib_im.manager.IMCommonConfig.NeedReadReceipt;
import static me.luzhuo.lib_im.manager.event.eventbus.MainEvent.TypeLogin;
import static me.luzhuo.lib_im.manager.event.eventbus.MainEvent.TypeUnreader;

/**
 * IM 消息接收事件
 * onEventMainThread 在主线程执行
 * onEvent 在子线程执行 (默认子线程)
 * onEventBackgroundThread 在子线程执行
 */
public class IMReceiverEvent {
    private static final String TAG = IMReceiverEvent.class.getSimpleName();
    private Context context;

    public IMReceiverEvent(Context context) {
        this.context = context;
    }

    /**
     * 用户登录状态事件
     */
    public void onEventMainThread(LoginStateChangeEvent event) {
        if (event == null) return;
        Log.e(TAG, "LoginStateChangeEvent");

        String userName = event.getMyInfo().getUserName();
        EventBus.getDefault().post(new MainEvent(TypeLogin, userName));
    }

    /**
     * ※ 通知点击事件
     */
    public void onEvent(NotificationClickEvent event) {
        if (event == null) return;
        Log.e(TAG, "NotificationClickEvent");

        Message msg = event.getMessage();
        if (msg == null) return;

        ConversationType type = msg.getTargetType();
        if (type == ConversationType.single) {
            String targetId = ((UserInfo) msg.getTargetInfo()).getUserName();
            IMStartUtils.startSingleDetail(context, targetId);
        } else if (type == ConversationType.group) {
            long groupId = ((GroupInfo) msg.getTargetInfo()).getGroupID();
            IMStartUtils.startGroupDetail(context, groupId);
        } else if (type == ConversationType.chatroom) {

        }
    }

    /**
     * ※ 收到在线消息
     */
    public void onEvent(MessageEvent event) {
        if (event == null) return;
        Log.e(TAG, "MessageEvent: " + event.getMessage().getTargetType().name());

        Message msg = event.getMessage();
        if (msg == null) return;
        EventBus.getDefault().post(new MainEvent(TypeUnreader, ""));

        if (msg.getTargetType() == ConversationType.single) {
            String targetId = ((UserInfo) msg.getTargetInfo()).getUserName();
            EventBus.getDefault().post(new ReceivedMessageEvent(me.luzhuo.lib_im.manager.enums.ConversationType.Single, targetId, msg));
        } else if (msg.getTargetType() == ConversationType.group) {
            long groupId = ((GroupInfo) msg.getTargetInfo()).getGroupID();
            EventBus.getDefault().post(new ReceivedMessageEvent(me.luzhuo.lib_im.manager.enums.ConversationType.Group, String.valueOf(groupId), msg));
        } else if (msg.getTargetType() == ConversationType.chatroom) { }
    }

    /**
     * ※ 离线消息
     */
    public void onEventMainThread(OfflineMessageEvent event) {
        if (event == null) return;
        Log.e(TAG, "OfflineMessageEvent");

        // 离线会话
        Conversation conv = event.getConversation();
        if (conv.getType() == ConversationType.single) {

        } else if (conv.getType() == ConversationType.group) {

        } else if (conv.getType() == ConversationType.chatroom) {

        }

        // 获取此次离线期间会话收到的新消息列表
        // List<Message> newMessageList = event.getOfflineMessageList();
    }

    /**
     * ※ 消息撤回
     */
    public void onEvent(MessageRetractEvent event) {
        if(event == null) return;
        Log.e(TAG, "MessageRetractEvent");

        Conversation conversation = event.getConversation();
    }

    /**
     * 消息漫游完成事件
     * 漫游完成后, 刷新会话事件
     */
    public void onEventMainThread(ConversationRefreshEvent event) {
        if(event == null) return;
        Log.e(TAG, "ConversationRefreshEvent");
    }

    /**
     * 邀请事件
     */
    public void onEvent(ContactNotifyEvent event) {
        if (event == null) return;
        Log.e(TAG, "ContactNotifyEvent");


        switch (event.getType()) {
            case invite_received: // 收到好友邀请

                break;
            case invite_accepted: // 对方接收了你的好友邀请

                break;
            case invite_declined: // 对方拒绝了你的好友邀请

                break;
            case contact_deleted: // 对方将你从好友中删除

                break;
            case contact_updated_by_dev_api: // 好友关系更新，由api管理员操作引起

                break;
            default:
                break;
        }
    }

    /**
     * ※ 消息已读事件
     */
    public void onEventMainThread(MessageReceiptStatusChangeEvent event) {
        if(event == null || !NeedReadReceipt) return;
        Log.e(TAG, "MessageReceiptStatusChangeEvent: " + event.getConversation().getType().name());

        List<MessageReceiptStatusChangeEvent.MessageReceiptMeta> messageReceiptMetas = event.getMessageReceiptMetas();

        Conversation conv = event.getConversation();
        if (conv.getType() == ConversationType.single) {
            for (MessageReceiptStatusChangeEvent.MessageReceiptMeta meta : messageReceiptMetas) {
                EventBus.getDefault().post(new ReadedEvent(me.luzhuo.lib_im.manager.enums.ConversationType.Single, meta.getServerMsgId(), meta.getUnReceiptCnt()));
            }
        } else if (conv.getType() == ConversationType.group) {
            for (MessageReceiptStatusChangeEvent.MessageReceiptMeta meta : messageReceiptMetas) {
                EventBus.getDefault().post(new ReadedEvent(me.luzhuo.lib_im.manager.enums.ConversationType.Group, meta.getServerMsgId(), meta.getUnReceiptCnt()));
            }
        } else if (conv.getType() == ConversationType.chatroom) { }
    }

    // ============================================= 群组 ↓ =============================================
    /**
     * 群公告更新
     */
    public void onEvent(GroupAnnouncementChangedEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupAnnouncementChangedEvent");


        StringBuilder builder = new StringBuilder();
        builder.append("群组ID:").append(event.getGroupID()).append("\n\n");
        for (GroupAnnouncementChangedEvent.ChangeEntity entity : event.getChangeEntities()) {
            builder.append("类型:").append(entity.getType().toString()).append("\n");
            builder.append("发起者(username):").append(entity.getFromUserInfo().getUserName()).append("\n");
            builder.append("内容:").append(entity.getAnnouncement().toJson()).append("\n");
            builder.append("时间:").append(entity.getCtime()).append("\n\n");
        }
    }

    /**
     * 群黑名单更新
     * @param event
     */
    public void onEvent(GroupBlackListChangedEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupBlackListChangedEvent");


        StringBuilder builder = new StringBuilder();
        builder.append("群组ID:").append(event.getGroupID()).append("\n\n");
        for (GroupBlackListChangedEvent.ChangeEntity entity : event.getChangeEntities()) {
            builder.append("类型:").append(entity.getType().toString()).append("\n");
            builder.append("操作者(username):").append(entity.getOperator().getUserName()).append("\n");
            builder.append("被操作的用户(username):\n");
            for (UserInfo userInfo : entity.getUserInfos()) {
                builder.append(userInfo.getUserName()).append(" ");
            }
            builder.append("\n");
            builder.append("时间:").append(entity.getCtime()).append("\n\n");
        }
    }

    /**
     * 群昵称更新
     */
    public void onEvent(GroupMemNicknameChangedEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupMemNicknameChangedEvent");

    }

    /**
     * 入群审批事件
     * @param event
     */
    public void onEventMainThread(GroupApprovalEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupApprovalEvent");

    }

    /**
     * 入群审批通过事件
     * @param event
     */
    public void onEventMainThread(GroupApprovedNotificationEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupApprovedNotificationEvent");

    }

    /**
     * 入群审批拒绝事件
     * @param event
     */
    public void onEvent(GroupApprovalRefuseEvent event) {
        if(event == null) return;
        Log.e(TAG, "GroupApprovalRefuseEvent");

    }
    // ============================================= 群组 ↑ =============================================

    // ============================================= 聊天室 ↓ =============================================
    /**
     * 接收聊天室消息
     */
    public void onEventMainThread(ChatRoomMessageEvent event) {
        if(event == null) return;
        Log.e(TAG, "ChatRoomMessageEvent");

    }

    /**
     * 聊天室通知
     */
    public void onEventBackgroundThread(ChatRoomNotificationEvent event) {
        if(event == null) return;
        Log.e(TAG, "ChatRoomNotificationEvent");

    }
    // ============================================= 聊天室 ↑ =============================================

    // ============================================= 其他事件与未知事件 ↓ =============================================
    /**
     * 个人信息更新
     */
    public void onEvent(MyInfoUpdatedEvent event) {
        if(event == null) return;
        Log.e(TAG, "MyInfoUpdatedEvent");

    }

    /**
     * 命令事件
     */
    public void onEvent(final CommandNotificationEvent event) {
        if(event == null) return;
        Log.e(TAG, "CommandNotificationEvent");

    }

}
