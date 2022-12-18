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

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VideoContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;
import me.luzhuo.lib_core.MediaManager;
import me.luzhuo.lib_im.manager.enums.ConversationType;

import static me.luzhuo.lib_im.manager.IMCommonConfig.NeedReadReceipt;

/**
 * 发送消息
 */
public class IMSendMessager {
    private static MediaManager mediaManager = new MediaManager();

    public interface SendMessageCallback{
        public void onOK();
        public void onErr(int code, String errMessage);
    }

    public interface IMessageCallback {
        /**
         创建消息的回调
         大部分消息都是同步创建的, 只有Image消息是异步创建的
         */
        public void onMessage(Message message);
    }

    // =========================================== 发送会话消息 ↓ ===========================================

    /**
     * 发送文本信息
     * @param userid im username
     * @param appkey 给其他应用发消息需要的 AppKey; 只有单聊才能给其他应用发, 其他类型的均不可以
     * @param content Content
     * @param iMessageCallback IMessageCallback
     * @param callback SendMessageCallback
     */
    public static void sendText(ConversationType type, String userid, String appkey, String content, IMessageCallback iMessageCallback, SendMessageCallback callback){
        if(TextUtils.isEmpty(content)) {
            if(callback != null) callback.onErr(-1, "发送的文本消息为空");
            return;
        }

        TextContent textContent = new TextContent(content);
        sendMessage(type, userid, appkey, textContent, iMessageCallback, callback);
    }

    public static void sendText(ConversationType type, String userid, String content, IMessageCallback iMessageCallback, SendMessageCallback callback){
        sendText(type, userid, null, content, iMessageCallback, callback);
    }

