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
package me.luzhuo.lib_im.ui.layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 软键盘的弹出和关闭会对Activity可视根布局DecorView大小进行改变, 对该布局的变大和缩小的监听, 来判断键盘的弹出还是关闭了.
 * 由于官方并没有提供判断软键盘是否弹出方法, 所以使用该方法来判断.
 * RelativeLayout布局大小并不会影响软键盘是否弹出的判断.
 */
public class SoftKeyboardSizeWatchLayout extends RelativeLayout {

    private Context mContext;
    private int mOldh = -1;
    private int mNowh = -1;
    protected int mScreenHeight = 0;
    protected boolean mIsSoftKeyboardPop = false; // 键盘是否弹出

    public SoftKeyboardSizeWatchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                // 获取Activity的可见大小
                ((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                if (mScreenHeight == 0) {
                    mScreenHeight = r.bottom;
                }
                mNowh = mScreenHeight - r.bottom;
                /*
                 如果上次比这次大, 说明布局被上移, 是由于键盘的弹出
                 如果上次比这次小, 说明布局被下移, 是由于键盘的关闭
                 */
                if (mOldh != -1 && mNowh != mOldh) {
                    if (mNowh > 0) {
                        mIsSoftKeyboardPop = true;
                        if (mListenerList != null) {
                            for (OnResizeListener l : mListenerList) {
                                l.OnSoftPop(mNowh);
                            }
                        }
                    } else {
                        mIsSoftKeyboardPop = false;
                        if (mListenerList != null) {
                            for (OnResizeListener l : mListenerList) {
                                l.OnSoftClose();
                            }
                        }
                    }
                }
                mOldh = mNowh;
            }
        });
    }

    /**
     * 软键盘是否弹起
     */
    public boolean isSoftKeyboardPop() {
        return mIsSoftKeyboardPop;
    }

    private List<OnResizeListener> mListenerList;

    /**
     * 设置软键盘开启和关闭的监听,
     * 主要是监听了软键盘的弹出, 把布局往丄推, 使可视布局变小; 反之则变大的原理.
     */
    public void addOnResizeListener(OnResizeListener l) {
        if (mListenerList == null) {
            mListenerList = new ArrayList<>();
        }
        mListenerList.add(l);
    }

    public interface OnResizeListener {
        /**
         * 软键盘弹起
         */
        void OnSoftPop(int height);

        /**
         * 软键盘关闭
         */
        void OnSoftClose();
    }
}