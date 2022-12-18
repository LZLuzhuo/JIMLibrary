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
package me.luzhuo.lib_im.ui.layout.keyboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import me.luzhuo.lib_im.R;
import me.luzhuo.lib_im.ui.layout.AutoHeightLayout;
import me.luzhuo.lib_im.ui.layout.func.EmoticonsFuncView;
import me.luzhuo.lib_im.ui.layout.func.FuncLayout;
import me.luzhuo.lib_im.ui.layout.func.adapter.PageSetAdapter;
import me.luzhuo.lib_im.ui.layout.func.bean.PageSetEntity;
import me.luzhuo.lib_im.ui.utils.EmoticonsKeyboardUtils;
import me.luzhuo.lib_im.ui.weight.EmoticonsEditText;
import me.luzhuo.lib_im.ui.weight.RecordVoiceButton;
import me.luzhuo.lib_im.ui.weight.adapter.TextWatcherAdapter;

/**
 * 表情符号键盘
 * 集成了 表情输入框 语音按钮 功能页
 */
public class EmoticonsKeyBoard extends AutoHeightLayout implements View.OnClickListener, EmoticonsFuncView.OnEmoticonsPageViewListener,
        EmoticonsToolBarView.OnToolBarItemClickListener, EmoticonsEditText.OnBackKeyClickListener, FuncLayout.OnFuncChangeListener {

    public static final int FUNC_TYPE_EMOTION = -1;
    public static final int FUNC_TYPE_APPPS = -2;

    protected LayoutInflater mInflater;

    protected ImageView mBtnVoiceOrText;
    protected RecordVoiceButton mBtnVoice;
    protected EmoticonsEditText mEtChat;
    protected ImageView mBtnFace;
    protected RelativeLayout mRlInput;
    protected ImageView mBtnMultimedia;
    protected Button mBtnSend;
    protected FuncLayout mfuncLayout;

    protected EmoticonsFuncView mEmoticonsFuncView;
    protected EmoticonsIndicatorView mEmoticonsIndicatorView;
    protected EmoticonsToolBarView mEmoticonsToolBarView;

    protected boolean mDispatchKeyEventPreImeLock = false;

    public EmoticonsKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflateKeyboardBar();
        initView();
        initFuncView();
    }

    protected void inflateKeyboardBar() {
        mInflater.inflate(R.layout.im_keyboard, this);
    }

    protected View inflateFunc() {
        return mInflater.inflate(R.layout.im_keyboard_func_emoticon, null);
    }

    protected void initView() {
        mBtnVoiceOrText = (ImageView) findViewById(R.id.btn_voice_or_text);
        mBtnVoice = (RecordVoiceButton) findViewById(R.id.btn_voice);
        mEtChat = (EmoticonsEditText) findViewById(R.id.et_chat);
        mBtnFace = (ImageView) findViewById(R.id.btn_face);
        mRlInput = (RelativeLayout) findViewById(R.id.rl_input);
        mBtnMultimedia = (ImageView) findViewById(R.id.btn_multimedia);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mfuncLayout = (FuncLayout) findViewById(R.id.ly_kvml);

//        mBtnVoiceOrText.setOnClickListener(this);
        mBtnFace.setOnClickListener(this);
        mBtnMultimedia.setOnClickListener(this);
        mEtChat.setOnBackKeyClickListener(this);
    }

    protected void initFuncView() {
        initEmoticonFuncView();
        initEditView();
    }

    protected void initEmoticonFuncView() {
        View keyboardView = inflateFunc();
        mfuncLayout.addFuncView(FUNC_TYPE_EMOTION, keyboardView);
        mEmoticonsFuncView = ((EmoticonsFuncView) findViewById(R.id.view_epv));
        mEmoticonsIndicatorView = ((EmoticonsIndicatorView) findViewById(R.id.view_eiv));
        mEmoticonsToolBarView = ((EmoticonsToolBarView) findViewById(R.id.view_etv));
        mEmoticonsFuncView.setOnIndicatorListener(this);
        mEmoticonsToolBarView.setOnToolBarItemClickListener(this);
        mfuncLayout.setOnFuncChangeListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initEditView() {
        mEtChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!mEtChat.isFocused()) {
                    mEtChat.setFocusable(true);
                    mEtChat.setFocusableInTouchMode(true);
                }
                return false;
            }
        });

        mEtChat.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    mBtnSend.setVisibility(VISIBLE);
                    mBtnMultimedia.setVisibility(GONE);
                    mBtnSend.setBackgroundResource(R.drawable.im_btn_keyboard_send_bg);
                } else {
                    mBtnMultimedia.setVisibility(VISIBLE);
                    mBtnSend.setVisibility(GONE);
                }
            }
        });
    }

    public void setAdapter(PageSetAdapter pageSetAdapter) {
        if (pageSetAdapter != null) {
            ArrayList<PageSetEntity> pageSetEntities = pageSetAdapter.getPageSetEntityList();
            if (pageSetEntities != null) {
                for (PageSetEntity pageSetEntity : pageSetEntities) {
                    mEmoticonsToolBarView.addToolItemView(pageSetEntity);
                }
            }
        }
        mEmoticonsFuncView.setAdapter(pageSetAdapter);
    }

    /**
     * 添加功能页
     */
    public void addFuncView(View view) {
        mfuncLayout.addFuncView(FUNC_TYPE_APPPS, view);
    }

    /**
     * 重置键盘为初始状态
     */
    public void reset() {
        EmoticonsKeyboardUtils.closeSoftKeyboard(this);
        mfuncLayout.hideAllFuncView();
        mBtnFace.setImageResource(R.mipmap.im_keyboard_icon_face_nomal);
    }

    protected void showVoice() {
        mRlInput.setVisibility(GONE);
        mBtnVoice.setVisibility(VISIBLE);
        reset();
    }

    protected void checkVoice() {
        if (mBtnVoice.isShown()) {
            mBtnVoiceOrText.setImageResource(R.drawable.im_btn_keyboard_voice_or_text_keyboard);
        } else {
            mBtnVoiceOrText.setImageResource(R.drawable.im_btn_keyboard_voice_or_text);
        }
    }

    protected void showText() {
        mRlInput.setVisibility(VISIBLE);
        mBtnVoice.setVisibility(GONE);
    }

    /**
     * 打开表情页 FUNC_TYPE_EMOTION
     * 打开功能页 FUNC_TYPE_APPPS
     */
    protected void toggleFuncView(int key) {
        showText(); // 显示输入框, 隐藏语音框
        mfuncLayout.toggleFuncView(key, isSoftKeyboardPop(), mEtChat);
    }

    @Override
    public void onFuncChange(int key) {
        if (FUNC_TYPE_EMOTION == key) {
            // 键盘弹出
            mBtnFace.setImageResource(R.mipmap.im_keyboard_icon_face_pop);
        } else {
            // 默认表情
            mBtnFace.setImageResource(R.mipmap.im_keyboard_icon_face_nomal);
        }
        checkVoice();
    }

    protected void setFuncViewHeight(int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mfuncLayout.getLayoutParams();
        params.height = height;
        mfuncLayout.setLayoutParams(params);
    }

    /**
     * 键盘高度变化的回调
     */
    @Override
    public void onSoftKeyboardHeightChanged(int height) {
        mfuncLayout.updateHeight(height);
    }

    @Override
    public void OnSoftPop(int height) {
        super.OnSoftPop(height);
        mfuncLayout.setVisibility(true);
        onFuncChange(mfuncLayout.DEF_KEY);
    }

    @Override
    public void OnSoftClose() {
        super.OnSoftClose();
        if (mfuncLayout.isOnlyShowSoftKeyboard()) {
            reset();
        } else {
            onFuncChange(mfuncLayout.getCurrentFuncKey());
        }
    }

    public void addOnFuncKeyBoardListener(FuncLayout.OnFuncKeyBoardListener l) {
        mfuncLayout.addOnKeyBoardListener(l);
    }

    @Override
    public void emoticonSetChanged(PageSetEntity pageSetEntity) {
        mEmoticonsToolBarView.setToolBtnSelect(pageSetEntity.getUuid());
    }

    @Override
    public void playTo(int position, PageSetEntity pageSetEntity) {
        mEmoticonsIndicatorView.playTo(position, pageSetEntity);
    }

    @Override
    public void playBy(int oldPosition, int newPosition, PageSetEntity pageSetEntity) {
        mEmoticonsIndicatorView.playBy(oldPosition, newPosition, pageSetEntity);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_face) {
            toggleFuncView(FUNC_TYPE_EMOTION);
        } else if (i == R.id.btn_multimedia) {
            toggleFuncView(FUNC_TYPE_APPPS);
        }
    }

    public  void setVoiceText() {
        if (mRlInput.isShown()) {
            mBtnVoiceOrText.setImageResource(R.drawable.im_btn_keyboard_voice_or_text_keyboard);
            showVoice();
        } else {
            showText();
            mBtnVoiceOrText.setImageResource(R.drawable.im_btn_keyboard_voice_or_text);
            EmoticonsKeyboardUtils.openSoftKeyboard(mEtChat);
        }
    }

    public ImageView getVoiceOrText() {
        return mBtnVoiceOrText;
    }

    @Override
    public void onToolBarItemClick(PageSetEntity pageSetEntity) {
        mEmoticonsFuncView.setCurrentPageSet(pageSetEntity);
    }

    @Override
    public void onBackKeyClick() {
        if (mfuncLayout.isShown()) {
            mDispatchKeyEventPreImeLock = true;
            reset();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mDispatchKeyEventPreImeLock) {
                mDispatchKeyEventPreImeLock = false;
                return true;
            }
            if (mfuncLayout.isShown()) {
                reset();
                return true;
            } else {
                return super.dispatchKeyEvent(event);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (EmoticonsKeyboardUtils.isFullScreen((Activity) getContext())) {
            return false;
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        if (EmoticonsKeyboardUtils.isFullScreen((Activity) getContext())) {
            return;
        }
        super.requestChildFocus(child, focused);
    }

    public boolean dispatchKeyEventInFullScreen(KeyEvent event) {
        if (event == null) {
            return false;
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (EmoticonsKeyboardUtils.isFullScreen((Activity) getContext()) && mfuncLayout.isShown()) {
                    reset();
                    return true;
                }
            default:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    boolean isFocused;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        isFocused = mEtChat.getShowSoftInputOnFocus();
                    } else {
                        isFocused = mEtChat.isFocused();
                    }
                    if (isFocused) {
                        mEtChat.onKeyDown(event.getKeyCode(), event);
                    }
                }
                return false;
        }
    }

    public EmoticonsEditText getEtChat() {
        return mEtChat;
    }

    public RecordVoiceButton getBtnVoice() {
        return mBtnVoice;
    }

    public Button getBtnSend() {
        return mBtnSend;
    }

    public EmoticonsFuncView getEmoticonsFuncView() {
        return mEmoticonsFuncView;
    }

    public EmoticonsIndicatorView getEmoticonsIndicatorView() {
        return mEmoticonsIndicatorView;
    }

    public EmoticonsToolBarView getEmoticonsToolBarView() {
        return mEmoticonsToolBarView;
    }
}