    /**
     * 发送图片消息
     * @param userid im username
     * @param imagePath image path
     * @param callback SendMessageCallback
     */
    public static void sendImage(final ConversationType type, final String userid, final String appkey, final String imagePath, final IMessageCallback iMessageCallback, final SendMessageCallback callback) {
        File imageFile = new File(imagePath);
        if(TextUtils.isEmpty(imagePath) || !imageFile.exists()){
            if(callback != null) callback.onErr(-1, "图片文件不存在");
            return;
        }

        ImageContent.createImageContentAsync(imageFile, new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String s, ImageContent imageContent) {
                if (responseCode == 0) {
                    sendMessage(type, userid, appkey, imageContent, iMessageCallback, callback);
                } else {
                    if(callback != null) callback.onErr(responseCode, s);
                }
            }
        });
    }

    public static void sendImage(final ConversationType type, final String userid, final String imagePath, final IMessageCallback iMessageCallback, final SendMessageCallback callback) {
        sendImage(type, userid, null, imagePath, iMessageCallback, callback);
    }

    /**
     * 发送语音
     * @param userid im username
     * @param voicePath 语音文件
     * @param duration 语音时长
     * @param callback SendMessageCallback
     * @throws FileNotFoundException
     */
    public static void sendVoice(ConversationType type, String userid, String appkey, String voicePath, int duration, IMessageCallback iMessageCallback, SendMessageCallback callback) throws FileNotFoundException {
        File voiceFile = new File(voicePath);
        if(TextUtils.isEmpty(voicePath) || !voiceFile.exists()){
            if(callback != null) callback.onErr(-1, "图片文件不存在");
            return;
        }

        VoiceContent voiceContent = new VoiceContent(voiceFile, duration);
        sendMessage(type, userid, appkey, voiceContent, iMessageCallback, callback);
    }

    public static void sendVoice(ConversationType type, String userid, String voicePath, int duration, IMessageCallback iMessageCallback, SendMessageCallback callback) throws FileNotFoundException {
        sendVoice(type, userid, null, voicePath, duration, iMessageCallback, callback);
    }

    /**
     * 发送视频
     * @param userid im username
     * @param videoPath 视频文件路径
     * @param callback SendMessageCallback
     * @throws FileNotFoundException
     * @throws JMFileSizeExceedException
     */
    public static void sendVideo(ConversationType type, String userid, String appkey, String videoPath, int duration, IMessageCallback iMessageCallback, SendMessageCallback callback) throws IOException {
        File videoFile = new File(videoPath);
        if(TextUtils.isEmpty(videoPath) || !videoFile.exists()){
            if(callback != null) callback.onErr(-1, "视频文件不存在");
            return;
        }

        /*
         * thumbImg - 视频缩略图，可以为空
         * thumbFormat - 视频缩略图格式，可以不填
         * videoFile - 视频文件对象
         * videoFileName - 视频文件自定义文件名
         * duration - 视频时长
         */
        final Bitmap bitmap = mediaManager.getVideoThumbnail(videoPath);
        VideoContent videoContent = new VideoContent(bitmap, ".png", videoFile, UUID.randomUUID().toString().replace("-", ""), duration);
        sendMessage(type, userid, appkey, videoContent, iMessageCallback, callback);
    }

    public static void sendVideo(ConversationType type, String userid, String videoPath, int duration, IMessageCallback iMessageCallback, SendMessageCallback callback) throws IOException {
        sendVideo(type, userid, null, videoPath, duration, iMessageCallback, callback);
    }

    /**
     * 发送文件
     * @param userid im username
     * @param filePath 文件路径
     * @param callback SendMessageCallback
     * @throws FileNotFoundException
     * @throws JMFileSizeExceedException
     */
    public static void sendFile(ConversationType type, String userid, String appkey, String filePath, IMessageCallback iMessageCallback, SendMessageCallback callback) throws FileNotFoundException, JMFileSizeExceedException {
        File fileFile = new File(filePath);
        if(TextUtils.isEmpty(filePath) || !fileFile.exists()){
            if(callback != null) callback.onErr(-1, "文件不存在");
            return;
        }

        FileContent fileContent = new FileContent(fileFile);
        sendMessage(type, userid, appkey, fileContent, iMessageCallback, callback);
    }

    public static void sendFile(ConversationType type, String userid, String filePath, IMessageCallback iMessageCallback, SendMessageCallback callback) throws FileNotFoundException, JMFileSizeExceedException {
        sendFile(type, userid, null, filePath, iMessageCallback, callback);
    }

    /**
     * 发送地图位置
     * @param userid im username
     * @param longitude 经度
     * @param latitude 纬度
     * @param mapLevel 地图层级
     * @param street 街道
     * @param bitmapFilePath 地图Bitmap化后的文件路径
     * @param callback SendMessageCallback
     */
    public static void sendLocation(ConversationType type, String userid, String appkey, double latitude, double longitude, int mapLevel, String title, String street, String bitmapFilePath, String bitmapUrl, IMessageCallback iMessageCallback, SendMessageCallback callback) {
        File bitmapFile = new File(bitmapFilePath);
        if(TextUtils.isEmpty(bitmapFilePath) || !bitmapFile.exists()){
            if(callback != null) callback.onErr(-1, "地图文件不存在");
            return;
        }

        LocationContent locationContent = new LocationContent(latitude, longitude, mapLevel, street);
        locationContent.setStringExtra("path", bitmapFilePath);
        locationContent.setStringExtra("url", bitmapUrl);
        locationContent.setStringExtra("title", title);
        sendMessage(type, userid, appkey, locationContent, iMessageCallback, callback);
    }

    public static void sendLocation(ConversationType type, String userid, double latitude, double longitude, int mapLevel, String title, String street, String bitmapFilePath, String bitmapUrl, IMessageCallback iMessageCallback, SendMessageCallback callback) {
        sendLocation(type, userid, null, latitude, longitude, mapLevel, title, street, bitmapFilePath, bitmapUrl, iMessageCallback, callback);
    }

    /**
     * 发送自定义消息
     * @param userid im username
     * @param strings Map<key, string>
     * @param ints Map<key, int>
     * @param callback SendMessageCallback
     */
    public static void sendCustom(ConversationType type, String userid, String appkey, Map<String, String> strings, Map<String, Integer> ints, Map<String, Boolean> bools, IMessageCallback iMessageCallback, SendMessageCallback callback) {
        CustomContent customContent = new CustomContent();

        if (strings != null)
        for (Map.Entry<String, String> stringEntry : strings.entrySet()) {
            customContent.setStringValue(stringEntry.getKey(), stringEntry.getValue());
        }

        if (ints != null)
        for (Map.Entry<String, Integer> integerEntry : ints.entrySet()) {
            customContent.setNumberValue(integerEntry.getKey(),  integerEntry.getValue());
        }

        if (bools != null)
        for (Map.Entry<String, Boolean> booleanEntry : bools.entrySet()) {
            customContent.setBooleanValue(booleanEntry.getKey(),  booleanEntry.getValue());
        }

        sendMessage(type, userid, appkey, customContent, iMessageCallback, callback);
    }

    public static void sendCustom(ConversationType type, String userid, Map<String, String> strings, Map<String, Integer> ints, Map<String, Boolean> bools, IMessageCallback iMessageCallback, SendMessageCallback callback) {
        sendCustom(type, userid, null, strings, ints, bools, iMessageCallback, callback);
    }

    /**
     * 发送通用的会话消息
     * @param targetId app userid == im username OR groupid
     * @param message 通用的消息
     * @param iMessageCallback IMessageCallback 创建的消息回调
     * @param callback SendMessageCallback 发送结果回调, 如果为null, 可以在其他地方监听结果
     */
    protected static void sendMessage(ConversationType type, String targetId, String appkey, MessageContent message, IMessageCallback iMessageCallback, final SendMessageCallback callback){
        Conversation conv;
        if(type == ConversationType.Single) {
            conv = JMessageClient.getSingleConversation(targetId, getAppkey(appkey));
            if(conv == null) conv = Conversation.createSingleConversation(targetId, getAppkey(appkey));
        } else if (type == ConversationType.Group) {
            conv  = JMessageClient.getGroupConversation(Long.parseLong(targetId));
            if(conv == null) conv = Conversation.createGroupConversation(Long.parseLong(targetId));
        } else {
            return;
        }

        Message msg = conv.createSendMessage(message);
        // Message message = conv.createSendMessageAtAllMember(content, null); // 发送消息并@所有人
        // Message message = conv.createSendMessage(content, atlist, null); // 发送消息并@指定的人
        if(iMessageCallback != null) iMessageCallback.onMessage(msg);

        MessageSendingOptions options = new MessageSendingOptions();
        options.setNeedReadReceipt(NeedReadReceipt);
        JMessageClient.sendMessage(msg, options);

        msg.setOnSendCompleteCallback(new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                if(i == 0) {
                    if(callback != null) callback.onOK();
                } else {
                    if(callback != null) callback.onErr(i, s);
                }
            }
        });
    }

    // =========================================== 发送会话消息 ↑ ===========================================

    // =========================================== 发送命令消息 ↓ ===========================================

    /**
     * 发送输入命令消息, 向对方表示, 本人正在数据
     * @param type ConversationType
     * @param targetId
     * @param content 命令内容
     */
    public static void sendTransCommand(ConversationType type, String targetId, String appkey, String content){
        if (type == ConversationType.Single) JMessageClient.sendSingleTransCommand(targetId, getAppkey(appkey), content, null);
        else if (type == ConversationType.Group);
        else if (type == ConversationType.ChatRoom);
    }

    public static void sendTransCommand(ConversationType type, String targetId, String content){
        sendTransCommand(type, targetId, null, content);
    }

    // =========================================== 发送命令消息 ↑ ===========================================

    // =========================================== 消息的其他功能 ↓ ===========================================

    /**
     * 已读回执
     * 设置该消息为已读
     * @param msg Message
     */
    public static void readed(ConversationType type, Message msg){
        if(type == ConversationType.Single || type == ConversationType.Group){
            if(msg.getDirect() == MessageDirect.receive && !msg.haveRead()) msg.setHaveRead(null);
        } else {
            // chatRoom
        }
    }

    // =========================================== 消息的其他功能 ↑ ===========================================

    /**
     * 获取 AppKey
     */
    public static String getAppkey(String appkey) {
        // 如果输入的 AppKey 为空, 则使用自己的 AppKey
        return TextUtils.isEmpty(appkey) ? cn.jmessage.b.a.a.a() : appkey;
    }
}
