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

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.ChatRoomManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.ChatRoomInfo;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import me.luzhuo.lib_im.manager.enums.ConversationType;

/**
 * 会话信息
 */
public class IMConversationManager {

    /**
     * 获取会话列表
     *
     * 从本地数据库中获取会话列表，默认按照会话的最后一条消息的时间，降序排列
     *
     * 会话类型判断的案例:
     * if(con.getType().equals(ConversationType.chatroom))
     *
     * 会话类型:
     * ConversationType.group 群聊
     *      long groupId = ((GroupInfo) conv.getTargetInfo()).getGroupID();
     * ConversationType.single 单聊
     *      String targetId = ((UserInfo) conv.getTargetInfo()).getUserName();
     *      String targeAppkey = conv.getTargetAppKey()
     * ConversationType.chatroom 聊天室
     *
     * @return 可能返回 null
     */
    public static List<Conversation> getConversationList(){
        return JMessageClient.getConversationList();
    }

    public static List<Conversation> getConversationList(ConversationType type){
        List<Conversation> cons = new ArrayList<>();
        List<Conversation> conversations = getConversationList();
        for (Conversation conversation : conversations) {
            if (type == ConversationType.Single) {
                if(conversation.getType() == cn.jpush.im.android.api.enums.ConversationType.single) cons.add(conversation);
            } else if (type == ConversationType.Group) {
                if(conversation.getType() == cn.jpush.im.android.api.enums.ConversationType.group) cons.add(conversation);
            } else if (type == ConversationType.ChatRoom) {
                if(conversation.getType() == cn.jpush.im.android.api.enums.ConversationType.chatroom) cons.add(conversation);
            }
        }
        return cons;
    }

    /**
     * 根据目标id获取相应的会话, 如果不存在该会话, 会创建
     * @param targetId im username or group id
     * @return Conversation
     */
    public static Conversation getConversation(ConversationType type, String targetId, String appkey) {
        Conversation conv;
        if(type == ConversationType.Single) {
            conv = JMessageClient.getSingleConversation(targetId, IMSendMessager.getAppkey(appkey));
            if(conv == null) conv = Conversation.createSingleConversation(targetId, IMSendMessager.getAppkey(appkey));
        } else if (type == ConversationType.Group) {
            conv  = JMessageClient.getGroupConversation(Long.parseLong(targetId));
            if(conv == null) conv = Conversation.createGroupConversation(Long.parseLong(targetId));
        } else {
            return null;
        }
        return conv;
    }

    public static Conversation getConversation(ConversationType type, String targetId) {
        return getConversation(type, targetId, null);
    }

