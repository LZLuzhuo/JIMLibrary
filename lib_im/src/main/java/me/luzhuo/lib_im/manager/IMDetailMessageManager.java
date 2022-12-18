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
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.api.BasicCallback;
import me.luzhuo.lib_core.app.base.CoreBaseApplication;
import me.luzhuo.lib_core.ui.dialog.Dialog;
import me.luzhuo.lib_core.ui.toast.ToastManager;
import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.manager.enums.DetailCustomMessageType;
import me.luzhuo.lib_im.manager.enums.DetailMessageType;
import me.luzhuo.lib_im.manager.enums.DetailOtherMessageType;

import static me.luzhuo.lib_im.manager.IMCommonConfig.NeedReadReceipt;

public class IMDetailMessageManager {
    private final static MediaPlayer mediaPlayer = new MediaPlayer();
    public IMDetailMessageManager() {
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
    }
    /**
     * 获取View的type, 用于Adapter
     * @param msg Message
     */
    public static int getViewType(Message msg) {
        for (DetailMessageType value : DetailMessageType.values()) {
            if(value.contentType == msg.getContentType()){
                if(msg.getDirect() == MessageDirect.receive) return value.leftType;
                else return value.rightType;
            }
        }

        for (DetailOtherMessageType value : DetailOtherMessageType.values()) {
            if(value.contentType == msg.getContentType()) return value.type;
        }

        if (ContentType.custom == msg.getContentType()) {
            final CustomContent content = (CustomContent) msg.getContent();
            int type = content.getNumberValue("ws_type").intValue();
            if (type == 2) { // 语音通话
                if (msg.getDirect() == MessageDirect.receive) return DetailCustomMessageType.VoicePhont.leftType;
                else return DetailCustomMessageType.VoicePhont.rightType;
            }
        }

        return 0;
    }

    public static int getViewRes(Message msg) {
        for (DetailMessageType value : DetailMessageType.values()) {
            if(value.contentType == msg.getContentType()){
                if(msg.getDirect() == MessageDirect.receive) return value.leftLayout;
                else return value.rightLayout;
            }
        }

        for (DetailOtherMessageType value : DetailOtherMessageType.values()) {
            if(value.contentType == msg.getContentType()) return value.layout;
        }

        if (ContentType.custom == msg.getContentType()) {
            final CustomContent content = (CustomContent) msg.getContent();
            int type = content.getNumberValue("ws_type").intValue();
            if (type == 2) { // 语音通话
                if (msg.getDirect() == MessageDirect.receive) return DetailCustomMessageType.VoicePhont.leftLayout;
                else return DetailCustomMessageType.VoicePhont.rightLayout;
            }
        }

        return R.layout.im_detail_default;
    }

    /**
     * 重发消息
     * @param context Context
     * @param data Message
     * @param err Err TextView
     * @param progress progress Bar
     */
    public static void reSendTextMessage(Context context, final Message data, final View err, final View progress) {
        if (data == null) {
            ToastManager.show(context, "该数据以不存在, 不能重发");
            return;
        }

        Dialog.instance().show(context, null, "重发该消息", "重发", "取消", false, new Dialog.OnClickListener() {
            @Override
            public void onOk(Object o) {
                if (err != null) err.setVisibility(View.GONE);
                if (progress != null) progress.setVisibility(View.VISIBLE);
                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        if (progress != null) progress.setVisibility(View.GONE);
                        if (status != 0) if (err != null) err.setVisibility(View.VISIBLE);
                    }
                });
                MessageSendingOptions options = new MessageSendingOptions();
                options.setNeedReadReceipt(NeedReadReceipt);
                JMessageClient.sendMessage(data, options);
            }
            @Override
            public void onCancel(Object o) { }
        }, null);
    }

    public static void reSendImageMessage(Context context, final Message data, final View err, final View progress, final View image_progress) {
        Dialog.instance().show(context, null, "重发该消息", "重发", "取消", false, new Dialog.OnClickListener() {
            @Override
            public void onOk(Object o) {
                if (err != null) err.setVisibility(View.GONE);
                if (progress != null) progress.setVisibility(View.VISIBLE);
                if (image_progress != null) image_progress.setVisibility(View.VISIBLE);
                /*data.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                    @Override
                    public void onProgressUpdate(double v) { }
                });*/
                data.setOnSendCompleteCallback(new BasicCallback() {
                    @Override
                    public void gotResult(final int status, String desc) {
                        if (progress != null) progress.setVisibility(View.VISIBLE);
                        if (image_progress != null) image_progress.setVisibility(View.VISIBLE);
                        if (status != 0) if (err != null) err.setVisibility(View.GONE);
                    }
                });

                MessageSendingOptions options = new MessageSendingOptions();
                options.setNeedReadReceipt(NeedReadReceipt);
                JMessageClient.sendMessage(data, options);
            }
            @Override
            public void onCancel(Object o) { }
        }, null);
    }

    /**
     * 是否是永久区的消息
     * @param conv
     * @return
     */
    public static boolean isForever(Conversation conv) {
        return false;
    }

    /**
     * 根据图片尺寸缩放ImageView
     * @param imageView ImageView
     * @param imagePath local imagePath
     * @return scaled ImageView
     */
    public static ImageView scaleView(ImageView imageView, String imagePath) {
        if (imageView == null || TextUtils.isEmpty(imagePath)) return null;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, opts);

        //计算图片缩放比例
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        return setDensity(imageWidth, imageHeight, imageView);
    }

    private static ImageView setDensity(double imageWidth, double imageHeight, ImageView imageView) {
        if (imageWidth > 350) {
            imageWidth = 550;
            imageHeight = 250;
        } else if (imageHeight > 450) {
            imageWidth = 300;
            imageHeight = 450;
        } else if ((imageWidth < 50 && imageWidth > 20) || (imageHeight < 50 && imageHeight > 20)) {
            imageWidth = 200;
            imageHeight = 300;
        } else if (imageWidth < 20 || imageHeight < 20) {
            imageWidth = 100;
            imageHeight = 150;
        } else {
            imageWidth = 300;
            imageHeight = 450;
        }

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) imageWidth;
        params.height = (int) imageHeight;
        imageView.setLayoutParams(params);

        return imageView;
    }

    private static FileInputStream mFIS;
    private static FileDescriptor mFD;
    /**
     * 播放音频
     */
    public static void playVoice(Message data, final ImageView old_im_detail_voice_icon, final AnimationDrawable oldAnimation, final boolean isSender) {
        final VoiceContent content = (VoiceContent) data.getContent();
        try {
            mediaPlayer.reset();
            mFIS = new FileInputStream(content.getLocalPath());
            mFD = mFIS.getFD();
            mediaPlayer.setDataSource(mFD);
            if (false) mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL); // 靠近耳朵
            else mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    oldAnimation.start();
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    oldAnimation.stop();
                    mp.reset();
                    if (isSender) old_im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_right3);
                    else old_im_detail_voice_icon.setImageResource(R.mipmap.im_detail_voice_left3);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ToastManager.show(CoreBaseApplication.context, "语音文件丢失, 请重试");
            content.downloadVoiceFile(data, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if (status == 0) ToastManager.show(CoreBaseApplication.context, "语音下载完成");
                    else ToastManager.show(CoreBaseApplication.context, "语音文件已过期");
                }
            });
        } finally {
            try {
                if (mFIS != null) mFIS.close();
            } catch (IOException o) {o.printStackTrace();}
        }
    }
}
