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
package me.luzhuo.lib_im.ui.weight;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.widget.AppCompatTextView;
import me.luzhuo.lib_im.R;

import static me.luzhuo.lib_im.ui.weight.RecordVoiceButton.VoiceState.按下说话;
import static me.luzhuo.lib_im.ui.weight.RecordVoiceButton.VoiceState.移出屏幕结束;
import static me.luzhuo.lib_im.ui.weight.RecordVoiceButton.VoiceState.移动取消发送;
import static me.luzhuo.lib_im.ui.weight.RecordVoiceButton.VoiceState.移动松开结束;
import static me.luzhuo.lib_im.ui.weight.RecordVoiceButton.VoiceState.说话抬起;


/**
 * 语音按钮
 */
public class RecordVoiceButton extends AppCompatTextView {
    public enum VoiceState{
        按下说话, 说话抬起, 移动取消发送, 移动松开结束, 移出屏幕结束
    }
    private VoiceState voiceState = 按下说话;

    private File myRecAudioFile;

    // 最小的取消时间
    private static final int MIN_INTERVAL_TIME = 1000;// 1s
    private final static int CANCEL_RECORD = 5;
    private final static int START_RECORD = 7;
    //依次为按下录音键坐标、手指离开屏幕坐标、手指移动坐标
    float mTouchY1, mTouchY2, mTouchY;
    // 最小的取消范围
    private final float MIN_CANCEL_DISTANCE = 300f;
    //依次为开始录音时刻，按下录音时刻，松开录音按钮时刻
    private long startTime, time1, time2;

    private Dialog recordIndicator;
    private Dialog mTimeShort;
    private ImageView mVolumeIv;
    private TextView mRecordHintTv;

    private MediaRecorder recorder;

    private ObtainDecibelThread mThread;

    private Handler mVolumeHandler;
    public static boolean mIsPressed = false;
    private Context mContext;
    private Timer timer = new Timer();
    private Timer mCountTimer;
    private boolean isTimerCanceled = false;
    private boolean mTimeUp = false;
    private final MyHandler myHandler = new MyHandler(this);
    private static int[] res;
    private Chronometer mVoiceTime;
    private TextView mTimeDown;
    private LinearLayout mMicShow;

    public RecordVoiceButton(Context context) {
        super(context);
        init();
    }