    /**
     * 获取给会话的id
     * @param conv Conversation
     * @return 目标id or 目标userName
     */
    public static String getTargetId(Conversation conv) {
        String targetId;
        if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.single) {
            String userName = ((UserInfo) conv.getTargetInfo()).getUserName();
            targetId = userName;
        } else if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.group) {
            long groupId = ((GroupInfo) conv.getTargetInfo()).getGroupID();
            targetId = String.valueOf(groupId);
        } else if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.chatroom) {
            long roomId = ((ChatRoomInfo) conv.getTargetInfo()).getRoomID();
            targetId = String.valueOf(roomId);
        } else {
            throw new IllegalStateException("Conversation type do not match.");
        }
        return targetId;
    }

    /**
     * 删除会话
     * 同时删除本地的聊天记录
     *
     * String userName = ((UserInfo) conv.getTargetInfo()).getUserName();
     * long groupId = ((GroupInfo) conv.getTargetInfo()).getGroupID()
     * long roomId = ((ChatRoomInfo) conv.getTargetInfo()).getRoomID();
     * @param targetId 单聊为userName
     */
    public static void deleteConversion(ConversationType type, String targetId, String appkey){
        if (type == ConversationType.Single) JMessageClient.deleteSingleConversation(targetId, IMSendMessager.getAppkey(appkey));
        else if (type == ConversationType.Group) JMessageClient.deleteGroupConversation(Long.parseLong(targetId));
        else if (type == ConversationType.ChatRoom) ChatRoomManager.leaveChatRoom(Long.parseLong(targetId), null);
        else throw new IllegalStateException("ConversationType parameters do not match.");
    }

    public static void deleteConversion(ConversationType type, String targetId){
        deleteConversion(type, targetId, null);
    }

    /**
     * 删除会话
     * 同时删除本地的聊天记录
     */
    public static void deleteConversion(Conversation conv, String appkey){
        String targetId = getTargetId(conv);
        if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.single) deleteConversion(ConversationType.Single, targetId, appkey);
        else if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.group) deleteConversion(ConversationType.Group, targetId, appkey);
        else if (conv.getType() == cn.jpush.im.android.api.enums.ConversationType.chatroom) deleteConversion(ConversationType.ChatRoom, targetId, appkey);
    }

    public static void deleteConversion(Conversation conv){
        deleteConversion(conv, null);
    }

    /**
     * 进入会话
     * 调用该函数后, 收到对应用户发来的消息时, 不会弹出通知栏提示
     * 同时清掉该会话的未读消息数, 以及通知栏通知
     * @param type
     * @param targetId
     */
    public static void enterConversation(ConversationType type, String targetId){
        // 进入聊天, 停止接收该会话的消息提醒
        if (type == ConversationType.Single) JMessageClient.enterSingleConversation(targetId);
        else if (type == ConversationType.Group) JMessageClient.enterGroupConversation(Long.parseLong(targetId));
        else if (type == ConversationType.ChatRoom) return;
        else throw new IllegalStateException("ConversationType parameters do not match.");
    }

    /**
     * 亮屏等进入
     */
    public static void enterConversationTemp(ConversationType type, String targetId){
        enterConversation(type, targetId);
    }

    /**
     * 退出当前会话
     *
     * 如果没有聊天消息, 会删除该会话
     * @param targetId 单聊为username, 群聊为groupId, 聊天室为targetId
     */
    public static void exitConversation(ConversationType type, String targetId){
        Conversation conv = getConversation(type, targetId);

        // 退出聊天, 开始接收消息提醒
        JMessageClient.exitConversation();
        // 清空未读消息数
        clearUnread(conv);

        if (conv.getAllMessage() == null || conv.getAllMessage().size() <= 0) {
            if (type == ConversationType.Single) JMessageClient.deleteSingleConversation(targetId);
            else if(type == ConversationType.Group) JMessageClient.deleteGroupConversation(Integer.parseInt(targetId));
        }
        if(type == ConversationType.ChatRoom) ChatRoomManager.leaveChatRoom(Long.parseLong(targetId), null);
    }

    /**
     * 息屏等临时退出
     */
    public static void exitConversationTemp(){
        JMessageClient.exitConversation();
    }

    /**
     * 获取会话标题
     * 如果是单聊, 则获取对方用户名
     * 如果是群聊, 则获取群名
     * @param targetId user name / group id
     * @param appkey 如果对方是其他应用的, 则需要提供 AppKey
     * @return 用户昵称 / 群名称
     */
    public static String getConversationTitle(ConversationType type, String targetId, String appkey) {
        if (TextUtils.isEmpty(targetId)){
            if (type == ConversationType.Single) return "单聊";
            else if (type == ConversationType.Group) return "群聊";
            else if (type == ConversationType.ChatRoom) return "聊天室";
        }

        Conversation conv = getConversation(type, targetId, appkey);
        return conv.getTitle();
    }

    public static String getConversationTitle(ConversationType type, String targetId) {
        return getConversationTitle(type, targetId, null);
    }

    // ============================================= 未读消息数 ↓ =============================================
    /**
     * 获取所有未读消息数
     */
    public static int getAllUnread() {
        return JMessageClient.getAllUnReadMsgCount();
    }

    /**
     * 获取该会话的未读消息数
     */
    public static int getUnread(Conversation conv){
        return conv.getUnReadMsgCnt();
    }

    /**
     * 清空该会话的未读消息数
     */
    public static void clearUnread(Conversation conv){
        conv.resetUnreadCount();
    }
    // ============================================= 未读消息数 ↑ =============================================
}
