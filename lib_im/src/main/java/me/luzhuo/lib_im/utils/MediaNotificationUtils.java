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
package me.luzhuo.lib_im.utils;

import android.app.Service;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.io.IOException;

/**
 * 通知媒体类: 震动 + 播放音频
 */
public class MediaNotificationUtils {

    private Context context;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Vibrator vibrator;
    private String assetFileName;

    public MediaNotificationUtils(Context context) throws IOException {
        this(context, "base_notification.mp3");
    }

    /**
     * @param assetFileName Asset 目录下的音频文件名
     */
    public MediaNotificationUtils(Context context, String assetFileName) throws IOException {
        this.context = context;
        this.vibrator = (Vibrator) context.getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        this.assetFileName = assetFileName;
        initMeida();
    }

    private void initMeida() throws IOException {
        AssetFileDescriptor openFd = context.getAssets().openFd(assetFileName);
        mediaPlayer.setDataSource(openFd.getFileDescriptor(), openFd.getStartOffset(), openFd.getLength());
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        mediaPlayer.setLooping(true);
        mediaPlayer.prepare();
    }

    public void start() {
        start(new long[]{300L,400L});
    }

    /**
     * 通知: 震动 + 提示音
     * @param pattern 节制模式
     */
    public void start(long[] pattern) {
        if (mediaPlayer.isPlaying()) return;

        // 震动
        vibrator.vibrate(pattern, 0);
        // 响铃
        mediaPlayer.start();
    }

    public void stop() throws IOException {
        // 取消震动
        vibrator.cancel();

        // 停止响铃
        mediaPlayer.reset();
        initMeida();
    }
}