    public RecordVoiceButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        init();
    }

    public RecordVoiceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    private void init() {
        mVolumeHandler = new ShowVolumeHandler(this);
        //如果需要跳动的麦克图 将五张相同的图片替换即可
        res = new int[] {R.mipmap.im_icon_voice_one, R.mipmap.im_icon_voice_two,
                R.mipmap.im_icon_voice_three, R.mipmap.im_icon_voice_four,
                R.mipmap.im_icon_voice_five, R.mipmap.im_icon_voice_rutern};
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.setPressed(true);
        int action = event.getAction();
        // 说话时间太短
        mTimeShort = new Dialog(getContext(), R.style.im_dialog_voice_record);
        mTimeShort.setContentView(R.layout.im_dialog_voice_send_time_short);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                voiceState = 按下说话;
                if(onUserVoiceListener != null) onUserVoiceListener.onUserSpeaking();

                //文字 松开结束
                this.setText(R.string.im_voice_btn_send_hint);
                mIsPressed = true;
                time1 = System.currentTimeMillis();
                mTouchY1 = event.getY();
                //检查sd卡是否存在
                /*if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {*/
                    if (isTimerCanceled) {
                        timer = createTimer();
                    }
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            android.os.Message msg = myHandler.obtainMessage();
                            msg.what = START_RECORD;
                            msg.sendToTarget();
                        }
                    }, 300);
                /*} else {
                    Toast.makeText(this.getContext(), R.string.im_voice_toast_sdcard_not_exist, Toast.LENGTH_SHORT).show();
                    this.setPressed(false);
                    //文字 按住说话
                    this.setText(R.string.im_voice_btn_record_cancel_hint);
                    mIsPressed = false;
                    return false;
                }*/
                break;
            case MotionEvent.ACTION_UP:
                voiceState = 说话抬起;
                if(onUserVoiceListener != null) onUserVoiceListener.onUserSpeaked();

                //文字 按住说话
                this.setText(R.string.im_voice_btn_record_hint);
                mIsPressed = false;
                this.setPressed(false);
                mTouchY2 = event.getY();
                time2 = System.currentTimeMillis();
                if (time2 - time1 < 300) {
                    showCancelDialog();
                    return true;
                } else if (time2 - time1 < MIN_INTERVAL_TIME) {
                    showCancelDialog();
                    cancelRecord();
                } else if (mTouchY1 - mTouchY2 > MIN_CANCEL_DISTANCE) {
                    cancelRecord();
                } else if (time2 - time1 < 60000) {
                    finishRecord();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchY = event.getY();
                //手指上滑到超出限定后，显示松开取消发送提示
                if (mTouchY1 - mTouchY > MIN_CANCEL_DISTANCE) {
                    voiceState = 移动取消发送;
                    //文字  松开手指取消发送
                    this.setText(R.string.im_voice_btn_record_cancel_hint);
                    mVolumeHandler.sendEmptyMessage(CANCEL_RECORD);
                    if (mThread != null) {
                        mThread.exit();
                    }
                    mThread = null;
                } else {
                    voiceState = 移动松开结束;
                    //文字 送开结束
                    this.setText(R.string.im_voice_btn_send_hint);
                    if (mThread == null) {
                        mThread = new ObtainDecibelThread();
                        mThread.start();
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:// 当手指移动到view外面，会cancel
                voiceState = 移出屏幕结束;
                //文字 按住说话
                this.setText(R.string.im_voice_btn_record_hint);
                cancelRecord();
                break;
        }

        return true;
    }

    private void showCancelDialog() {
        mTimeShort.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mTimeShort.dismiss();
            }
        }, 1000);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            isTimerCanceled = true;
        }
        if (mCountTimer != null) {
            mCountTimer.cancel();
            mCountTimer.purge();
        }
    }

    private Timer createTimer() {
        timer = new Timer();
        isTimerCanceled = false;
        return timer;
    }

    private void initDialogAndStartRecord() {
        //存放录音文件目录
        File rootDir = mContext.getFilesDir();
        String fileDir = rootDir.getAbsolutePath() + "/voice";
        File destDir = new File(fileDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        //录音文件的命名格式
        myRecAudioFile = new File(fileDir, new DateFormat().format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + ".amr");
        if (myRecAudioFile == null) {
            cancelTimer();
            stopRecording();
            Toast.makeText(mContext, R.string.im_voice_toast_create_file_failed, Toast.LENGTH_SHORT).show();
        }
        recordIndicator = new Dialog(getContext(), R.style.im_dialog_voice_record);
        recordIndicator.setContentView(R.layout.im_dialog_voice_record);
        mVolumeIv = (ImageView) recordIndicator.findViewById(R.id.jmim_volume_hint_iv);
        mRecordHintTv = (TextView) recordIndicator.findViewById(R.id.jmim_record_voice_tv);
        mVoiceTime = (Chronometer) recordIndicator.findViewById(R.id.voice_time);

        mTimeDown = (TextView) recordIndicator.findViewById(R.id.time_down);
        mMicShow = (LinearLayout) recordIndicator.findViewById(R.id.mic_show);

        mRecordHintTv.setText(R.string.im_voice_btn_move_to_cancel_hint);
        startRecording();
        recordIndicator.show();
    }

    //录音完毕加载 ListView item
    private void finishRecord() {
        cancelTimer();
        stopRecording();

        if (recordIndicator != null) {
            recordIndicator.dismiss();
        }

        long intervalTime = System.currentTimeMillis() - startTime;
        if (intervalTime < MIN_INTERVAL_TIME) {
            mMicShow.setVisibility(GONE);
            myRecAudioFile.delete();
        } else {
            mMicShow.setVisibility(VISIBLE);
            if (myRecAudioFile != null && myRecAudioFile.exists()) {
                MediaPlayer mp = new MediaPlayer();
                try {
                    FileInputStream fis = new FileInputStream(myRecAudioFile);
                    mp.setDataSource(fis.getFD());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //某些手机会限制录音，如果用户拒接使用录音，则需判断mp是否存在
                if (mp != null) {
                    int duration = mp.getDuration() / 1000;//即为时长 是s
                    if (duration < 1) {
                        duration = 1;
                    } else if (duration > 60) {
                        duration = 60;
                    }
                    if(onUserVoiceListener != null) onUserVoiceListener.onVoiceContent(myRecAudioFile, duration);

                } else {
                    Toast.makeText(mContext, R.string.im_voice_toast_record_voice_permission_request, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //取消录音，清除计时
    private void cancelRecord() {
        //可能在消息队列中还存在HandlerMessage，移除剩余消息
        mVolumeHandler.removeMessages(56, null);
        mVolumeHandler.removeMessages(57, null);
        mVolumeHandler.removeMessages(58, null);
        mVolumeHandler.removeMessages(59, null);
        mTimeUp = false;
        cancelTimer();
        stopRecording();
        if (recordIndicator != null) {
            recordIndicator.dismiss();
        }
        if (myRecAudioFile != null) {
            myRecAudioFile.delete();
        }
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile(myRecAudioFile.getAbsolutePath());
            myRecAudioFile.createNewFile();
            recorder.prepare();
            recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mediaRecorder, int i, int i2) {
                    Log.i("RecordVoiceController", "recorder prepare failed!");
                }
            });
            recorder.start();
            startTime = System.currentTimeMillis();

            mVoiceTime.setBase(SystemClock.elapsedRealtime());
            mVoiceTime.start();

            mCountTimer = new Timer();
            mCountTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mTimeUp = true;
                    android.os.Message msg = mVolumeHandler.obtainMessage();
                    msg.what = 50;
                    Bundle bundle = new Bundle();
                    bundle.putInt("restTime", 10);
                    msg.setData(bundle);
                    msg.sendToTarget();
                    mCountTimer.cancel();
                }
            }, 51000);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.im_voice_toast_illegal_state, Toast.LENGTH_SHORT).show();
            cancelTimer();
            dismissDialog();
            if (mThread != null) {
                mThread.exit();
                mThread = null;
            }
            if (myRecAudioFile != null) {
                myRecAudioFile.delete();
            }
            recorder.release();
            recorder = null;
        } catch (RuntimeException e) {
            Toast.makeText(mContext, R.string.im_voice_toast_record_voice_permission_denied, Toast.LENGTH_SHORT).show();
            cancelTimer();
            dismissDialog();
            if (mThread != null) {
                mThread.exit();
                mThread = null;
            }
            if (myRecAudioFile != null) {
                myRecAudioFile.delete();
            }
            recorder.release();
            recorder = null;
        }


        mThread = new ObtainDecibelThread();
        mThread.start();

    }

    //停止录音，隐藏录音动画
    private void stopRecording() {
        if (mThread != null) {
            mThread.exit();
            mThread = null;
        }
        releaseRecorder();
    }

    public void releaseRecorder() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (Exception e) {
                Log.d("RecordVoice", "Catch exception: stop recorder failed!");
            } finally {
                recorder.release();
                recorder = null;
            }
        }
    }

    /**
     * 控制音量动画
     */
    private class ObtainDecibelThread extends Thread {

        private volatile boolean running = true;

        public void exit() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (recorder == null || !running) {
                    break;
                }
                try {
                    if(voiceState == 移动取消发送) break;

                    int x = recorder.getMaxAmplitude();
                    if (x != 0) {
                        int f = (int) (10 * Math.log(x) / Math.log(10));
                        if (f < 20) {
                            mVolumeHandler.sendEmptyMessage(0);
                        } else if (f < 26) {
                            mVolumeHandler.sendEmptyMessage(1);
                        } else if (f < 32) {
                            mVolumeHandler.sendEmptyMessage(2);
                        } else if (f < 38) {
                            mVolumeHandler.sendEmptyMessage(3);
                        } else {
                            mVolumeHandler.sendEmptyMessage(4);
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public void dismissDialog() {
        if (recordIndicator != null) {
            recordIndicator.dismiss();
        }
        this.setText(R.string.im_voice_btn_record_hint);
    }

    /**
     * 录音动画控制
     */
    private static class ShowVolumeHandler extends Handler {

        private final WeakReference<RecordVoiceButton> lButton;

        public ShowVolumeHandler(RecordVoiceButton button) {
            lButton = new WeakReference<>(button);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            RecordVoiceButton controller = lButton.get();
            if (controller != null) {
                int restTime = msg.getData().getInt("restTime", -1);
                // 若restTime>0, 进入倒计时
                if (restTime > 0) {
                    controller.mTimeUp = true;
                    android.os.Message msg1 = controller.mVolumeHandler.obtainMessage();
                    msg1.what = 60 - restTime + 1;
                    Bundle bundle = new Bundle();
                    bundle.putInt("restTime", restTime - 1);
                    msg1.setData(bundle);
                    //创建一个延迟一秒执行的HandlerMessage，用于倒计时
                    controller.mVolumeHandler.sendMessageDelayed(msg1, 1000);

                    //还可以说...秒
//                    controller.mRecordHintTv.setText(String.format(controller.mContext.getString(IdHelper
//                            .getString(controller.mContext, "jmim_rest_record_time_hint")), restTime));
                    controller.mMicShow.setVisibility(GONE);
                    controller.mTimeDown.setVisibility(VISIBLE);
                    controller.mTimeDown.setText(restTime + "");

                    // 倒计时结束，发送语音, 重置状态
                } else if (restTime == 0) {
                    controller.finishRecord();
                    controller.setPressed(false);
                    controller.mTimeUp = false;
                    // restTime = -1, 一般情况
                } else {
                    // 没有进入倒计时状态
                    if (!controller.mTimeUp) {
                        if (msg.what < CANCEL_RECORD) {
                            controller.mRecordHintTv.setText(R.string.im_voice_btn_move_to_cancel_hint);
                            // dialog_bg
                            controller.mMicShow.setBackgroundColor(Color.parseColor("#50000000"));
                        } else {
                            if(controller.mRecordHintTv == null) return; // 当用户点开语音, 然后瞬间上滑取消时, 可能dialog还没有初始化

                            controller.mRecordHintTv.setText(R.string.im_voice_btn_record_cancel_hint);
                            controller.mMicShow.setBackgroundColor(Color.parseColor("#6FF54F53"));
                        }
                        // 进入倒计时
                    } else {
                        if (msg.what == CANCEL_RECORD) {
                            controller.mRecordHintTv.setText(R.string.im_voice_btn_record_cancel_hint);
                            controller.mMicShow.setBackgroundColor(Color.parseColor("#6FF54F53"));
                            if (!mIsPressed) {
                                controller.cancelRecord();
                            }
                        }
                    }
                    controller.mVolumeIv.setImageResource(res[msg.what]);
                }
            }
        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<RecordVoiceButton> lButton;

        public MyHandler(RecordVoiceButton button) {
            lButton = new WeakReference<>(button);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            RecordVoiceButton controller = lButton.get();
            if (controller != null) {
                switch (msg.what) {
                    case START_RECORD:
                        if (mIsPressed) {
                            controller.initDialogAndStartRecord();
                        }
                        break;
                }
            }
        }
    }

    /**
     * 设置用户语音监听
     */
    public void setOnUserVoiceListener(OnUserVoiceListener voiceListener) {
        this.onUserVoiceListener = voiceListener;
    }
    private OnUserVoiceListener onUserVoiceListener;
    public interface OnUserVoiceListener{
        /**
         * 用户正在语音说话
         */
        void onUserSpeaking();

        /**
         * 用户已停止语音说话
         */
        void onUserSpeaked();

        /**
         * 用户说完后产生的Voice文件
         * @param myRecAudioFile 语音文件
         * @param duration 语音文件长度, 最长60s
         */
        void onVoiceContent(File myRecAudioFile, int duration);
    }
}